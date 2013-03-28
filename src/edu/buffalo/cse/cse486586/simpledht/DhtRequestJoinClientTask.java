package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class DhtRequestJoinClientTask extends AsyncTask<DhtMessage, Void, Void>{

		private static final String TAG = DhtRequestJoinClientTask.class.getName();
		
		protected Void doInBackground(DhtMessage... msgs){
			try {
				Log.v(TAG, "About to push to socket: " + Constants.DHT_MASTER);
				Socket writeSocket = new Socket(Constants.IP_ADDRESS, Constants.DHT_MASTER);
				writeSocket.getOutputStream().write(msgs[0].getPayload());
				writeSocket.getOutputStream().flush();
				writeSocket.close();
				Log.v(TAG, "Pushed!");
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Log.v(TAG, "Error creating Inet Address");
			} catch (IOException e) {
				Log.v(TAG, "Error creating Socket");
				e.printStackTrace();
			}
			return null;
		}
		
	
	
}
