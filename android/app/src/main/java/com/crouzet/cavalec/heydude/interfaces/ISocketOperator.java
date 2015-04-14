package com.crouzet.cavalec.heydude.interfaces;


public interface ISocketOperator {
	public boolean sendMessage(String message, String ip, int port);
	public int startListening(int port);
	public void stopListening();
	public void exit();
	public int getListeningPort();

}
