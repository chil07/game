package com.newbie.socket.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class LoginAuthReqHandler extends SimpleChannelInboundHandler<Object>{

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception{
		ctx.writeAndFlush(buildLoginReq());
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx,Object msg) throws Exception{
		NettyMessage message = (NettyMessage) msg;
		if(message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_RESP.getValue()) {
			byte loginResult = (byte) message.getBody();
			if(loginResult != (byte)0) {
				ctx.close();
			}else {
				System.out.println("Login is Ok "+message);
				ctx.fireChannelRead(msg);
			}
			
		}else {
			ctx.fireChannelRead(msg);
		}
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		
		ctx.flush();
	}
	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		ctx.fireExceptionCaught(cause);
	}
	
	private NettyMessage buildLoginReq() {
		NettyMessage msg = new NettyMessage();
		Header header = new Header();
		header.setType(MessageType.LOGIN_REQ.getValue());
		msg.setHeader(header);
		//msg.setBody("it is request");
		return msg;
	}


	
}
