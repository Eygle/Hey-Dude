package com.crouzet.cavalec.heydude.interfaces;


public interface IAppManager {
	public boolean sendMessage(String message);
    public boolean isNetworkConnected();
    public int getListeningPort();
	public void exit();
}
