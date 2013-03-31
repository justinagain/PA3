package edu.buffalo.cse.cse486586.simpledht;

public class DhtMessage {
	
	public static final String GLOBAL_QUERY_RESPONSE = "a";
	public static final String GLOBAL_QUERY = "g";
	public static final String REQUEST_JOIN = "j";
	public static final String NEW_JOIN_RESPONSE = "r";
	public static final String NEW_PREDECESSOR_RESPONSE = "p";
	public static final String NEW_SUCCESSOR_RESPONSE = "s";
	public static final String QUERY = "q";
	public static final String INSERT = "i";
	public static final int AVD_INSERT_PT_ONE = 1;
	public static final int AVD_INSERT_PT_TWO = 5;
	public static final int AVD_INSERT_PT_THREE = 9;
	public static final int KEY_INSERT_PT = 5;
	public static final int VALUE_INSERT_PT = 11;
	public static final int COUNT_INSERT_PT = 17;
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
	
	public void setKey(String key){ 
		reinitializeArray(KEY_INSERT_PT, 6);
		insertTextPayloadContent(key, KEY_INSERT_PT);
	}

	public void setValue(String value){ 
		reinitializeArray(VALUE_INSERT_PT, 6);
		insertTextPayloadContent(value, VALUE_INSERT_PT);
	}

	public String getKey(){ 
		String keyValue = new String(getPayloadAsString(6, KEY_INSERT_PT));
		keyValue = keyValue.replaceAll("z", "");
		return keyValue;
	}
	
	public String getValue(){ 
		String contentValue = new String(getPayloadAsString(6, VALUE_INSERT_PT));
		contentValue = contentValue.replaceAll("z", "");
		return contentValue;
	}

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
	public boolean isNewJoinResponse(){ return determineType(NEW_JOIN_RESPONSE); }
	public boolean isNewPredecessorResponse(){ return determineType(NEW_PREDECESSOR_RESPONSE); }
	public boolean isNewSucessorResponse(){ return determineType(NEW_SUCCESSOR_RESPONSE); }
	public boolean isInsertRequest(){return determineType(INSERT);}
	public boolean isGlobalDumpRequest() {return determineType(GLOBAL_QUERY);}
	public boolean isGloablDumpResponse() {return determineType(GLOBAL_QUERY_RESPONSE);}

	
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

	public static DhtMessage getInsertMessage(String avd, String key, String value) {
		DhtMessage dhtMessage = new DhtMessage(INSERT);
		dhtMessage.setAvd(avd, AVD_INSERT_PT_ONE);
		dhtMessage.setKey(key);
		dhtMessage.setValue(value);
		return dhtMessage;
	}

	public static DhtMessage getGlobalDumpMessage(String sendAvd, String requestAvd, int count) {
		DhtMessage dhtMessage = new DhtMessage(GLOBAL_QUERY);
		dhtMessage.setAvd(sendAvd, DhtMessage.AVD_INSERT_PT_ONE);
		dhtMessage.setAvd(requestAvd, DhtMessage.AVD_INSERT_PT_TWO);
		dhtMessage.setCount(count);
		return dhtMessage;
	}

	public static DhtMessage getGlobalDumpResponseMessage(String sendAvd, String key, String value) {
		DhtMessage dhtMessage = new DhtMessage(GLOBAL_QUERY_RESPONSE);
		dhtMessage.setAvd(sendAvd, DhtMessage.AVD_INSERT_PT_ONE);
		dhtMessage.setKey(key);
		dhtMessage.setValue(value);
		return dhtMessage;
	}

	private void setCount(int count) {
		reinitializeArray(COUNT_INSERT_PT, 4);
		insertTextPayloadContent(count + "", COUNT_INSERT_PT);
	}
	
	/** byte[] manipulation methods */
	public String getMessageCount() {
		String intString = new String(getPayloadAsString(4, COUNT_INSERT_PT));
		intString = intString.replaceAll("z", "");
		return intString;
	}

}
