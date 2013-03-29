package edu.buffalo.cse.cse486586.simpledht;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDhtProvider extends ContentProvider {

	public static final String TAG = SimpleDhtProvider.class.getName();
	private String port;
	private boolean isLeader = false;
	private TreeMap<String, Object> nodeMap = new TreeMap<String, Object>();
	private TreeMap<String, Object> keyValueMap = new TreeMap<String, Object>();
	private DhtMessage joinedMessage = DhtMessage.getDefaultMessage();
	private Uri selectAllUri;

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	
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
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public Uri insert(Uri groupMessengerUri, ContentValues contentValues) {
		Log.v(TAG, "About to insert into content provider with URI: " + groupMessengerUri.toString());
        writeToInternalStorage(groupMessengerUri, contentValues);
        getContext().getContentResolver().notifyChange(groupMessengerUri, null);
		return groupMessengerUri;
    }
    
	private boolean writeToInternalStorage(Uri uri, ContentValues contentValues){
		boolean success = false;
		FileOutputStream fos;
		try {
			Log.v(TAG, "About to insert into a speicific file");
			keyValueMap.clear();
			String keyValue = contentValues.get(OnTestClickListener.KEY_FIELD).toString();
			String contentValue = contentValues.get(OnTestClickListener.VALUE_FIELD).toString();

			Log.v(TAG, "keyvalue is: " + keyValue);
			String keyHash = genHash(keyValue);
			keyValueMap.put(keyHash, keyValue);
			String predecessor = joinedMessage.getAvdOne();
			String predecessorHash = genHash(predecessor);
			keyValueMap.put(predecessorHash, predecessor);
			String currentNode = joinedMessage.getAvdTwo();
			String currentNodeHash = genHash(currentNode);
			keyValueMap.put(currentNodeHash, currentNode);
			String successor = joinedMessage.getAvdThree();
			String successorHash = genHash(successor);
			keyValueMap.put(successorHash, successor);
			
			
			String sHash = keyValueMap.higherKey(keyHash);
			if(sHash == null){
				sHash = keyValueMap.firstKey();
			}
			
			if(joinedMessage == null){
				Log.v(TAG, "Serious problem.  Attempting to add to content provider when not a part of the ring ... ");				
			}
			else if(sHash.compareTo(predecessorHash) == 0  && sHash.compareTo(currentNodeHash) != 0){
				Log.v(TAG, "I have a message that must be sent to the predecessor node " + predecessor);
			}
			else if(sHash.compareTo(successorHash) == 0   && sHash.compareTo(currentNodeHash) != 0){
				Log.v(TAG, "I have a message that must be sent to the successor node " + successor);				
			}
			else{
				Log.v(TAG, "I have a message that belongs to the current node " + currentNode);				
				String fileName = uri.toString().replace("content://", "");
				fileName = fileName + "_" + keyValue;
				Log.v(TAG, "filename is: " + fileName);
				fos = this.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
				fos.write(contentValue.getBytes());				
				fos.close();
				success = true;
				Log.v(TAG, "Wrote ContentValues successfully.");				
			}
		} catch (FileNotFoundException e) {
			Log.v(TAG, "File not found when writing ContentValues");
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "Some IO Exception when writing ContentValues");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			Log.v(TAG, "Some genHash exception");
			e.printStackTrace();
		}
		return success;
	}


    @Override
    public boolean onCreate() {
    	selectAllUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider-ALL");
    	cleanProvider();
    	setPortNumber();
		setLeaderStatus();
    	Log.v(SimpleDhtMainActivity.TAG, "Set the port number and leader status.");
    	createServer();
    	broadCastJoin();
    	return false;
    }

	private void cleanProvider() {
		File[] files = this.getContext().getFilesDir().listFiles();
		String fileName = selectAllUri.toString();
		fileName = fileName.replace("content://", "");
		fileName = fileName.replace("-ALL", "");
		for (File file : files) {
			Log.v(TAG, "Base file is: " + fileName + " and compare name is: " + file.getName());
			if(file.getName().startsWith(fileName)){
				Log.v(TAG, "We have a match and must delete - it is old content.");
				file.delete();				
			}
		}
		
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
    public Cursor query(Uri providedUri, String[] arg1, String keyValue, String[] arg3,
			String arg4) {
		MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
		if(providedUri.toString().endsWith("-ALL") || keyValue.equals("ALL")){
			File[] files = this.getContext().getFilesDir().listFiles();
			String fileName = selectAllUri.toString();
			fileName = fileName.replace("content://", "");
			fileName = fileName.replace("-ALL", "");
			for (File file : files) {
				Log.v(TAG, "Base file is: " + fileName + " and compare name is: " + file.getName());
				if(file.getName().startsWith(fileName)){
					Log.v(TAG, "We have a match and must delete - it is old content.");
					try {
						FileInputStream fis = this.getContext().openFileInput(file.getName());
						Log.v(TAG, "About to read from a speicific file: " + file.getName());
						int characterIntValue;
						String value = "";
						while ((characterIntValue= fis.read()) != -1) {
							value = value + (char)characterIntValue;
						}		
						String[] cursorRow = new String[]{file.getName(), value};
						matrixCursor.addRow(cursorRow);
						Log.v(TAG, "Value read from file is: " + value);
					} catch (FileNotFoundException e) {
						Log.v(TAG, "File not found when reading ContentValues: " + file.getName());
						e.printStackTrace();
					} catch (IOException e) {
						Log.v(TAG, "Some IO Exception when reading ContentValues");
						e.printStackTrace();
					}			
				}
			}			
		}
		else{
			try {
				String fileName = providedUri.toString();
				fileName = fileName + "_" + keyValue;
				fileName = fileName.replace("content://", "");
				FileInputStream fis = this.getContext().openFileInput(fileName);
				Log.v(TAG, "About to read from a speicific file: " + fileName);
				int characterIntValue;
				String value = "";
				while ((characterIntValue= fis.read()) != -1) {
					value = value + (char)characterIntValue;
				}		
				String[] cursorRow = new String[]{keyValue, value};
				matrixCursor.addRow(cursorRow);
				Log.v(TAG, "Value read from file is: " + value);
			} catch (FileNotFoundException e) {
				Log.v(TAG, "File not found when reading ContentValues");
				e.printStackTrace();
			} catch (IOException e) {
				Log.v(TAG, "Some IO Exception when reading ContentValues");
				e.printStackTrace();
			}			
		}
		return matrixCursor;    }

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
		if(! joinedMessage.getAvdTwo().equals(joinedMessage.getAvdThree())){
			Log.v(TAG, "We now must shuffle our test values");
			Cursor resultCursor = query(selectAllUri, null, "", null, "");
			int keyIndex = resultCursor.getColumnIndex(OnTestClickListener.KEY_FIELD);
			int valueIndex = resultCursor.getColumnIndex(OnTestClickListener.VALUE_FIELD);
			for (boolean hasItem = resultCursor.moveToFirst(); hasItem; hasItem = resultCursor.moveToNext()) {
				String keyWithUri = resultCursor.getString(keyIndex);
				String[] values = keyWithUri.split("_");
				String key = values[1];
				String value = resultCursor.getString(valueIndex);
				Log.v(TAG, "Key and value are: " + key + " : " + value);
				
				String newSuccessorHash;
				try {
					newSuccessorHash = genHash(joinedMessage.getAvdThree());
					String keyHash = genHash(key);
					if(keyHash.compareTo(newSuccessorHash) >= 1){
						Log.v(TAG, "We have a node that must be sent to the newsuccessor and removed from the current contentprovider");
						this.getContext().deleteFile(keyWithUri);						
					}
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				
			}			
		}
	}    
}