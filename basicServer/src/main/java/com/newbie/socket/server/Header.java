package com.newbie.socket.server;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chilie
 * 
 * 消息头
 * crcCode:三部分组成，0xabef+主版本号+次版本号 ,长度2个字节
 * length:整个消息的长度 int
 * sessionId:唯一id  long
 * type:消息类型 1byte { 
 * 					0:业务请求消息
 * 					1:业务响应消息
 * 					2:业务one way消息
 * 					3:握手请求消息
 * 					4:握手响应消息
 * 					5:心跳请求消息
 * 					6:心跳响应消息}
 * priority:优先级 1byte
 * attachment Map<String,Object> 变长，扩展字段
 */
public final class Header {

	private int crcCode = 0xabef0101;
	private int length;
	private long sessionId;
	private byte type;
	private byte priority;
	private Map<String,Object> attachment = new HashMap<String,Object>();
	public int getCrcCode() {
		return crcCode;
	}
	public void setCrcCode(int crcCode) {
		this.crcCode = crcCode;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public long getSessionId() {
		return sessionId;
	}
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
		this.type = type;
	}
	public byte getPriority() {
		return priority;
	}
	public void setPriority(byte priority) {
		this.priority = priority;
	}
	public Map<String, Object> getAttachment() {
		return attachment;
	}
	public void setAttachment(Map<String, Object> attachment) {
		this.attachment = attachment;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Header: ");
		Field[] params = Header.class.getDeclaredFields();
		for(Field f : params) {
			sb.append("["+f.getName()+"=");
			f.setAccessible(true);
			try {
				sb.append(f.get(this)+",");
			} catch (IllegalArgumentException | IllegalAccessException e) {
				
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
