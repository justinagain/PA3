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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
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
	public static final String ALL_SELECTION_LOCAL = "all_local_select";
	public static final String ALL_SELECTION_GLOBAL = "all_global_select";
	public static final String PREDECESSOR_NODE = "pred";
	public static final String CURRENT_NODE = "curr";
	public static final String SUCCESSOR_NODE = "succ";
	private boolean isLeader = false;
	private String successorNode;
	private String currentNode;
	private String predecessorNode;
	private ArrayList<DhtMessage> gloablMessages = new ArrayList<DhtMessage>();
	private boolean publishGlobalDump = false;
	private HashMap<String,String> ringMap = new HashMap<String, String>();

    @Override
    public Uri insert(Uri simpleDhtUri, ContentValues contentValues) {
		Log.v(TAG, "About to insert into content provider with URI: " + simpleDhtUri.toString());
        writeToInternalStorage(simpleDhtUri, contentValues);
        getContext().getContentResolver().notifyChange(simpleDhtUri, null);
		return simpleDhtUri;
    }
    
    @Override
    public Cursor query(Uri providedUri, String[] arg1, String keyValue, String[] arg3,
			String arg4) {
		Log.v(TAG, "Entering SimpleDhtProvider query");
    	MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
		if(keyValue.equals(ALL_SELECTION_LOCAL)){
			getLocalKeyValue(matrixCursor, "");			
		}
		else if(keyValue.equals(ALL_SELECTION_GLOBAL)){
			getLocalKeyValue(matrixCursor, "");
			new DhtRequesGlobalDumpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DhtMessage.getGlobalDumpMessage(successorNode, currentNode));
			while(!publishGlobalDump){
				//wait for the responses to come on in!
			}
		}
		else{
			getLocalKeyValue(matrixCursor, keyValue);
			// We need to go elsewhere to find the key
			if(matrixCursor.getCount() == 0){
				
			}
		}
		return matrixCursor;    
	}

	private boolean writeToInternalStorage(Uri uri, ContentValues contentValues){
		boolean success = false;
		FileOutputStream fos;
		try {
			Log.v(TAG, "About to insert into a speicific file");
			String keyValue = contentValues.get(OnTestClickListener.KEY_FIELD).toString();
			String contentValue = contentValues.get(OnTestClickListener.VALUE_FIELD).toString();

			//String type = evaluateHash(keyValue);

			String type = evaluateHashVersionTwo(keyValue);

			
			if(type.equals(PREDECESSOR_NODE)){
				Log.v(TAG, "I have a message that must be sent to the predecessor node " + predecessorNode);
				DhtMessage message = DhtMessage.getInsertMessage(predecessorNode, keyValue, contentValue);
		    	new DhtRequestInsertTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);				
			}
			else if(type.equals(CURRENT_NODE)){
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
			else if(type.equals(SUCCESSOR_NODE)){
				Log.v(TAG, "I have a message that must be sent to the successor node " + successorNode);				
				DhtMessage message = DhtMessage.getInsertMessage(successorNode, keyValue, contentValue);
		    	new DhtRequestInsertTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);				
			}
			
		} catch (FileNotFoundException e) {
			Log.v(TAG, "File not found when writing ContentValues");
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "Some IO Exception when writing ContentValues");
			e.printStackTrace();
		}
		return success;
	}

	private String evaluateHash(String keyToInsert){
		Log.v(TAG, "Evaluation where key should be inserted");
		String type = "";
		String keyHash;
		try {
			keyHash = genHash(keyToInsert);
			String predecessorHash = genHash(predecessorNode);
			String currentNodeHash = genHash(currentNode);
			String successorHash = genHash(successorNode);
			if( keyHash.compareTo(predecessorHash) < 1 && predecessorHash.compareTo(currentNodeHash) != 0){
				Log.v(TAG, "Key should go to Predecessor");
				type = PREDECESSOR_NODE;
			}
			else if( keyHash.compareTo(currentNodeHash) < 1 || currentNodeHash.compareTo(successorHash) == 0){
				Log.v(TAG, "Key should go to CurrentNode");
				type = CURRENT_NODE;
			}
			else if ( keyHash.compareTo(currentNodeHash) > 1 ) {
				Log.v(TAG, "Key should go to Successor");
				type = SUCCESSOR_NODE;
			}
			else{
				Log.v(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> A key with no home!");				
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return type;
	}

	private String evaluateHashVersionTwo(String keyToInsert){
		Log.v(TAG, "Evaluation where key should be inserted");
		String type = "";
		String keyHash;
		try {
			if(currentNode.equals("5558")){
				int i = 0;
				i++;
				
			}
			keyHash = genHash(keyToInsert);
			String predecessorHash = genHash(predecessorNode);
			String currentNodeHash = genHash(currentNode);
			String successorHash = genHash(successorNode);
			
			
			// CASE ONE: THERE IS ONE NODE
			if(predecessorHash.compareTo(currentNodeHash) == 0 && 
			   successorHash.compareTo(predecessorHash) == 0){
				Log.v(TAG, "CASE ONE hit");
				Log.v(TAG, "Key should go to CurrentNode - there is only one node");
				type = CURRENT_NODE;				
			}
			// CASE TWO: THERE ARE TWO NODES
			else if(predecessorHash.compareTo(currentNodeHash) != 0 && 
					successorHash.compareTo(currentNodeHash) != 0 && 
					predecessorHash.compareTo(successorHash) == 0){
				if(keyHash.compareTo(currentNodeHash) > 0 && 
						keyHash.compareTo(successorHash) < 0){
					Log.v(TAG, "CASE TWO A hit");
					Log.v(TAG, "Key should go to CurrentNode in two node setting");
					type = CURRENT_NODE;									
				}
				else if(keyHash.compareTo(currentNodeHash) > 0 &&
						currentNodeHash.compareTo(successorHash) > 0){
					Log.v(TAG, "CASE TWO B hit");
					Log.v(TAG, "Key should go to CurrentNode in two node setting");
					type = CURRENT_NODE;														
				}
				else if(keyHash.compareTo(currentNodeHash) < 0 &&
						keyHash.compareTo(successorHash) < 0){
					Log.v(TAG, "CASE TWO C hit");
					Log.v(TAG, "Key should go to CurrentNode in two node setting");
					type = CURRENT_NODE;																			
				}
				else{
					Log.v(TAG, "CASE TWO B hit");
					Log.v(TAG, "Key should go to SuccessorNode in two node setting");
					type = SUCCESSOR_NODE;														
				}
			}
			// CASE THREE: THERE ARE THREE OR MORE NODES
			else{
				// Sweet Spot Hit
				if(keyHash.compareTo(currentNodeHash) > 0 && 
						keyHash.compareTo(successorHash) < 0){
					Log.v(TAG, "CASE THREE A hit");
					Log.v(TAG, "Key should go to CurrentNode in two node setting");
					type = CURRENT_NODE;									
				}
				// End of ring hit no loop
				else if(keyHash.compareTo(currentNodeHash) > 0 &&
						currentNodeHash.compareTo(successorHash) > 0){
					Log.v(TAG, "CASE THREE B hit");
					Log.v(TAG, "Key should go to CurrentNode in two node setting");
					type = CURRENT_NODE;														
				}
				// End of ring hit with loop
				else if(keyHash.compareTo(currentNodeHash) < 0 &&
						keyHash.compareTo(successorHash) < 0){
					Log.v(TAG, "CASE THREE C hit");
					Log.v(TAG, "Key should go to CurrentNode in two node setting");
					type = CURRENT_NODE;																			
				}
				// Send to predecessor
				else if(keyHash.compareTo(currentNodeHash) < 0){
					Log.v(TAG, "CASE THREE D hit");
					Log.v(TAG, "Key should go to PredecessorNode in two node setting");
					type = SUCCESSOR_NODE;														
				}
				// Send to successor
				else if(keyHash.compareTo(successorHash) > 0){
					Log.v(TAG, "CASE THREE E hit");
					Log.v(TAG, "Key should go to SuccessorNode in two node setting");
					type = SUCCESSOR_NODE;														
				}

			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return type;
	}

	
	private void getLocalKeyValue(MatrixCursor matrixCursor, String keyValue) {
		Log.v(TAG, "Entering getLocalKeyValue");
		File[] files = this.getContext().getFilesDir().listFiles();
		String fileName = Util.getProviderUri().toString();
		fileName = fileName.replace("content://", "");
		for (File file : files) {
			String forLoopKeyValue = keyValue;
			if(keyValue.length() == 0){
				forLoopKeyValue = file.getName().split("_")[1];
			}
			if(file.getName().startsWith(fileName) && file.getName().endsWith(forLoopKeyValue)){
				Log.v(TAG, "A match is found: " + forLoopKeyValue);
				try {
					FileInputStream fis = this.getContext().openFileInput(file.getName());
					int characterIntValue;
					String value = "";
					while ((characterIntValue= fis.read()) != -1) {
						value = value + (char)characterIntValue;
					}		
					String[] cursorRow = new String[]{forLoopKeyValue, value};
					matrixCursor.addRow(cursorRow);
				} catch (FileNotFoundException e) {
					Log.v(TAG, "File not found when reading ContentValues: " + file.getName());
					e.printStackTrace();
				} catch (IOException e) {
					Log.v(TAG, "Some IO Exception when reading ContentValues");
					e.printStackTrace();
				}			
			}
		}
		Log.v(TAG, "Matrix size is: " + matrixCursor.getCount());
	}

	/** Process messages **/
	public void processJoinRequest(DhtMessage dm) {
		try {

			String predecessor = "";			
			String successor = "";
			String pseudoCurrentNode = dm.getAvdOne();
			String pseudoCurrentNodeHash = genHash(pseudoCurrentNode);

			
			if(ringMap.keySet().size() == 0){
				predecessor = pseudoCurrentNode;
				successor = pseudoCurrentNode;
				ringMap.put(pseudoCurrentNodeHash, pseudoCurrentNode);
			}
			else if(ringMap.keySet().size() == 1){
				predecessor = ringMap.get(ringMap.keySet().toArray()[0]);;
				successor = ringMap.get(ringMap.keySet().toArray()[0]);;
				ringMap.put(pseudoCurrentNodeHash, pseudoCurrentNode);
			}
			else{
				ringMap.put(pseudoCurrentNodeHash, pseudoCurrentNode);
				Object[] keys = ringMap.keySet().toArray();
				Arrays.sort(keys);
				for (int i = 0; i < keys.length; i++) {
					if(((String)keys[i]).compareTo(pseudoCurrentNodeHash) == 0){
						if(i == 0){
							predecessor = ringMap.get(keys[2]);
							successor = ringMap.get(keys[1]);							
						}
						else if(i == 1){
							predecessor = ringMap.get(keys[0]);
							successor = ringMap.get(keys[2]);							
						}
						else{
							predecessor = ringMap.get(keys[1]);
							successor = ringMap.get(keys[0]);							
						}
					}
				}

			}
			
			Log.v(SimpleDhtMainActivity.TAG, "Determined ordering: ");
			Log.v(SimpleDhtMainActivity.TAG, "predecessor: " + predecessor);
			Log.v(SimpleDhtMainActivity.TAG, "currentNode: " + currentNode);
			Log.v(SimpleDhtMainActivity.TAG, "successor: " + successor);
			
			DhtMessage one = DhtMessage.getJoinResponseMessage(predecessor, pseudoCurrentNode, successor, DhtMessage.NEW_PREDECESSOR_RESPONSE);
			DhtMessage two = DhtMessage.getJoinResponseMessage(predecessor, pseudoCurrentNode, successor, DhtMessage.NEW_JOIN_RESPONSE);
			DhtMessage three = DhtMessage.getJoinResponseMessage(predecessor, pseudoCurrentNode, successor, DhtMessage.NEW_SUCCESSOR_RESPONSE);			
			new DhtJoinResponseClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, one, two, three); 

		} catch (NoSuchAlgorithmException e) {
			Log.v(SimpleDhtMainActivity.TAG, "Error trying to place request in nodemap in processJoinRequest");
			e.printStackTrace();
		}
	}

	public void processNewSuccessorResponse(DhtMessage dm) {
		String newPredecessor = dm.getAvdTwo();
		Log.v(TAG,"New successor for: " + currentNode);
		Log.v(TAG,"Setting s to: " + newPredecessor);
		predecessorNode = newPredecessor;
	}
	
	public void processNewJoinResponse(DhtMessage dm) {
		predecessorNode = dm.getAvdOne();
		successorNode = dm.getAvdThree();
		Log.v(TAG,"Setting predecessor and successor for: " + currentNode);
		Log.v(TAG,"Setting successor to: " + successorNode);
		Log.v(TAG,"Setting predecessor to: " + predecessorNode);
	}
			
	public void processNewPredecessorResponse(DhtMessage dm) {
		String newSuccessor = dm.getAvdTwo();
		Log.v(TAG,"New predecessor for: " + currentNode);
		Log.v(TAG,"Setting predecessor to: " + newSuccessor);
		successorNode = newSuccessor;
	}


	public void processInsertRequest(DhtMessage dm) {
		ContentValues cv = new ContentValues();
		cv.put(OnTestClickListener.KEY_FIELD, dm.getKey());
		cv.put(OnTestClickListener.VALUE_FIELD, dm.getValue());
		insert(Util.getProviderUri(), cv);
	}    

	public void processGlobalDumpResponse(DhtMessage dm) {
		gloablMessages.add(dm);
	}    

	public void publishGlobalDumpResponses() {
		publishGlobalDump = true;
	}


	/** Overidden mehtods**/
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {return 0;}
    @Override
    public String getType(Uri uri) {return null;}
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {return 0;}
	
	/** getter methods **/
	public String getCurrentNode() { return currentNode; }
	public String getSuccessorNode() {return successorNode;}    	
	public String getPredecessorNode() {return predecessorNode;}    	
	public boolean isLeader(){ return isLeader;}
	    
	/** Initialization code **/
    @Override
    public boolean onCreate() {
    	Log.v(SimpleDhtMainActivity.TAG, "Cleaning provider....");
    	cleanProvider();
    	setNodes();
		setLeaderStatus();
    	Log.v(SimpleDhtMainActivity.TAG, "Set the port number and leader status.");
    	createServer();
    	broadCastJoin();
    	return false;
    }

	private void cleanProvider() {
		File[] files = this.getContext().getFilesDir().listFiles();
		String fileName = Util.getProviderUri().toString();            
		fileName = fileName.replace("content://", "");
		for (File file : files) {
			Log.v(TAG, "Base file is: " + fileName + " and compare name is: " + file.getName());
			if(file.getName().startsWith(fileName)){
				Log.v(TAG, "We have a match and must delete - it is old content.");
				file.delete();				
			}
		}		
	}

	private void setLeaderStatus() {
		if(currentNode.equals(Constants.AVD0_PORT)){
			Log.v(SimpleDhtMainActivity.TAG, "I am the leader.  Set my value to true.");
			isLeader = true;
		}
	}
	
	private void setNodes() {
		Application application = (Application)getContext();
		TelephonyManager tel = (TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
		currentNode = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		predecessorNode = currentNode;
		successorNode = currentNode;
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
    	new DhtRequestJoinClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, DhtMessage.getJoinMessage(currentNode));
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


//	else{
//	try {
//		String fileName = providedUri.toString();
//		fileName = fileName + "_" + keyValue;
//		fileName = fileName.replace("content://", "");
//		FileInputStream fis = this.getContext().openFileInput(fileName);
//		Log.v(TAG, "About to read from a speicific file: " + fileName);
//		int characterIntValue;
//		String value = "";
//		while ((characterIntValue= fis.read()) != -1) {
//			value = value + (char)characterIntValue;
//		}		
//		String[] cursorRow = new String[]{keyValue, value};
//		matrixCursor.addRow(cursorRow);
//		Log.v(TAG, "Value read from file is: " + value);
//	} catch (FileNotFoundException e) {
//		Log.v(TAG, "File not found when reading ContentValues");
//		e.printStackTrace();
//	} catch (IOException e) {
//		Log.v(TAG, "Some IO Exception when reading ContentValues");
//		e.printStackTrace();
//	}			
//}

    
//	if(! joinedMessage.getAvdTwo().equals(joinedMessage.getAvdThree())){
//	Log.v(TAG, "We now must shuffle our test values");
//	Cursor resultCursor = query(Util.getProviderUri(), null, ALL_SELECTION_LOCAL, null, "");
//	int keyIndex = resultCursor.getColumnIndex(OnTestClickListener.KEY_FIELD);
//	int valueIndex = resultCursor.getColumnIndex(OnTestClickListener.VALUE_FIELD);
//	FileOutputStream fos;
//	for (boolean hasItem = resultCursor.moveToFirst(); hasItem; hasItem = resultCursor.moveToNext()) {
//		String keyWithUri = resultCursor.getString(keyIndex);
//		String[] values = keyWithUri.split("_");
//		String key = values[1];
//		String value = resultCursor.getString(valueIndex);
//		Log.v(TAG, "Key and value are: " + key + " : " + value);
//						
//		String newSuccessorHash;
//		try {
//			TreeMap<String, Object> instanceMap = new TreeMap<String, Object>();
//			instanceMap.put(genHash(joinedMessage.getAvdOne()), joinedMessage.getAvdOne());
//			instanceMap.put(genHash(joinedMessage.getAvdTwo()), joinedMessage.getAvdTwo());
//			instanceMap.put(genHash(joinedMessage.getAvdThree()), joinedMessage.getAvdThree());
//			String keyHash = genHash(key);
//			instanceMap.put(keyHash, value);
//			
//			String lowestHash = instanceMap.lowerKey(keyHash);										
//			if(lowestHash == null){
//				lowestHash = instanceMap.lastKey();
//			}					
//			
//			Log.v(TAG, "Current node is: " + joinedMessage.getAvdTwo());
//			newSuccessorHash = genHash(joinedMessage.getAvdThree());
//			if(lowestHash.compareTo(newSuccessorHash) == 0){
//				Log.v(TAG, "We have a node that must be sent to the newsuccessor and removed from the current contentprovider");
//				//1. Send to new one
//				DhtMessage message = DhtMessage.getInsertMessage(joinedMessage.getAvdThree(), key, value);
//		    	new DhtRequestInsertTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
//				//create an sync and ship it on out ... 
//				// 2. Remove from old one
//				this.getContext().deleteFile(keyWithUri);						
//			}
//			else{
//				Log.v(TAG, "I have a message that belongs to the current node " + dm.getAvdTwo());	
//				String fileName = Util.getProviderUri().toString();
//				fileName = fileName + "_" + key;
//				fileName = fileName.replace("content://", "");
//				Log.v(TAG, "filename is: " + fileName);
//				try {
//					fos = this.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
//					fos.write(value.getBytes());				
//					fos.close();
//					Log.v(TAG, "Wrote ContentValues successfully.");				
//				} catch (FileNotFoundException e) {
//					Log.v(TAG, "Error occurred writing file.");				
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					Log.v(TAG, "Error occurred writing file.");				
//					e.printStackTrace();
//				}
//			}
//
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		}
//		
//	}			
//}
    
//	Log.v(TAG, "keyvalue is: " + keyValue);
//	String keyHash = genHash(keyValue);
//	keyValueMap.put(keyHash, keyValue);
//	String predecessorHash = genHash(predecessorNode);
//	keyValueMap.put(predecessorHash, predecessorNode);
//	String currentNodeHash = genHash(currentNode);
//	keyValueMap.put(currentNodeHash, currentNode);
//	String successorHash = genHash(successorNode);
//	keyValueMap.put(successorHash, successorNode);
//	
//	
//	String sHash = keyValueMap.higherKey(keyHash);
//	if(sHash == null){
//		sHash = keyValueMap.firstKey();
//	}
	

//	if(sHash.compareTo(predecessorHash) == 0  && sHash.compareTo(currentNodeHash) != 0 && sHash.compareTo(successorHash) != 0){
//	Log.v(TAG, "I have a message that must be sent to the predecessor node " + predecessorNode);
//	DhtMessage message = DhtMessage.getInsertMessage(predecessorNode, keyValue, contentValue);
//	new DhtRequestInsertTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
//}
//else if(sHash.compareTo(successorHash) == 0   && sHash.compareTo(currentNodeHash) != 0 && sHash.compareTo(predecessorHash) != 0){
//	Log.v(TAG, "I have a message that must be sent to the successor node " + successorNode);				
//	DhtMessage message = DhtMessage.getInsertMessage(successorNode, keyValue, contentValue);
//	new DhtRequestInsertTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
//}
//else{
//	Log.v(TAG, "I have a message that belongs to the current node " + currentNode);				
//	String fileName = uri.toString().replace("content://", "");
//	fileName = fileName + "_" + keyValue;
//	Log.v(TAG, "filename is: " + fileName);
//	fos = this.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
//	fos.write(contentValue.getBytes());				
//	fos.close();
//	success = true;
//	Log.v(TAG, "Wrote ContentValues successfully.");				
//}

//	Log.v(SimpleDhtMainActivity.TAG, "Creating join request responses");
//	String hash = genHash(dm.getAvdOne());
//	nodeMap.put(hash, dm.getAvdOne());
//	
//	String predecessorHash = nodeMap.lowerKey(hash);
//	if(predecessorHash == null){
//		predecessorHash = nodeMap.lastKey();
//	}
//	String predecessor = (String)nodeMap.get(predecessorHash);
//	// If happens to be the first key, cycle to create the ring
//	String successorHash = nodeMap.higherKey(hash);
//	if(successorHash == null){
//		successorHash = nodeMap.firstKey();
//	}
//	String successor = (String)nodeMap.get(successorHash);
    
}