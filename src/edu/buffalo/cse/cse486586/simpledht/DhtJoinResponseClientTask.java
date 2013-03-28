package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class DhtJoinResponseClientTask extends AsyncTask<DhtMessage, Void, Void>{

		private static final String TAG = DhtJoinResponseClientTask.class.getName();
		
		protected Void doInBackground(DhtMessage... msgs){
			try {

				Log.v(TAG, "About to push to socket: " + Util.getPortNumber(msgs[0].getAvdOne()));				
				Socket writeSocket = new Socket(Constants.IP_ADDRESS, Util.getPortNumber(msgs[0].getAvdOne()));
				writeSocket.getOutputStream().write(msgs[0].getPayload());
				writeSocket.getOutputStream().flush();
				writeSocket.close();
				Log.v(TAG, "Pushed to predecessor!");

				Log.v(TAG, "About to push to socket: " + Util.getPortNumber(msgs[1].getAvdTwo()));
				writeSocket = new Socket(Constants.IP_ADDRESS, Util.getPortNumber(msgs[1].getAvdTwo()));
				writeSocket.getOutputStream().write(msgs[1].getPayload());
				writeSocket.getOutputStream().flush();
				writeSocket.close();
				Log.v(TAG, "Pushed to insert node!");

				Log.v(TAG, "About to push to socket: " + Util.getPortNumber(msgs[2].getAvdThree()));				
				writeSocket = new Socket(Constants.IP_ADDRESS, Util.getPortNumber(msgs[2].getAvdThree()));
				writeSocket.getOutputStream().write(msgs[2].getPayload());
				writeSocket.getOutputStream().flush();
				writeSocket.close();
				Log.v(TAG, "Pushed to successor!");
				
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
