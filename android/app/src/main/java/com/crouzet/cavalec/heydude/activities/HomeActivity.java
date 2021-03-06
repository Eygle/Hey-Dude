package com.crouzet.cavalec.heydude.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crouzet.cavalec.heydude.HeyDudeConstants;
import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.adapters.UsersAdapter;
import com.crouzet.cavalec.heydude.gcm.GcmManager;
import com.crouzet.cavalec.heydude.http.ApiUtils;
import com.crouzet.cavalec.heydude.model.User;
import com.crouzet.cavalec.heydude.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * First activity launch
 * Allow user to connect via Google+
 * Display list of online users
 */
public class HomeActivity extends GooglePlusSigninActivity {
    // Online user list view's Adapter
    private UsersAdapter adapter;

    // Allow users to login in an asynchronous way
    private Handler handler;

    // Dialog used to display received calls
    private AlertDialog dialogCall;

    // Caller informations
    private User caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // Initialise Google Cloud Messaging
        new GcmManager(this);

        initialiseGooglePlus();
        initialiseOnlineUsersList();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register receivers
        registerReceiver(updateBroadcast, new IntentFilter(HeyDudeConstants.BROADCAST_REFRESH_USER_LIST));
        registerReceiver(receiveCall, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_CALL));
        registerReceiver(receiveHangup, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_HANGUP));

        // Send login to the server
        handler = new Handler();
        handler.post(login);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister receiver
        unregisterReceiver(updateBroadcast);
        unregisterReceiver(receiveCall);
        unregisterReceiver(receiveHangup);

        handler = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Logout
        if (HeyDudeSessionVariables.me != null && HeyDudeSessionVariables.token != null) {
            ApiUtils.logout();
        }
    }

    /**
     * Initialise and display online users
     */
    private void initialiseOnlineUsersList() {
        lvOnlineUsers = (ListView) findViewById(R.id.lv_contact);

        adapter = new UsersAdapter(this, HeyDudeSessionVariables.onlineUsers);
        lvOnlineUsers.setAdapter(adapter);

        final Context context = this;
        lvOnlineUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HeyDudeSessionVariables.dest = HeyDudeSessionVariables.onlineUsers.get(position);

                Intent intent = new Intent(context, ChatActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Asynchronous object used to try to login periodically
     * The login fail while the user is not connected to Google+
     */
    private Runnable login = new Runnable() {
        @Override
        public void run() {
            if (handler == null) return;
            if (HeyDudeSessionVariables.me != null && HeyDudeSessionVariables.token != null) {
                ApiUtils.login();
            }
            handler.postDelayed(login, 90000);
        }
    };

    /**
     * Broadcast received when online users data are updated from server
     */
    protected BroadcastReceiver updateBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();

            class UpdateData extends AsyncTask<JSONObject, Integer, Boolean> {

                protected Boolean doInBackground(JSONObject... data) {
                    return UserUtil.ManageDataFromOnlineUsers.update(data[0]);
                }

                protected void onPostExecute(Boolean result) {
                    if (result) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            try {
                new UpdateData().execute(new JSONObject(b.getString(HeyDudeConstants.BROADCAST_DATA_USER_LIST)));
            } catch (JSONException e) {
                e.printStackTrace();
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

            for (User user : HeyDudeSessionVariables.onlineUsers) {
                if (user.getId().equals(id)) {
                    caller = user;
                }
            }
            if (caller == null) return;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(String.format(context.getString(R.string.receive_call_title), caller.getName()))
                    .setMessage(String.format(context.getString(R.string.receive_call_message), caller.getName()))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            HeyDudeSessionVariables.dest = caller;

                            ApiUtils.answerCall(ApiUtils.ACCEPT_CALL, caller.getId());

                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("ACCEPT_CALL", true);
                            startActivity(intent);
                            dialogCall.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ApiUtils.answerCall(ApiUtils.REFUSE_CALL, caller.getId());
                            dialogCall.dismiss();
                        }
                    });
            // Create the AlertDialog object and show it
            dialogCall = builder.create();
            dialogCall.show();
        }
    };

    /**
     * Broadcast received when online users data are updated from server
     */
    protected BroadcastReceiver receiveHangup = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String id = b.getString(HeyDudeConstants.BROADCAST_DATA_DEST);

            if (caller == null || !caller.getId().equals(id) || dialogCall == null || !dialogCall.isShowing()) return;
            dialogCall.dismiss();
        }
    };
}
