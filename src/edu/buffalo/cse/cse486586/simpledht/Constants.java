package edu.buffalo.cse.cse486586.simpledht;

import java.net.URI;
import java.net.URISyntaxException;

public class Constants {
	final public static String IP_ADDRESS = "10.0.2.2";

	final public static String AVD0 = "avd0";
	final public static String AVD1 = "avd1";
	final public static String AVD2 = "avd2";

	final public static String AVD0_PORT = "5554";
	final public static String AVD1_PORT = "5556";
	final public static String AVD2_PORT = "5558";
	final public static String AVD0_REDIRECT_PORT = "11108";
	final public static String AVD1_REDIRECT_PORT = "11112";
	final public static String AVD2_REDIRECT_PORT = "11116";
	final public static String[] AVD0_REMOTE_CLIENTS = new String[]{"11112", "11116"};
	final public static String[] AVD1_REMOTE_CLIENTS = new String[]{"11108", "11116"};
	final public static String[] AVD2_REMOTE_CLIENTS = new String[]{"11108", "11112"};

	final public static int[] AVD_REMOTE_CLIENTS = new int[]{11108, 11112, 11116};
	public static final int DHT_MASTER = 11108;
}
