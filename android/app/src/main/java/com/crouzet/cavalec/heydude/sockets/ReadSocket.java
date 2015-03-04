package com.crouzet.cavalec.heydude.sockets;

import android.os.Handler;

import com.crouzet.cavalec.heydude.interfaces.ReceiverCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Johan on 26/02/2015.
 */
public class ReadSocket {
    private int PORT = 4243;
    private String IP;

    private ServerSocket socket;
    private Handler updateConversationHandler;
    private Thread thread = null;

    private ReceiverCallback callback;

    public ReadSocket(String ip, boolean caller, ReceiverCallback callback) {
        IP = ip;
        PORT = caller ? 4243 : 4242;

        this.callback = callback;

        updateConversationHandler = new Handler();
        thread = new Thread(new ReadThread());
        thread.start();
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReadThread implements Runnable {

        @Override
        public void run() {
            Socket socket = null;

            try {
                ReadSocket.this.socket = new ServerSocket(PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = ReadSocket.this.socket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;

            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

            @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    //updateConversationHandler.post(new updateUIThread(read));
                    System.out.println(read);
                    callback.receivedMessage(read);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
