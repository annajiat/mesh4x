package com.mesh4j.sync.message.encoding;

public interface IMessageEncoding {

	String encode(String message);
	
	String decode(String message);
}
