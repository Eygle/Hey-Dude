package com.crouzet.cavalec.heydude.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crouzet.cavalec.heydude.HeyDudeConstants;
import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.adapters.UsersAdapter;
import com.crouzet.cavalec.heydude.services.BackgroundServiceUpdateOnlineUsers;


public class HomeActivity extends ActionBarActivity {
    private static Intent fgBackgroundServiceUpdateOnlineUsers;

    ListView lv;
    UsersAdapter adapter;

    /**
     * Broadcast received when programs data are updated from server
     */
    protected BroadcastReceiver updateBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.notifyDataSetChanged();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (fgBackgroundServiceUpdateOnlineUsers == null) {
            fgBackgroundServiceUpdateOnlineUsers = new Intent(this, BackgroundServiceUpdateOnlineUsers.class);
        }

        lv = (ListView) findViewById(R.id.lv_contact);

        adapter = new UsersAdapter(this, HeyDudeSessionVariables.onlineUsers);
        lv.setAdapter(adapter);

        final Context context = this;
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HeyDudeSessionVariables.dest = HeyDudeSessionVariables.onlineUsers.get(position);

                Intent intent = new Intent(context, ChatActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(updateBroadcast, new IntentFilter(HeyDudeConstants.BROADCAST_REFRESH_LIST));

        if (fgBackgroundServiceUpdateOnlineUsers != null && !BackgroundServiceUpdateOnlineUsers.mRunning) {
            startService(fgBackgroundServiceUpdateOnlineUsers);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(updateBroadcast);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fgBackgroundServiceUpdateOnlineUsers != null) {
            stopService(fgBackgroundServiceUpdateOnlineUsers);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_logout:
                // TODO
                break;
            // action with ID action_settings was selected
            case R.id.action_revoke:
                // TODO
                break;
            default:
                break;
        }

        return true;
    }
}
