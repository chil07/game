package com.newbie.socket.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class SocketServer {

	static int port;
	public static ApplicationContext ac;
	static String springPaths = "spring-config.xml";
	
	public static void main(String args[]) {
		if(args.length > 0)
			port = Integer.parseInt(args[0]);
		else
			port = 8081;
		start();
	}
	
	private static void start() {
		ac = new ClassPathXmlApplicationContext(springPaths);
		//启动服务器
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bstrap = new ServerBootstrap();
			bstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new SocketServerInitializer());
			Channel channel = bstrap.bind(port).sync().channel();
			System.out.println("server start at port: "+port);
			channel.closeFuture().sync();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
}
