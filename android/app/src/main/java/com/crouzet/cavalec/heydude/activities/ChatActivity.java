package com.crouzet.cavalec.heydude.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import com.crouzet.cavalec.heydude.utils.Utils;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ChatActivity extends ActionBarActivity implements ReceiverCallback {

    //private ReadSocket readSocket;
    //private SendSocket sendSocket;

    private EditText editTextMsg;

    private ListView listView;
    private List<Message> msgs;
    private ChatAdapter adapter;

    MessagesDataSource db = new MessagesDataSource(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            ActionBar bar = getSupportActionBar();
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle(HeyDudeSessionVariables.dest.getName());
            Drawable img = Utils.createBitmapFromURL(HeyDudeSessionVariables.dest.getImage());
            if (img != null) {
                bar.setIcon(img);
            }
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
    public boolean onOptionsItemSelected (MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                this.finish();
                break;

        }

        return true;
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
