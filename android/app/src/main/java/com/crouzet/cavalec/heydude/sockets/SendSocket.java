package com.crouzet.cavalec.heydude.sockets;

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
    private Socket socket;
    private int PORT;
    private String IP;

    public SendSocket(String ip, boolean caller) {
        IP = ip;
        PORT = caller ? 4242 : 4243;

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
