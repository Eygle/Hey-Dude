package com.crouzet.cavalec.heydude.sockets;

import android.util.Log;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Johan on 26/02/2015.
 */
public class SendSocket {
    private static String TAG = SendSocket.class.getSimpleName();

    private Socket socket;
    private int PORT;
    private String IP;

    public SendSocket(String ip, boolean caller) {
        IP = ip;
        PORT = caller ? 4242 : 4243;

        Log.e(TAG,  HeyDudeSessionVariables.name + " send socket: " + IP + ":" + PORT);

        new Thread(new ClientThread()).start();
    }

    public void send(String str)
    {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        Log.d(TAG, "Close socket");
        try {
            socket.close();
        } catch (IOException|NullPointerException e) {
            e.printStackTrace();
        }
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(IP);
                socket = new Socket(serverAddr, PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
