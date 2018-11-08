package com.newbie.common;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/*
 * 基础Server启动类
 */
public class BasicServer {
	static int port;
	public static ApplicationContext ac;
	static String springPaths = "spring-config.xml";

	public static void main(String args[]) throws Exception{
		if(args.length > 0) {
			port = Integer.parseInt(args[0]);	
		}
		start();	
	}
	public static void start() {
		//加载spring配置
		
		ac = new ClassPathXmlApplicationContext(springPaths);
		//启动服务器
		EventLoopGroup bGroup = new NioEventLoopGroup();//两个线程组，这个处理接受客户端的网络请求
		EventLoopGroup wGroup = new NioEventLoopGroup();//处理socketChannel的网络读写
		try {
			ServerBootstrap bstrap = new ServerBootstrap();
			bstrap.group(bGroup,wGroup)
			.channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					// TODO Auto-generated method stub
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast(new HttpServerCodec());
					pipeline.addLast(new HttpObjectAggregator(65536));
					pipeline.addLast(new ChunkedWriteHandler());
					pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
					pipeline.addLast(new IdleStateHandler(60, 30, 60 * 30, TimeUnit.SECONDS));
					pipeline.addLast(new WebSocketServerHandler());
				}
			
			}).option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);
			Channel ch = bstrap.bind(port).sync().channel();
			System.out.println("server start at port:"+port+" success");
			ch.closeFuture().sync();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}finally {
			bGroup.shutdownGracefully();
			wGroup.shutdownGracefully();
			
		}
	}
}