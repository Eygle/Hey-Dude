package com.crouzet.cavalec.heydude.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.adapters.ChatAdapter;
import com.crouzet.cavalec.heydude.db.MessagesDataSource;
import com.crouzet.cavalec.heydude.interfaces.ReceiverCallback;
import com.crouzet.cavalec.heydude.model.Message;
import com.crouzet.cavalec.heydude.http.ApiUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ChatActivity extends ActionBarActivity implements ReceiverCallback {

    private final int CHECK_CALLS_STATUS_DELAY = 3000;

    //private ReadSocket readSocket;
    //private SendSocket sendSocket;

    private EditText editTextMsg;

    private ListView listView;
    private List<Message> msgs;
    private ChatAdapter adapter;

    MessagesDataSource db = new MessagesDataSource(this);

    private ProgressDialog loader = null;

    private Handler handler = new Handler();

    public static boolean mRunning = false;

    private Runnable checkCallStatus = new Runnable() {
        @Override
        public void run() {
            final Runnable r = this;

            ApiUtils.getCallStatus(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    if (!mRunning) return;
                    try {
                        Log.d(">>>>>>>>>>", response.toString());
                        switch (response.getString("status")) {
                            case "accept":
                                callAccepted();
                                break;
                            case "refuse":
                                callRefused();
                                break;
                            case "wait":
                                handler.postDelayed(r, CHECK_CALLS_STATUS_DELAY);
                                break;
                            case "timeout":
                                callTimeout();
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

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

        try {
            db.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        db.close();
        //sendSocket.closeSocket();
        //readSocket.closeSocket();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
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
                call();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        ApiUtils.hangup();
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
        ApiUtils.call(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                showLoader();
                handler.postDelayed(checkCallStatus, CHECK_CALLS_STATUS_DELAY);
            }
        });

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
        hideLoader();
        findViewById(R.id.chat_form).setVisibility(View.VISIBLE);
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
        String msg = editTextMsg.getText().toString();

        if (msg.length() == 0) {
            Toast.makeText(this, R.string.message_empty_error, Toast.LENGTH_SHORT).show();
            return;
        }

        //sendSocket.send(msg);
        editTextMsg.setText("");

        Message message = db.createMessage(msg,
                HeyDudeSessionVariables.name,
                HeyDudeSessionVariables.dest.getName(),
                HeyDudeSessionVariables.image,
                new Date());
        msgs.add(message);
        adapter.notifyDataSetChanged();
    }

    public void receivedMessage(String msg) {
        Message message = db.createMessage(msg,
                HeyDudeSessionVariables.dest.getName(),
                HeyDudeSessionVariables.name,
                HeyDudeSessionVariables.dest.getImage(),
                new Date());
        msgs.add(message);
        adapter.notifyDataSetChanged();
    }
}
