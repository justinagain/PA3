package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {

	private String port;
	private boolean isLeader = false;
	private String predecessor;
	private String successor;
	private ArrayList<String> ring = new ArrayList<String>();
	
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
    	//test
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean onCreate() {
    	setPortNumber();
		setLeaderStatus();
    	Log.v(SimpleDhtMainActivity.TAG, "Set the port number and leader status.");
    	createServer();
    	if(! isLeader){
    		broadCastJoin();
    	}
    	return false;
    }

	private void createServer() {
		try{
			ServerSocket serverSocket = new ServerSocket(10000);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		}
		catch(IOException e){
			Log.v(SimpleDhtMainActivity.TAG, "Exception creating ServerSocket");
		}
		
	}

	private void broadCastJoin() {
    	Log.v(SimpleDhtMainActivity.TAG, "Need to join the network.");		
	}

	private void setLeaderStatus() {
		if(port.equals(Constants.AVD0_PORT)){
			isLeader = true;
		}
	}

	private void setPortNumber() {
		Application application = (Application)getContext();
		TelephonyManager tel = (TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
		port = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
	}

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
