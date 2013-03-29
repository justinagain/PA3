package edu.buffalo.cse.cse486586.simpledht;

public class DhtMessage {
	
	public static final String REQUEST_JOIN = "j";
	public static final String RESPONSE_JOIN = "r";
	public static final String RESPONSE_PREDECESSOR_JOIN = "p";
	public static final String RESPONSE_SUCCESSOR_JOIN = "s";
	public static final String QUERY = "q";
	public static final String INSERT = "i";
	public static final int AVD_INSERT_PT_ONE = 1;
	public static final int AVD_INSERT_PT_TWO = 5;
	public static final int AVD_INSERT_PT_THREE = 9;
//	public static final String TEST_TWO_BROADCAST = "c";
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
	
	public byte[] getPayload(){ return payload;}

	
	/** Factory methods to create specific message types */
	public static DhtMessage getJoinMessage(String port){
		DhtMessage dhtMessage = new DhtMessage(REQUEST_JOIN);
		dhtMessage.setAvd(port, AVD_INSERT_PT_ONE);
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
	
	public void setAvd(String avdNumber, int insertionPoint){ 
		reinitializeArray(insertionPoint, 4);
		insertTextPayloadContent(avdNumber, insertionPoint);
	}
	
	public String getAvdOne(){ return new String(getPayloadAsString(4, AVD_INSERT_PT_ONE));}
	public String getAvdTwo(){ return new String(getPayloadAsString(4, AVD_INSERT_PT_TWO));}
	public String getAvdThree(){ return new String(getPayloadAsString(4, AVD_INSERT_PT_THREE));}

	private byte[] getPayloadAsString(int size, int startPoint) {
		byte[] avdBytes = new byte[size];
		for (int i = 0; i < avdBytes.length; i++) {
			avdBytes[i] = payload[startPoint];
			startPoint++;
		}
		return avdBytes;
	}
	
	private void insertTextPayloadContent(String value, int insertPoint) {
		byte[] stringBytes = value.getBytes();
		for (int i = 0; i < value.length(); i++) {
			payload[insertPoint] = stringBytes[i];
			insertPoint = insertPoint + 1;
		}
	}

	public boolean isJoinRequest(){ return determineType(REQUEST_JOIN); }
	public boolean isJoinResponse(){ return determineType(RESPONSE_JOIN); }
	public boolean isJoinPredecessorResponse(){ return determineType(RESPONSE_PREDECESSOR_JOIN); }
	public boolean isJoinSuccesorResponse(){ return determineType(RESPONSE_SUCCESSOR_JOIN); }
	
	private boolean determineType(String type) {
		String byteValue = new String(new byte[]{payload[0]});
		boolean isRequestType = false;
		if(byteValue.equals(type)){
			isRequestType = true;
		}
		return isRequestType;
	}

	public static DhtMessage getJoinResponseMessage(String predecessor, String insertNode, String successor, String responseType) {
		DhtMessage dhtMessage = new DhtMessage(responseType);
		dhtMessage.setAvd(predecessor,DhtMessage.AVD_INSERT_PT_ONE);
		dhtMessage.setAvd(insertNode,DhtMessage.AVD_INSERT_PT_TWO);
		dhtMessage.setAvd(successor,DhtMessage.AVD_INSERT_PT_THREE);
		return dhtMessage;
	}

	public static DhtMessage getDefaultMessage() {
		return new DhtMessage(REQUEST_JOIN);
	}

}
