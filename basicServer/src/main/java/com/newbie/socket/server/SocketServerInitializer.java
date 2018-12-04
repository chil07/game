package com.newbie.socket.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class SocketServerInitializer extends ChannelInitializer<SocketChannel>{

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new NettyMessageDecoder(1024*1024,4,4,0,0));
		ch.pipeline().addLast(new NettyMessageEncoder());
		ch.pipeline().addLast("readTimeoutHandler",new ReadTimeoutHandler(50));
		ch.pipeline().addLast(new LoginAuthRespHandler());
		ch.pipeline().addLast("heartbeatHandler",new HeartBeatRespHandler());
	}

}
