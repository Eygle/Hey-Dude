package com.crouzet.cavalec.heydude.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.adapters.ChatAdapter;
import com.crouzet.cavalec.heydude.interfaces.ReceiverCallback;
import com.crouzet.cavalec.heydude.model.Message;
import com.crouzet.cavalec.heydude.sockets.ReadSocket;
import com.crouzet.cavalec.heydude.sockets.SendSocket;
import com.crouzet.cavalec.heydude.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends ActionBarActivity implements ReceiverCallback {

    //private ReadSocket readSocket;
    //private SendSocket sendSocket;

    private EditText message;

    private ListView listView;
    private List<Message> msgs = new ArrayList<>();
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        try {
            ActionBar bar = getSupportActionBar();
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            Drawable img = Utils.createBitmapFromURL(HeyDudeSessionVariables.dest.getImage());
            if (img != null) {
                bar.setIcon(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        msgs.add(new Message("Coucou", "Thomas", "Johan", new Date()));
        adapter = new ChatAdapter(this, msgs);

        listView = (ListView)findViewById(R.id.chat_listview);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //sendSocket.closeSocket();
        //readSocket.closeSocket();
    }

    public void sendMessage(View v) {
        String msg = message.getText().toString();
        //sendSocket.send(msg);
        message.setText("");
        msgs.add(new Message(msg, HeyDudeSessionVariables.dest.getName(), HeyDudeSessionVariables.name));
        adapter.notifyDataSetChanged();
    }

    public void receivedMessage(String msg) {
        msgs.add(new Message(msg, HeyDudeSessionVariables.name, HeyDudeSessionVariables.dest.getName()));
        adapter.notifyDataSetChanged();
    }
}
