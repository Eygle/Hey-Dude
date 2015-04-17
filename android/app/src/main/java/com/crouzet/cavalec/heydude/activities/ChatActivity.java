package com.crouzet.cavalec.heydude.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.crouzet.cavalec.heydude.HeyDudeConstants;
import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.adapters.ChatAdapter;
import com.crouzet.cavalec.heydude.db.MessagesDataSource;
import com.crouzet.cavalec.heydude.http.ApiUtils;
import com.crouzet.cavalec.heydude.model.Message;
import com.crouzet.cavalec.heydude.model.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ChatActivity extends ActionBarActivity {
    private final int TIMEOUT_DELAY = 30000;

    private MenuItem callIcon;
    private EditText editTextMsg;

    private ListView listView;
    private List<Message> msgs;
    private ChatAdapter adapter;

    MessagesDataSource db = new MessagesDataSource(this);

    private ProgressDialog loader = null;

    private Handler handler = new Handler();

    public static boolean mRunning = false;

    private boolean callInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            ActionBar bar = getSupportActionBar();
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle(HeyDudeSessionVariables.dest.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("ACCEPT_CALL")) {
            callAccepted();
        }

        editTextMsg = (EditText)findViewById(R.id.chat_message);

        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initListView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(messageReceiver, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_MSG));
        registerReceiver(callAnswerReceiver, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_CALL_ANSWER));
        registerReceiver(receiveCall, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_CALL));
        registerReceiver(receiveHangup, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_HANGUP));

        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(messageReceiver);
        unregisterReceiver(callAnswerReceiver);
        unregisterReceiver(receiveCall);
        unregisterReceiver(receiveHangup);
    }

    @Override
    protected void onStop() {
        super.onStop();

        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);

        callIcon = menu.findItem(R.id.action_call);

        if (callInProgress) {
            callIcon.setIcon(R.drawable.hangup);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        super.onOptionsItemSelected(item);

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                ApiUtils.hangup();
                this.finish();
                break;
            case R.id.action_call:
                if (callInProgress) {
                    hangup();
                } else {
                    call();
                }
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initListView() {
        msgs = db.getAllMessages(HeyDudeSessionVariables.dest.getName());

        adapter = new ChatAdapter(this, msgs);

        listView = (ListView)findViewById(R.id.chat_listview);
        listView.setAdapter(adapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);

        final Context context = this;

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Message msg = adapter.getItem(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.message_actions)
                        .setItems(R.array.message_actions_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    ClipboardManager clipboard = (ClipboardManager)
                                            getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("simple text", msg.getMessage());
                                    clipboard.setPrimaryClip(clip);
                                } else if (which == 1) {
                                    db.deleteMessage(msg);
                                    msgs.remove(msg);
                                    adapter.notifyDataSetChanged();
                                    listView.setSelection(position);
                                }
                            }
                        });
                builder.show();
                return false;
            }
        });
    }

    private void call() {
        mRunning = true;
        showLoader();
        ApiUtils.call();
    }

    private void hangup() {
        ApiUtils.hangup();

        callIcon.setIcon(R.drawable.call);
        findViewById(R.id.chat_form).setVisibility(View.GONE);
    }

    private void showLoader() {
        if (loader == null) {
            loader = new ProgressDialog(ChatActivity.this);
            loader.setTitle(R.string.call_loader_title);
            loader.setMessage(String.format(getString(R.string.call_loader_message), HeyDudeSessionVariables.dest.getName()));
            loader.setCancelable(false);
            loader.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    hangup();
                    mRunning = false;
                    hideLoader();
                }
            });
        }
        loader.show();
    }

    private void hideLoader() {
        if (loader != null && loader.isShowing()) {
            loader.dismiss();
        }
    }

    private void callAccepted() {
        callInProgress = true;
        hideLoader();
        findViewById(R.id.chat_form).setVisibility(View.VISIBLE);

        if (callIcon != null)
            callIcon.setIcon(R.drawable.hangup);
    }

    private void callRefused() {
        hideLoader();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.dialog_call_refused), HeyDudeSessionVariables.dest.getName()))
                .setNeutralButton(R.string.ok, null);
        builder.create().show();
    }

    private void callTimeout() {
        hideLoader();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.dialog_call_timeout), HeyDudeSessionVariables.dest.getName()))
                .setNeutralButton(R.string.ok, null);
        builder.create().show();
    }

    public void sendMessage(View v) {
        final String msg = editTextMsg.getText().toString();

        if (msg.length() == 0) {
            Toast.makeText(this, R.string.message_empty_error, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiUtils.sendMessage(msg);

        editTextMsg.setText("");

        Message message = db.createMessage(msg,
                HeyDudeSessionVariables.me.getName(),
                HeyDudeSessionVariables.dest.getName(),
                HeyDudeSessionVariables.me.getImage(),
                new Date());
        msgs.add(message);
        adapter.notifyDataSetChanged();
    }

    public void receivedMessage(String msg) {
        Message message = db.createMessage(msg,
                HeyDudeSessionVariables.dest.getName(),
                HeyDudeSessionVariables.me.getName(),
                HeyDudeSessionVariables.dest.getImage(),
                new Date());
        msgs.add(message);
        adapter.notifyDataSetChanged();
    }

    /**
     * Broadcast received when online users data are updated from server
     */
    protected BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle b = intent.getExtras();
            receivedMessage(b.getString(HeyDudeConstants.BROADCAST_DATA_MSG));
        }
    };

    /**
     * Broadcast received when online users data are updated from server
     */
    protected BroadcastReceiver callAnswerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String status = b.getString(HeyDudeConstants.BROADCAST_DATA_CALL_STATUS);

            switch (status) {
                case "accept":
                    callAccepted();
                    break;
                case "refuse":
                    callRefused();
                    break;
            }
        }
    };

    /**
     * Broadcast received when user receive a call
     */
    protected BroadcastReceiver receiveCall = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String id = b.getString(HeyDudeConstants.BROADCAST_DATA_DEST);

            User caller = null;
            for (User user : HeyDudeSessionVariables.onlineUsers) {
                if (user.getId().equals(id)) {
                    caller = user;
                }
            }
            if (caller == null) return;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final User finalCaller = caller;
            builder.setTitle(String.format(context.getString(R.string.receive_call_title), caller.getName()))
                    .setMessage(String.format(context.getString(R.string.receive_call_message), caller.getName()))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            ApiUtils.answerCall(ApiUtils.ACCEPT_CALL, finalCaller.getId());

                            if (finalCaller.getId().equals(HeyDudeSessionVariables.dest.getId())) {
                                callAccepted();
                            } else {
                                HeyDudeSessionVariables.dest = finalCaller;
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra("ACCEPT_CALL", true);
                                startActivity(intent);
                                ChatActivity.this.finish();
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ApiUtils.answerCall(ApiUtils.REFUSE_CALL, finalCaller.getId());
                        }
                    });
            // Create the AlertDialog object and show it
            builder.create().show();
        }
    };

    /**
     * Broadcast received user receive a hangup
     */
    protected BroadcastReceiver receiveHangup = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String id = b.getString(HeyDudeConstants.BROADCAST_DATA_DEST);

            if ((callInProgress || (loader != null && loader.isShowing())) && HeyDudeSessionVariables.dest.getId().equals(id)) {
                callInProgress = false;
                hideLoader();

                callIcon.setIcon(R.drawable.call);
                findViewById(R.id.chat_form).setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this,
                        String.format(getString(R.string.user_has_hangup),HeyDudeSessionVariables.dest.getName()),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
}
