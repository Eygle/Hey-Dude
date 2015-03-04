package com.crouzet.cavalec.heydude.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.activities.HomeActivity;
import com.crouzet.cavalec.heydude.utils.Crypto;

import java.security.KeyPair;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        Intent home = new Intent(this, HomeActivity.class);
        startActivity(home);*/

        Crypto.initCrypto();
        KeyPair keys = Crypto.generateKeys();

        String msg = "Bonjour les amis je m'appelle johan !";

        String enc = Crypto.encrypt(msg, keys.getPublic());
        String dec = Crypto.decrypt(enc, keys.getPrivate());

        TextView encrypted = (TextView)findViewById(R.id.encryptedText);
        TextView decrypted = (TextView)findViewById(R.id.decrytedText);

        if (enc != null) {
            encrypted.setText(enc);
        }
        if (dec != null) {
            decrypted.setText(Crypto.decrypt(enc, keys.getPrivate()));
        }
    }

    public void isCaller(View v) {
        HeyDudeSessionVariables.pseudo = "Eygle";
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("isCaller", true);
        intent.putExtra("destName", "Thomas");
        startActivity(intent);
    }

    public void isReceiver(View v) {
        HeyDudeSessionVariables.pseudo = "Thomas";
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("isCaller", false);
        intent.putExtra("destName", "Eygle");
        startActivity(intent);
    }
}
