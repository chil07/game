package com.newbie.socket.server;

/**
 * 
 * @author chilei
 * type:消息类型 1byte { 
 * 					0:业务请求消息
 * 					1:业务响应消息
 * 					2:业务one way消息
 * 					3:握手请求消息
 * 					4:握手响应消息
 * 					5:心跳请求消息
 * 					6:心跳响应消息}
 *
 */
public enum MessageType {

	BIZ_REQ((byte)0),BIZ_RESP((byte)1),BIZ_ONEWAY((byte)2),LOGIN_REQ((byte)3),LOGIN_RESP((byte)4),HEATB_REQ((byte)5),HEATB_RESP((byte)6);
	
	private MessageType(byte value) {
		this.value = value;
	}
	
	private byte value;
	
	public byte getValue() {
		return value;
	}
}
