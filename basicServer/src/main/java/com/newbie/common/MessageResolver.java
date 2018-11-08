package com.newbie.common;

import org.springframework.stereotype.Service;

@Service
public class MessageResolver {

	public MessageEntity resolveMsg(String msg) {
		if(msg.contains("login:")) {
			return new LoginMessageEntity(msg);
		}else if(msg.contains("chat:")) {
			return new ChatMessageEntity(msg);
		}else {
			return new MessageEntity(msg);
		}
		
	}
}
