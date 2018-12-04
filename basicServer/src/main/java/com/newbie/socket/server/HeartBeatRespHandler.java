package com.newbie.socket.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HeartBeatRespHandler extends SimpleChannelInboundHandler<Object>{

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception{
		NettyMessage message = (NettyMessage)msg;
		if(message.getHeader() != null 
				&& message.getHeader().getType() == MessageType.HEATB_REQ.getValue()) {
			NettyMessage heartBeat = buildHeartBeatResponse();
			System.out.println("server receive heart beat: "+ message);
			ctx.writeAndFlush(heartBeat);
		}else {
			ctx.fireChannelRead(msg);
		}
	}
	
	private NettyMessage buildHeartBeatResponse() {
		NettyMessage message = new NettyMessage();
		Header header = new Header();
		header.setType(MessageType.HEATB_RESP.getValue());
		message.setHeader(header);
		return message;
	}
}
