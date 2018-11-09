package com.newbie.common;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class BizThreadPoolUtil {

	private static final ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

	private static final ExecutorService threadPool = new ThreadPoolExecutor(2, 8, 4000, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(5));

	public static void doBiz(ChannelHandlerContext ctx, MessageEntity msg) {
		singleExecutor.submit(new MsgTaskResult(ctx, msg));
	}
}

class MsgTaskResult implements Callable<String> {
	ChannelHandlerContext ctx;
	MessageEntity msg;

	public MsgTaskResult(ChannelHandlerContext ctx, MessageEntity msg) {
		this.ctx = ctx;
		this.msg = msg;
	}

	@Override
	public String call() throws Exception {
		if (msg instanceof LoginMessageEntity) {
			for (Channel channel : WebSocketServerHandler.channels) {
				channel.writeAndFlush(new TextWebSocketFrame(msg.getMsg() + "加入了大厅"));
				
			}
		} else if (msg instanceof ChatMessageEntity) {
			for (Channel channel : WebSocketServerHandler.channels) {
				if (channel != ctx.channel()) {
					channel.writeAndFlush(new TextWebSocketFrame("[" + ctx.channel().remoteAddress() + "]" + msg.getMsg()
							+ " ,欢迎使用netty websocket server服务, 现在时刻:" + new java.util.Date().toString()));
				} else {
					channel.writeAndFlush(new TextWebSocketFrame(
							msg.getMsg() + " ,欢迎使用netty websocket server服务, 现在时刻:" + new java.util.Date().toString()));

				}
			}
		} else if(msg instanceof CreateRoomMsgEntity) {
			//
			Random r = new Random();
			Integer roomNum = r.nextInt(100);
			WebSocketServerHandler.rooms.put(roomNum, msg.getMsg().split(":")[1]);
			ArrayList<String> users = new ArrayList<String>();
			users.add(msg.getMsg().split(":")[1]);
			WebSocketServerHandler.roomUser.put(roomNum, users);
			ArrayList<Channel> channels = new ArrayList<Channel>();
			channels.add(ctx.channel());
			WebSocketServerHandler.roomChannel.put(roomNum, channels);
			for (Channel channel : WebSocketServerHandler.channels) {
				channel.writeAndFlush(new TextWebSocketFrame(msg.getMsg() + "创建了房间"+roomNum));
				
			}
		} else if(msg instanceof JoinRoomMsgEntity) {
			int roomNum = Integer.valueOf(msg.getMsg().split(":")[2]);
			String username = msg.getMsg().split(":")[1];
			ArrayList<Channel> channels = WebSocketServerHandler.roomChannel.get(roomNum);
			if(channels != null) {
				channels.add(ctx.channel());
			}
			ArrayList<String> user = WebSocketServerHandler.roomUser.get(roomNum);
			if(user != null) {
				user.add(username);
			}
			for(Channel channel: channels) {
				channel.writeAndFlush(new TextWebSocketFrame(msg.getMsg() + "加入了房间"));
				
			}
		}
		Thread.sleep(2000);
		return "send message success";
	}
}
