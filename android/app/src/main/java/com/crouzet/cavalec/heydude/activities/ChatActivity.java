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
import android.util.Base64;
import android.util.Log;
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
import com.crouzet.cavalec.heydude.utils.CryptoAES;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

/**
 * Activity used to chat with people
 */
public class ChatActivity extends ActionBarActivity {
    // Delay in milliseconds after which the call will be end if the user calld doesn't answer
    private final int TIMEOUT_DELAY = 30000;

    // Crypto algo initialised with symmetric key
    private CryptoAES cryptoAES;
    private byte[] key;

    private MenuItem callIcon;
    private EditText editTextMsg;

    // Printed messages list and adapter
    private ListView listView;
    private List<Message> msgs;
    private ChatAdapter adapter;

    // Database containing all messages
    MessagesDataSource db = new MessagesDataSource(this);

    // Dialogs
    private ProgressDialog loader = null;
    private AlertDialog dialogCall = null;

    // Handler used for call timeout
    private Handler handler = new Handler();

    // Set to true if call is in progress (used to display call icon)
    private boolean callInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialise title bar icons and title
        try {
            ActionBar bar = getSupportActionBar();
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle(HeyDudeSessionVariables.dest.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Called when the user accepted a call from HomeView
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("ACCEPT_CALL")) {
            showLoader();
            loader.setMessage(getString(R.string.call_secure_loader_message));
        }

        // Edit text used by user to write messages
        editTextMsg = (EditText)findViewById(R.id.chat_message);

        // Open database for having old messages
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

        // Register receivers for broadcast messages
        registerReceiver(keyReceiver, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_KEY));
        registerReceiver(messageReceiver, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_MSG));
        registerReceiver(callAnswerReceiver, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_CALL_ANSWER));
        registerReceiver(callReceiver, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_CALL));
        registerReceiver(hangupReceiver, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_HANGUP));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister receivers
        unregisterReceiver(keyReceiver);
        unregisterReceiver(messageReceiver);
        unregisterReceiver(callAnswerReceiver);
        unregisterReceiver(callReceiver);
        unregisterReceiver(hangupReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Close database
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);

        callIcon = menu.findItem(R.id.action_call);

        // Set the icon depending of whether or not a call is in progress
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

    /**
     * Initialise and display the list of messages (including from previous sessions)
     */
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

                // Handle messages options (delete, copy)
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

    /**
     * Call a user
     */
    private void call() {
        try {
            // Generate symmetric key for the session and store it
            key = cryptoAES.generateRandomBytes(32);
            // Initialise crypto AES algo with the generated key
            cryptoAES = new CryptoAES(key);
        } catch (NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        showLoader();
        ApiUtils.call();

        // Check for call timeout after TIMEOUT_DELAY
        handler.postDelayed(callTimeout, TIMEOUT_DELAY);
    }

    /**
     * Hangup the conversation
     */
    private void hangup() {
        callInProgress = false;
        ApiUtils.hangup();

        callIcon.setIcon(R.drawable.call);
        findViewById(R.id.chat_form).setVisibility(View.GONE);
    }

    /**
     * Display loader when call in a waiting state
     */
    private void showLoader() {
        if (loader == null) {
            loader = new ProgressDialog(ChatActivity.this);
            loader.setTitle(R.string.call_loader_title);
            loader.setCancelable(false);
            loader.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    hangup();
                    hideLoader();
                }
            });
        }
        loader.setMessage(String.format(getString(R.string.call_loader_message), HeyDudeSessionVariables.dest.getName()));
        loader.show();
    }

    /**
     * Hide loader
     */
    private void hideLoader() {
        if (loader != null && loader.isShowing()) {
            loader.dismiss();
        }
    }

    /**
     * Called when the called have been accepted
     * @param pubK asymmetric public key of receiver
     */
    private void callAccepted(String pubK) {
        ApiUtils.sendKey(key); // Todo encrypt key with pubK

        initCall();
    }

    /**
     * Initialise call
     * Remove call timeout
     * Hide loader
     * Change call icon
     */
    private void initCall() {
        handler.removeCallbacks(callTimeout);

        callInProgress = true;
        hideLoader();
        findViewById(R.id.chat_form).setVisibility(View.VISIBLE);

        if (callIcon != null)
            callIcon.setIcon(R.drawable.hangup);
    }

    /**
     * Called when the call have been refused by receiver
     * Display a dialog
     */
    private void callRefused() {
        hideLoader();

        handler.removeCallbacks(callTimeout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.dialog_call_refused), HeyDudeSessionVariables.dest.getName()))
                .setNeutralButton(R.string.ok, null);
        builder.create().show();
    }

    /**
     * Asynchronous object used to check for call timeout
     */
    private Runnable callTimeout = new Runnable() {
        public void run() {
            hangup();
            hideLoader();

            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setMessage(String.format(getString(R.string.dialog_call_timeout), HeyDudeSessionVariables.dest.getName()))
                    .setNeutralButton(R.string.ok, null);
            builder.create().show();
        }
    };

    /**
     * Get the message from EditText and send it to receiver
     * Before being send th message is encrypted with symmetric key using AES
     * The messages are displayed and store in database
     */
    public void sendMessage() {
        final String msg = editTextMsg.getText().toString();

        if (msg.length() == 0) {
            Toast.makeText(this, R.string.message_empty_error, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ApiUtils.sendMessage(cryptoAES.encrypt(msg), cryptoAES.getIV());
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | ShortBufferException | InvalidKeyException e) {
            e.printStackTrace();
        }

        editTextMsg.setText("");

        Message message = db.createMessage(msg,
                HeyDudeSessionVariables.me.getName(),
                HeyDudeSessionVariables.dest.getName(),
                HeyDudeSessionVariables.me.getImage(),
                new Date());
        msgs.add(message);
        adapter.notifyDataSetChanged();
    }

    /**
     * Called when receiving the symmetric RSA key encrypted with asymmetric RSA public key
     * @param k Base 64 encrypted key sended by corespondent
     */
    public void receivedKey(String k) {
        // Get AES by decrypting it with RSA and private key
        key = Base64.decode(k, Base64.DEFAULT); // TODO decrypt key with private key (tout le base64)

        try {
            cryptoAES = new CryptoAES(key);
        } catch (NoSuchPaddingException|NoSuchAlgorithmException|NoSuchProviderException e) {
            e.printStackTrace();
        }

        callInProgress = true;
        hideLoader();
        findViewById(R.id.chat_form).setVisibility(View.VISIBLE);

        if (callIcon != null)
            callIcon.setIcon(R.drawable.hangup);
    }

    /**
     * Called when a message is received
     * @param m message encrypted with AES and Base64
     * @param iv Initiation Vector encrypted in Base64
     */
    public void receivedMessage(String m, String iv) {
        String msg = null;
        try {
            msg = cryptoAES.decrypt(Base64.decode(m, Base64.DEFAULT), Base64.decode(iv, Base64.DEFAULT));
            Message message = db.createMessage(msg,
                    HeyDudeSessionVariables.dest.getName(),
                    HeyDudeSessionVariables.me.getName(),
                    HeyDudeSessionVariables.dest.getImage(),
                    new Date());
            msgs.add(message);
            adapter.notifyDataSetChanged();
        } catch (InvalidKeyException | ShortBufferException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcast received when online users data are updated from server
     */
    protected BroadcastReceiver keyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle b = intent.getExtras();
            receivedKey(b.getString(HeyDudeConstants.BROADCAST_DATA_KEY));
        }
    };

    /**
     * Broadcast received when online users data are updated from server
     */
    protected BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle b = intent.getExtras();
            receivedMessage(b.getString(HeyDudeConstants.BROADCAST_DATA_MSG), b.getString(HeyDudeConstants.BROADCAST_DATA_IV));
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
                    callAccepted(b.getString(HeyDudeConstants.BROADCAST_DATA_KEY));
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
    protected BroadcastReceiver callReceiver = new BroadcastReceiver() {
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
                            showLoader();
                            loader.setMessage(getString(R.string.call_secure_loader_message));

                            if (finalCaller.getId().equals(HeyDudeSessionVariables.dest.getId())) {
                                initCall();
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
            dialogCall = builder.create();
            dialogCall.show();
        }
    };

    /**
     * Broadcast received user receive a hangup
     */
    protected BroadcastReceiver hangupReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String id = b.getString(HeyDudeConstants.BROADCAST_DATA_DEST);

            if (HeyDudeSessionVariables.dest.getId().equals(id)) {
                if (dialogCall != null && dialogCall.isShowing()) {
                    dialogCall.dismiss();
                } else if (callInProgress) {
                    callInProgress = false;

                    callIcon.setIcon(R.drawable.call);
                    findViewById(R.id.chat_form).setVisibility(View.GONE);
                    Toast.makeText(ChatActivity.this,
                            String.format(getString(R.string.user_has_hangup), HeyDudeSessionVariables.dest.getName()),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
}
