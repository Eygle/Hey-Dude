package com.crouzet.cavalec.heydude.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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
import com.crouzet.cavalec.heydude.model.User;
import com.crouzet.cavalec.heydude.services.BackgroundServiceCheckIfUserCallMe;
import com.crouzet.cavalec.heydude.services.BackgroundServiceUpdateOnlineUsers;
import com.crouzet.cavalec.heydude.utils.ApiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


public class HomeActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static Intent fgBackgroundServiceUpdateOnlineUsers;
    private static Intent fgBackgroundServiceCheckCalls;

    ListView lvOnlineUsers;
    UsersAdapter adapter;

    // Google Plus
    private static final int RC_SIGN_IN = 0;
    // Logcat tag
    private static final String TAG = "MainActivity";

    // Profile pic image size in pixels
    private static final int PROFILE_PIC_SIZE = 200;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    /**
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;

    private SignInButton btnSignIn;

    /**
     * Broadcast received when online users data are updated from server
     */
    protected BroadcastReceiver updateBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.notifyDataSetChanged();
        }
    };

    /**
     * Broadcast received when online users data are updated from server
     */
    protected BroadcastReceiver receiveCall = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Bundle b = intent.getExtras();
            final User u = (User)b.getSerializable("caller");

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(String.format(context.getString(R.string.receive_call_title), u.getName()))
                    .setMessage(String.format(context.getString(R.string.receive_call_message), u.getName()))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            HeyDudeSessionVariables.dest = u;

                            Intent intent = new Intent(context, ChatActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.no, null);
            // Create the AlertDialog object and show it
            builder.create().show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (fgBackgroundServiceUpdateOnlineUsers == null) {
            fgBackgroundServiceUpdateOnlineUsers = new Intent(this, BackgroundServiceUpdateOnlineUsers.class);
        }
        if (fgBackgroundServiceCheckCalls == null) {
            fgBackgroundServiceCheckCalls = new Intent(this, BackgroundServiceCheckIfUserCallMe.class);
        }

        initialiseOnlineUsersList();

        initialiseGooglePlus();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(updateBroadcast, new IntentFilter(HeyDudeConstants.BROADCAST_REFRESH_LIST));
        registerReceiver(receiveCall, new IntentFilter(HeyDudeConstants.BROADCAST_RECEIVE_CALL));

        if (fgBackgroundServiceUpdateOnlineUsers != null && !BackgroundServiceUpdateOnlineUsers.mRunning) {
            startService(fgBackgroundServiceUpdateOnlineUsers);
        }
        if (fgBackgroundServiceCheckCalls != null && !BackgroundServiceCheckIfUserCallMe.mRunning) {
            startService(fgBackgroundServiceCheckCalls);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(updateBroadcast);
        unregisterReceiver(receiveCall);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (fgBackgroundServiceUpdateOnlineUsers != null) {
            stopService(fgBackgroundServiceUpdateOnlineUsers);
        }

        if (fgBackgroundServiceCheckCalls != null) {
            stopService(fgBackgroundServiceCheckCalls);
        }

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
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
                signOutFromGplus();
                break;
            // action with ID action_settings was selected
            case R.id.action_revoke:
                revokeGplusAccess();
                break;
            default:
                break;
        }

        return true;
    }

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

    // Google+ name

    private void initialiseGooglePlus() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();

        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGplus();
            }
        });
    }

    /**
     * Sign-in into google
     * */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Sign-out from google
     * */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateUI(false);
        }
    }

    /**
     * Revoking access from google
     * */
    private void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(TAG, "User access revoked!");
                            mGoogleApiClient.connect();
                            updateUI(false);
                        }

                    });
        }
    }


    /**
     * Method to resolve any signin errors
     * */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult for later usage
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;

        // Get user's information
        getProfileInformation();

        // Send the informations to the server so it can says we are online
        ApiUtils.connect();

        /*BitmapDrawable img = Utils.createBitmapFromURL(HeyDudeSessionVariables.image);
        if (img != null) {
            try {
                getSupportActionBar().setIcon(img);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }*/

        // Update the UI after signin
        updateUI(true);
    }


    /**
     * Updating the UI, showing/hiding buttons and profile layout
     * */
    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
            lvOnlineUsers.setVisibility(View.VISIBLE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            lvOnlineUsers.setVisibility(View.GONE);
        }
    }

    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);

                //String personGooglePlusProfile = currentPerson.getUrl();

                String personPhotoUrl = currentPerson.getImage().getUrl();
                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + PROFILE_PIC_SIZE;

                HeyDudeSessionVariables.id = currentPerson.getId();
                HeyDudeSessionVariables.name = currentPerson.getDisplayName();
                HeyDudeSessionVariables.image = personPhotoUrl;
                HeyDudeSessionVariables.email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
        updateUI(false);
    }
}
