package com.crouzet.cavalec.heydude.sockets;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crouzet.cavalec.heydude.HeyDudeConstants;
import com.crouzet.cavalec.heydude.interfaces.ISocketOperator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

public class SocketOperator implements ISocketOperator
{
    private final static String TAG = SocketOperator.class.getSimpleName();

	private int listeningPort = 0;
	
	private HashMap<InetAddress, Socket> sockets = new HashMap<>();

    private boolean listening;

    private Context context;

    public SocketOperator(Context c) {
        context = c;
    }
	
	private class ReceiveConnection extends Thread {
		Socket clientSocket = null;
		public ReceiveConnection(Socket socket) 
		{
            Log.d(TAG, "Accept connection socket");
			this.clientSocket = socket;
			SocketOperator.this.sockets.put(socket.getInetAddress(), socket);
		}
		
		@Override
		public void run() {
			 try {
	//			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(
						    new InputStreamReader(
						    		clientSocket.getInputStream()));
				String inputLine;
				
				 while ((inputLine = in.readLine()) != null) 
				 {
                     Log.d(TAG, "Receive something");
					 if (!inputLine.equals("exit"))
					 {
                         Log.d(TAG, "Received message: " + inputLine);
                         // TODO Broadcast message received
                         Intent i = new Intent(HeyDudeConstants.BROADCAST_RECEIVE_MSG);

                         i.putExtra("message", inputLine);
                         context.sendBroadcast(i);
						 //appManager.messageReceived(inputLine);
					 }
					 else
					 {
						 clientSocket.shutdownInput();
						 clientSocket.shutdownOutput();
						 clientSocket.close();
						 SocketOperator.this.sockets.remove(clientSocket.getInetAddress());
					 }						 
				 }		
				
			} catch (IOException e) {
				//Log.e("ReceiveConnection.run: when receiving connection ","");
			}			
		}	
	}


	public boolean sendMessage(String message, String ip, int port) 
	{
		try {
			Log.d(TAG, "Send message to " + ip + ":" + port);
			
			String[] str = ip.split("\\.");
			
			byte[] IP = new byte[str.length];
			
			for (int i = 0; i < str.length; i++) {
				
				IP[i] = (byte) Integer.parseInt(str[i]);				
			}
			Socket socket = getSocket(InetAddress.getByAddress(IP), port);
			if (socket == null) {
                Log.d(TAG, "Socket is null !");
				return false;
			}
		
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			
			out.println(message);
		} catch (UnknownHostException e) {
            e.printStackTrace();
			return false;
		} catch (IOException e) {
            e.printStackTrace();
			return false;
		}
		
		return true;		
	}



	public int startListening(int portNo) 
	{
		listening = true;

        ServerSocket serverSocket;
        try {
			serverSocket = new ServerSocket(portNo);
			this.listeningPort = portNo;
		} catch (IOException e) {			
			
			//e.printStackTrace();
			this.listeningPort = 0;
			return 0;
		}

		while (listening) {
			try {
				new ReceiveConnection(serverSocket.accept()).start();
				
			} catch (IOException e) {
				e.printStackTrace();
				return 2;
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {			
			Log.e("Exception server socket", "Exception when closing server socket");
            e.printStackTrace();
			return 3;
		}
		return 1;
	}
	
	
	public void stopListening() 
	{
		this.listening = false;
	}
	
	private Socket getSocket(InetAddress addr, int portNo) 
	{
        Log.d(TAG, "Try get socket with ip: " + addr.getAddress().toString());
		Socket socket = null;
		if (sockets.containsKey(addr))
		{
			socket = sockets.get(addr);
			// check the status of the socket
			if  (!socket.isConnected() ||
				  socket.isInputShutdown() ||
				  socket.isOutputShutdown() ||
				  socket.getPort() != portNo)
			{			
				// if socket is not suitable,  then create a new socket
				sockets.remove(addr);				
				try {
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
					socket = new Socket(addr, portNo);
					sockets.put(addr, socket);
				} 
				catch (IOException e) {
                    e.printStackTrace();
					//Log.e("getSocket: when closing and removing", "");
				}				
			}
		}
		else  
		{
			try {
				socket = new Socket(addr, portNo);
				sockets.put(addr, socket);
			} catch (IOException e) {
                e.printStackTrace();
				//Log.e("getSocket: when creating", "");
			}					
		}
		return socket;		
	}


	public void exit() 
	{			
		for (Iterator<Socket> iterator = sockets.values().iterator(); iterator.hasNext();) 
		{
			Socket socket = iterator.next();
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			} catch (IOException e) 
			{
                e.printStackTrace();
			}		
		}
		
		sockets.clear();
		this.stopListening();
//		timer.cancel();		
	}


	public int getListeningPort() {
		
		return this.listeningPort;
	}	

}
