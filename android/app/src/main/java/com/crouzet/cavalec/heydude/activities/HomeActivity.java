package com.crouzet.cavalec.heydude.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.crouzet.cavalec.heydude.HeyDudeConstants;
import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.adapters.UsersAdapter;
import com.crouzet.cavalec.heydude.http.ApiUtils;
import com.crouzet.cavalec.heydude.interfaces.IAppManager;
import com.crouzet.cavalec.heydude.model.User;
import com.crouzet.cavalec.heydude.services.BackgroundServiceCheckIfUserCallMe;
import com.crouzet.cavalec.heydude.services.BackgroundServiceHandleSockets;
import com.crouzet.cavalec.heydude.services.BackgroundServiceUpdateOnlineUsers;
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

    private View mProgressView;
    MenuItem actionLogout, actionRevoke;

    private IAppManager imService;

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

                            ApiUtils.answerCall(ApiUtils.ACCEPT_CALL, u.getId());

                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("ACCEPT_CALL", true);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ApiUtils.answerCall(ApiUtils.REFUSE_CALL, u.getId());
                        }
                    });
            // Create the AlertDialog object and show it
            builder.create().show();
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            Log.d(TAG, "Socket service connected");
            imService = ((BackgroundServiceHandleSockets.IMBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Log.d(TAG, "Socket service disconnected");
            imService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, BackgroundServiceHandleSockets.class));

        setContentView(R.layout.activity_home);

        if (fgBackgroundServiceUpdateOnlineUsers == null) {
            fgBackgroundServiceUpdateOnlineUsers = new Intent(this, BackgroundServiceUpdateOnlineUsers.class);
        }
        if (fgBackgroundServiceCheckCalls == null) {
            fgBackgroundServiceCheckCalls = new Intent(this, BackgroundServiceCheckIfUserCallMe.class);
        }

        mProgressView = findViewById(R.id.login_progress);

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

        bindService(new Intent(this, BackgroundServiceHandleSockets.class), mConnection , Context.BIND_AUTO_CREATE);

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
        unbindService(mConnection);
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

        actionLogout = menu.findItem(R.id.action_logout);
        actionRevoke = menu.findItem(R.id.action_revoke);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_logout:
                ApiUtils.logout();
                signOutFromGplus();
                break;
            // action with ID action_settings was selected
            case R.id.action_revoke:
                ApiUtils.deleteAccount();
                revokeGplusAccess();
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        btnSignIn.setVisibility(show ? View.GONE : View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);

            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
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
        showProgress(true);

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
        showProgress(false);

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

        showProgress(false);

        // Get user's information
        getProfileInformation();

        handler.post(tryLogin);

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
            actionLogout.setVisible(true);
            actionRevoke.setVisible(true);

        } else {
            btnSignIn.setVisibility(View.VISIBLE);
            lvOnlineUsers.setVisibility(View.GONE);
            actionLogout.setVisible(false);
            actionRevoke.setVisible(false);
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
        showProgress(false);
        updateUI(false);
    }

    private Handler handler = new Handler();
    private Runnable tryLogin = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Check if port is listened");
            if (imService != null && imService.getListeningPort() != 0) {
                Log.d(TAG, "Login");
                HeyDudeSessionVariables.port = imService.getListeningPort();
                ApiUtils.login();
            } else {
                handler.postDelayed(this, 500);
            }
        }
    };
}
