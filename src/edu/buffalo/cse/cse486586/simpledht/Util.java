package edu.buffalo.cse.cse486586.simpledht;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Util {

	public static String getPortNumber(Activity activity){
		TelephonyManager tel = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
		String port = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		String avdIdentifier = "unspecified";
		if(port.equals(Constants.AVD0_PORT)){
			avdIdentifier = Constants.AVD0;
		}
		else if(port.equals(Constants.AVD1_PORT)){
			avdIdentifier = Constants.AVD1;			
		}
		else if(port.equals(Constants.AVD2_PORT)){
			avdIdentifier = Constants.AVD2;						
		}
		
		return avdIdentifier;		
	}
	
	public static String[] getRemoteClientPorts(String portString){
		String[] remoteClientPorts = null;
		if(portString.equals(Constants.AVD0_PORT)){
			remoteClientPorts = Constants.AVD0_REMOTE_CLIENTS;
			Log.v(SimpleDhtMainActivity.TAG, "Found port to push to avd1 avd2");
		}
		else if(portString.equals(Constants.AVD1_PORT)){
			remoteClientPorts = Constants.AVD1_REMOTE_CLIENTS;
			Log.v(SimpleDhtMainActivity.TAG, "Found port to push to avd0 avd2");
		}
		else if(portString.equals(Constants.AVD2_PORT)){
			remoteClientPorts = Constants.AVD2_REMOTE_CLIENTS;
			Log.v(SimpleDhtMainActivity.TAG, "Found port to push to avd0 avd1");
		}
		else{
			Log.v(SimpleDhtMainActivity.TAG, "Did not find a push port!");
		}
		return remoteClientPorts;
	}

	public static int getPortNumber(String avd) {
		int port = 0;
		if(avd.equals(Constants.AVD0)){
			port = Integer.parseInt(Constants.AVD0_REDIRECT_PORT);
		}
		else if(avd.equals(Constants.AVD1)){
			port = Integer.parseInt(Constants.AVD1_REDIRECT_PORT);			
		}
		else if(avd.equals(Constants.AVD2)){
			port = Integer.parseInt(Constants.AVD1_REDIRECT_PORT);						
		}
		return port;
	}
	
}
