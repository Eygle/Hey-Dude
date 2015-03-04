package com.crouzet.cavalec.heydude.activities;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.adapters.ChatAdapter;
import com.crouzet.cavalec.heydude.interfaces.ReceiverCallback;
import com.crouzet.cavalec.heydude.model.Chat;
import com.crouzet.cavalec.heydude.model.Message;
import com.crouzet.cavalec.heydude.sockets.ReadSocket;
import com.crouzet.cavalec.heydude.sockets.SendSocket;
import com.crouzet.cavalec.heydude.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends Activity implements ReceiverCallback {

    private boolean isCaller;
    private String destName;

    private ReadSocket readSocket;
    private SendSocket sendSocket;

    private EditText message;

    private ListView listView;
    private List<Message> msgs = new ArrayList<>();
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle extras = getIntent().getExtras();
        isCaller = extras.getBoolean("isCaller");
        destName = extras.getString("destName");

        readSocket = new ReadSocket("192.168.0.1", isCaller, this);
        sendSocket = new SendSocket("192.168.0.1", isCaller);

        message = (EditText)findViewById(R.id.chat_message);

        msgs.add(new Message("Coucou", "Thomas", "Johan", new Date()));
        adapter = new ChatAdapter(this, msgs);

        listView = (ListView)findViewById(R.id.chat_listview);
        listView.setAdapter(adapter);

        Toast.makeText(this, Utils.getIP(this), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        sendSocket.closeSocket();
        readSocket.closeSocket();
    }

    public void sendMessage(View v) {
        String msg = message.getText().toString();
        sendSocket.send(msg);
        message.setText("");
        msgs.add(new Message(msg, destName, HeyDudeSessionVariables.pseudo));
        adapter.notifyDataSetChanged();
    }

    public void receivedMessage(String msg) {
        msgs.add(new Message(msg, HeyDudeSessionVariables.pseudo, destName));
        adapter.notifyDataSetChanged();
    }
}
