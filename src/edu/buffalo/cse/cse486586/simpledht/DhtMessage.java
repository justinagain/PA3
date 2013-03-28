package edu.buffalo.cse.cse486586.simpledht;

public class DhtMessage {
	
	public static final String REQUEST_JOIN = "j";
	public static final String QUERY = "q";
	public static final String INSERT = "i";
//	public static final String TEST_TWO_BROADCAST = "c";
//	private static final int AVD_INSERT_PT = 1;
//	private static final int AVD_SEQUENCE_NUMBER_INSERT_PT = 5;
//	private static final int MSG_SIZE_INSERT_PT = 11;
//	private static final int MSG_INSERT_PT = 14;
	private static final byte ARRAY_INITIALIZER = "z".getBytes()[0];
	public static final int MSG_SIZE = 142;
	private byte[] payload;
	
	private DhtMessage(String type) {
		payload = new byte[142];
		initializeArray();
		payload[0] = type.getBytes()[0];
	}

	public static DhtMessage createMessageFromByteArray(byte[] data) {
		return new DhtMessage(data);
	}

	private DhtMessage(byte[] newPayload) {
		payload = newPayload;
	}

	private void initializeArray() {
		for (int i = 0; i < payload.length; i++) {
			payload[i] = ARRAY_INITIALIZER;
		}
	}
	
	private void reinitializeArray(int startIndex, int length){
		for (int i = 0; i < length; i++) {
			payload[startIndex] = ARRAY_INITIALIZER;
			startIndex++;
		}		
	}
	
	/** Factory methods to create specific message types */
	public static DhtMessage getJoinMessage(String port){
		DhtMessage dhtMessage = new DhtMessage(REQUEST_JOIN);
		return dhtMessage;
	}
	
	public static DhtMessage getQueryMessage(){
		DhtMessage dhtMessage = new DhtMessage(QUERY);
		return dhtMessage;		
	}
	
	public static DhtMessage getTestTwoRequestBroadcastMessage() {
		DhtMessage dhtMessage = new DhtMessage(INSERT);
		return dhtMessage;		
	}


}
