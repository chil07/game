package com.newbie.socket.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class SocketClient {
	static final String host = "127.0.0.1";
	static final int port = 8081;

	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	EventLoopGroup group = new NioEventLoopGroup();
	
	public static void main(String args[]) throws Exception{
		new SocketClient().connect(host, port);
	}
	
	public void connect(String host, int port) throws Exception {
		try {
			Bootstrap b = new Bootstrap(); 
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).
			handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new NettyMessageDecoder(1024*1024,4,4,0,0));
					ch.pipeline().addLast("MessageEncoder",new NettyMessageEncoder());
					ch.pipeline().addLast("readTimeoutHandler",new ReadTimeoutHandler(50));
					ch.pipeline().addLast("LoginAuthHandler",new LoginAuthReqHandler());
					ch.pipeline().addLast("HeartBeatHandler",new HeartBeatReqHandler());
				}
				
			});
			ChannelFuture future = b.connect(new InetSocketAddress(host,port)).sync();
			System.out.println("client start");
			future.channel().closeFuture().sync();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						TimeUnit.SECONDS.sleep(5);
						try {
							connect(host,port);
						}catch(Exception e) {
							e.printStackTrace();
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				
			});
		}
	}
}
