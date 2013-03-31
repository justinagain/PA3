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
	private SimpleDhtProvider sdp;
	
	
	public ServerTask(SimpleDhtProvider newSdp){
		sdp = newSdp;
	}
	
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
				DhtMessage dm = DhtMessage.createMessageFromByteArray(data);
				if(dm.isJoinRequest()){
					Log.v(TAG, "A join request has been received.");
					Log.v(TAG, "Recevied from " + dm.getAvdOne());
					sdp.processJoinRequest(dm);
				}
				else if(dm.isNewPredecessorResponse()){
					Log.v(TAG, "A join predecessor response has been received.");
					sdp.processNewPredecessorResponse(dm);
				}
				else if(dm.isNewJoinResponse()){
					Log.v(TAG, "A join response has been received.");
					sdp.processNewJoinResponse(dm);
				}
				else if(dm.isNewSucessorResponse()){
					Log.v(TAG, "A join successor response has been received.");
					sdp.processNewSuccessorResponse(dm);
				}
				else if(dm.isInsertRequest()){
					Log.v(TAG, "An insert request has been received.");
					sdp.processInsertRequest(dm);					
				}
				else if(dm.isGlobalDumpRequest() && ! dm.getAvdTwo().equals(sdp.getCurrentNode())){
					sdp.processGlobalRequest(dm);
				}
				else if(dm.isGlobalDumpRequest() && dm.getAvdTwo().equals(sdp.getCurrentNode())){
					sdp.processPublishGlobalDumpResponses(dm);
				}
				else if(dm.isGloablDumpResponse()){
					sdp.processGlobalDumpResponse(dm);
				}
				else if(dm.isSingleQueryRequest()){
					sdp.processSingleQueryRequest(dm);
				}
				else if(dm.isSingleQueryResponse()){
					sdp.processSingleQueryResponse(dm);
				}
				socket.close();
			}
		}
		catch (IOException e){
			Log.v(SimpleDhtMainActivity.TAG, "IOException creating ServerSocket");
		}
		return null;
	}

	
}