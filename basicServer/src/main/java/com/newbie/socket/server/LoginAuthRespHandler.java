package com.newbie.socket.server;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class LoginAuthRespHandler extends SimpleChannelInboundHandler<Object>{

	private Map<String,Boolean> nodeCheck = new ConcurrentHashMap<String,Boolean>();
	
	private String[] whiteList = {"127.0.0.1"};
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception{
		NettyMessage message = (NettyMessage)msg;
		if(message.getHeader() != null &&
				message.getHeader().getType() == MessageType.LOGIN_REQ.getValue()) {
			String nodeIndex = ctx.channel().remoteAddress().toString();
			NettyMessage loginResp = null;
			if(nodeCheck.containsKey(nodeIndex)) {
				loginResp = buildResponse((byte)-1);
				
			}else {
				InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
				String ip = address.getAddress().getHostAddress();
				boolean isOK = false;
				for(String WIP:whiteList) {
					if(WIP.equals(ip)) {
						isOK = true;
						break;
					}
				}
				loginResp = isOK?buildResponse((byte)0):buildResponse((byte)-1);
				if(isOK) {
					nodeCheck.put(nodeIndex, true);
				}
				System.out.println("Login repsonse is :"+loginResp);
				ctx.writeAndFlush(loginResp);
			}
		}else {
			ctx.fireChannelRead(msg);
		}
	}
	
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("auth response read complete");
		ctx.flush();
	}
	
	private NettyMessage buildResponse(byte result) {
		NettyMessage message = new NettyMessage();
		Header header = new Header();
		header.setType(MessageType.LOGIN_RESP.getValue());
		message.setHeader(header);
		message.setBody(result);
		return message;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		nodeCheck.remove(ctx.channel().remoteAddress().toString());
		ctx.close();
		ctx.fireExceptionCaught(cause);
	}
	
	
}
