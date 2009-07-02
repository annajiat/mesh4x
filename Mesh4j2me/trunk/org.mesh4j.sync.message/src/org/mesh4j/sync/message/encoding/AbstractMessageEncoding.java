package org.mesh4j.sync.message.encoding;

import org.mesh4j.sync.validations.Guard;

public abstract class AbstractMessageEncoding implements IMessageEncoding {

	public String decode(String message) {
		Guard.argumentNotNull(message, "message");
		if(message.length() == 0){
			return message;
		}
		
		if(!message.startsWith("n") && !message.startsWith("c")){
			Guard.throwsArgumentException("ERROR_MESSAGE_SYNC_INVALID_ENCODED_MESSAGE", new String[]{message});
		}
		String data = message.substring(1, message.length());
		if(message.startsWith("n")){
			return data;
		}
		String decodedMsg = this.basicDecode(data);
		if(decodedMsg == null){
			Guard.throwsArgumentException("ERROR_MESSAGE_SYNC_INVALID_ENCODED_MESSAGE", new String[]{message});
		}
		return decodedMsg;
	}

	protected abstract String basicDecode(String message);


	public String encode(String message) {
		Guard.argumentNotNull(message, "message");
		if(message.length() == 0){
			return message;
		}
		String encodeMsg = this.basicEncode(message);
		if(encodeMsg.length() >= message.length()+1){
			encodeMsg = "n"+message;
		}else{
			encodeMsg = "c"+encodeMsg;
		}
		return encodeMsg;
	}

	public boolean isBynary() {
		return false;
	}
	
	protected abstract String basicEncode(String message);
}
