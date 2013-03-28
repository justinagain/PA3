package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

public class ServerTask extends AsyncTask<ServerSocket, String, Void>{

	private static final String TAG = ServerTask.class.getName();
	
	@Override
	protected Void doInBackground(ServerSocket... sockets) {
		Log.v(TAG, "Create a socket");
		String msg = null;
		ServerSocket serverSocket = sockets[0];
		Socket socket;
		
		try{
			while(true){					
				Log.v(TAG, "Socket awaits accept ... ");
				socket = serverSocket.accept();
				Log.v(TAG, "A message is coming in ... ");
				InputStream stream = socket.getInputStream();
				byte[] data = new byte[DhtMessage.MSG_SIZE];
				int count = stream.read(data);				
				Log.v(TAG, "Message recieved with bytes: " + count);
				DhtMessage bm = DhtMessage.createMessageFromByteArray(data);
				if(bm.isJoinRequest()){
					Log.v(TAG, "A join request has been received.");
					Log.v(TAG, "Recevied from " + bm.getAvd());
					//processBroadcastRequest(bm, BroadcastMessage.BROADCAST);
				}
			}
		}
//				else if(bm.isBroadcast()){
//					Log.v(INFO_TAG, "A broadcast has been received.");					
//					processBroadcastReceipt(bm);
//				}
//				else if(bm.isTestTwoRequestBroadcast()){
//					Log.v(INFO_TAG, "TestTwo case has been received.");					
//					processBroadcastRequest(bm, BroadcastMessage.TEST_TWO_BROADCAST);					
//				}				
//				else if(bm.isTestTwoBroadcast()){
//					Log.v(INFO_TAG, "A TestTwo broadcast has been received.");					
//					processBroadcastReceipt(bm);
//					//if(bm.getAvd().equals(Util.getPortNumber(mActivity))){
//						// Call it once
//						createTestTwoGenericBroadcastRequest();							
//						// Call it twice
//						createTestTwoGenericBroadcastRequest();					
//					//}
//				}
//				socket.close();
//			}
//		}
		catch (IOException e){
			Log.v(SimpleDhtMainActivity.TAG, "IOException creating ServerSocket");
		}
		return null;
	}

	
}