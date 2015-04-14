package com.crouzet.cavalec.heydude.services;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.interfaces.IAppManager;
import com.crouzet.cavalec.heydude.interfaces.ISocketOperator;
import com.crouzet.cavalec.heydude.model.User;
import com.crouzet.cavalec.heydude.sockets.SocketOperator;

import java.net.URLEncoder;
import java.util.Random;
import java.util.Timer;

/**
 * Created by Johan on 14/04/2015.
 */
public class BackgroundServiceHandleSockets extends Service implements IAppManager {
    private final static String TAG = BackgroundServiceHandleSockets.class.getSimpleName();

    private ISocketOperator socketOperator = new SocketOperator(this);

    private Timer timer;

    private final IBinder mBinder = new IMBinder();

    public ConnectivityManager conManager;

    public class IMBinder extends Binder {
        public IAppManager getService() {
            return BackgroundServiceHandleSockets.this;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Create service");
        conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        // Timer is used to take the friendList info every UPDATE_TIME_PERIOD;
        timer = new Timer();

        Thread thread = new Thread()
        {
            @Override
            public void run() {

                //socketOperator.startListening(LISTENING_PORT_NO);
                Random random = new Random();
                int tryCount = 0;
                while (socketOperator.startListening(10000 + random.nextInt(20000)) == 0)
                {
                    tryCount++;
                    if (tryCount > 10)
                    {
                        // if it can't listen a port after trying 10 times, give up...
                        break;
                    }

                }
            }
        };
        thread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean sendMessage(String message) {
        User friend = HeyDudeSessionVariables.dest;

        String IP = friend.getIP();
        //IP = "10.0.2.2";
        int port = friend.getPort();

        String msg = "EMAIL=" + URLEncoder.encode(friend.getEmail()) +
                "&MESSAGE=" + URLEncoder.encode(message);

        return socketOperator.sendMessage(msg, IP,  port);
    }

    public boolean isNetworkConnected() {
        return conManager.getActiveNetworkInfo().isConnected();
    }

    public int getListeningPort() {
        return socketOperator == null ? 0 : socketOperator.getListeningPort();
    }

    public void exit()
    {
        timer.cancel();
        socketOperator.exit();
        socketOperator = null;
        this.stopSelf();
    }
}
