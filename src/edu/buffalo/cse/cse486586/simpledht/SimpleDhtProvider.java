package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Map;
import java.util.TreeMap;

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
	private TreeMap<String, Object> nodeMap = new TreeMap<String, Object>();
	private DhtMessage joinedMessage;
	
	public boolean isLeader(){
		return isLeader;
	}
	
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
    	else{
    		try {
				nodeMap.put(genHash(port), port);
			} catch (NoSuchAlgorithmException e) {
				Log.v(SimpleDhtMainActivity.TAG, "Exception adding to nodemap.");
				e.printStackTrace();
			}
    	}
    	return false;
    }

	private void createServer() {
		try{
			ServerSocket serverSocket = new ServerSocket(10000);
			new ServerTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		}
		catch(IOException e){
			Log.v(SimpleDhtMainActivity.TAG, "Exception creating ServerSocket");
		}
		
	}

	private void broadCastJoin() {
    	Log.v(SimpleDhtMainActivity.TAG, "Need to join the network.");	
    	new DhtRequestJoinClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DhtMessage.getJoinMessage(port));
	}

	private void setLeaderStatus() {
		if(port.equals(Constants.AVD0_PORT)){
			Log.v(SimpleDhtMainActivity.TAG, "I am the leader.  Set my value to true.");
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

	public void processJoinRequest(DhtMessage dm) {
		try {

			Log.v(SimpleDhtMainActivity.TAG, "Creating join request responses");
			String hash = genHash(dm.getAvdOne());
			nodeMap.put(hash, dm.getAvdOne());
			
			String predecessorHash = nodeMap.lowerKey(hash);
			if(predecessorHash == null){
				predecessorHash = nodeMap.lastKey();
			}
			String predecessor = (String)nodeMap.get(predecessorHash);
			// If happens to be the first key, cycle to create the ring
			String successorHash = nodeMap.higherKey(hash);
			if(successorHash == null){
				successorHash = nodeMap.firstKey();
			}
			String successor = (String)nodeMap.get(successorHash);
			
			Log.v(SimpleDhtMainActivity.TAG, "Created a DhtJoinResponseClientTask");
			Log.v(SimpleDhtMainActivity.TAG, "Predecessor: " + predecessor);
			Log.v(SimpleDhtMainActivity.TAG, "InsertNode: " + dm.getAvdOne());
			Log.v(SimpleDhtMainActivity.TAG, "Successor: " + successor);
			DhtMessage one = DhtMessage.getJoinResponseMessage(predecessor, dm.getAvdOne(), successor, DhtMessage.RESPONSE_PREDECESSOR_JOIN);
			Log.v(SimpleDhtMainActivity.TAG, "one Predecessor: " + one.getAvdOne());
			Log.v(SimpleDhtMainActivity.TAG, "one InsertNode: " + one.getAvdTwo());
			Log.v(SimpleDhtMainActivity.TAG, "one Successor: " + one.getAvdThree());
			
			DhtMessage two = DhtMessage.getJoinResponseMessage(predecessor, dm.getAvdOne(), successor, DhtMessage.RESPONSE_JOIN);
			Log.v(SimpleDhtMainActivity.TAG, "two Predecessor: " + two.getAvdOne());
			Log.v(SimpleDhtMainActivity.TAG, "two InsertNode: " + two.getAvdTwo());
			Log.v(SimpleDhtMainActivity.TAG, "two Successor: " + two.getAvdThree());
			
			DhtMessage three = DhtMessage.getJoinResponseMessage(predecessor, dm.getAvdOne(), successor, DhtMessage.RESPONSE_SUCCESSOR_JOIN);			
			Log.v(SimpleDhtMainActivity.TAG, "three Predecessor: " + three.getAvdOne());
			Log.v(SimpleDhtMainActivity.TAG, "three InsertNode: " + three.getAvdTwo());
			Log.v(SimpleDhtMainActivity.TAG, "three Successor: " + three.getAvdThree());

			new DhtJoinResponseClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, one, two, three); 
		} catch (NoSuchAlgorithmException e) {
			Log.v(SimpleDhtMainActivity.TAG, "Error trying to place request in nodemap in processJoinRequest");
			e.printStackTrace();
		}
	}

	public void processJoinResponse(DhtMessage dm) {
		joinedMessage = dm;
	}

	public void processJoinSuccessorResponse(DhtMessage dm) {
		//Give the node a new predecessor
		joinedMessage.setAvd(dm.getAvdTwo(), DhtMessage.AVD_INSERT_PT_ONE);
	}

	public void processJoinPredecessorResponse(DhtMessage dm) {
		// Give the node a new successor
		joinedMessage.setAvd(dm.getAvdTwo(), DhtMessage.AVD_INSERT_PT_THREE);		
	}    
}