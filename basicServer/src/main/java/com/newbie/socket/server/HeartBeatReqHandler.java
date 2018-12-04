package com.newbie.socket.server;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HeartBeatReqHandler extends SimpleChannelInboundHandler<Object>{
	
	private volatile ScheduledFuture<?> heartBeat;
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception{
		NettyMessage message = (NettyMessage)msg;
		//客户端收到握手成功的请求
		if(message.getHeader() != null 
				&& message.getHeader().getType() == MessageType.LOGIN_RESP.getValue()) {
			heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatReqHandler.HeartBeatTask(ctx), 0, 5000, TimeUnit.MILLISECONDS);
		
		}else if(message.getHeader() != null 
				&& message.getHeader().getType() == MessageType.HEATB_RESP.getValue()) {
			System.out.println("client receive heart beat:"+message);
		}else {
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if(heartBeat != null) {
			heartBeat.cancel(true);
			heartBeat = null;
		}
		ctx.fireExceptionCaught(cause);
	}
	
	
	private class HeartBeatTask implements Runnable{
		
		private final ChannelHandlerContext ctx;
		
		public HeartBeatTask(final ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			NettyMessage msg = buildHeartBeat();
			System.out.println("client send heart beat: "+msg);
			ctx.writeAndFlush(msg);
		}
		
		private NettyMessage buildHeartBeat() {
			NettyMessage message = new NettyMessage();
			Header header = new Header();
			header.setType(MessageType.HEATB_REQ.getValue());
			message.setHeader(header);
			return message;
		}
		
	}
	
}
