package com.newbie.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

	private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

	private WebSocketServerHandshaker handshaker;

	MessageResolver msgResolver;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

		Thread th = Thread.currentThread();

		System.out.println("Tread name:" + th.getName());

		// http请求
		System.out.println("get a message" + msg);
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		}
		// websocket请求
		else if (msg instanceof WebSocketFrame) {
			handleWebsocketFrame(ctx, (WebSocketFrame) msg);
		}

	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
		if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
			sendHttpResponse(ctx, req,
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		System.out.println("first connect1");
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://127.0.0.1:8081/ws",
				null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			System.out.println("first connect2");
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
		} else {
			System.out.println("first connect3");
			handshaker.handshake(ctx.channel(), req);
		}
		System.out.println("first connect4");
	}

	private void handleWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		// 判断是否是关闭链路的命令
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}
		// 是否是ping消息
		if (frame instanceof PingWebSocketFrame) {
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}
		// 仅文本消息处理
		if (!(frame instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(
					String.format("%s frame types not support", frame.getClass().getName()));
		}
		// 返回消息处理
		String request = ((TextWebSocketFrame) frame).text();
		// 根据不同的request请求处理成不同的对象
		msgResolver = BasicServer.ac.getBean(MessageResolver.class);
		MessageEntity msgEntity = msgResolver.resolveMsg(request);
		if (msgEntity instanceof LoginMessageEntity) {
			for (Channel channel : channels) {
				channel.writeAndFlush(new TextWebSocketFrame(request+"加入了大厅"));
				System.out.println(request+"加入了大厅");
			}
		} else if (msgEntity instanceof ChatMessageEntity) {
			for (Channel channel : channels) {
				if (channel != ctx.channel()) {
					channel.writeAndFlush(new TextWebSocketFrame("[" + ctx.channel().remoteAddress() + "]" + request
							+ " ,欢迎使用netty websocket server服务, 现在时刻:" + new java.util.Date().toString()));
				} else {
					channel.writeAndFlush(new TextWebSocketFrame(
							request + " ,欢迎使用netty websocket server服务, 现在时刻:" + new java.util.Date().toString()));

				}
			}
		}

		System.out.println("send a message to channel: " + request);

	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse resp) {
		// 返回应答给客户端
		if (resp.status() != HttpResponseStatus.OK) {
			ByteBuf buf = Unpooled.copiedBuffer(resp.status().toString(), CharsetUtil.UTF_8);
			resp.content().writeBytes(buf);
			buf.release();
			HttpUtil.setContentLength(resp, resp.content().readableBytes());
		}
		// 如果是非Keep-Alive,关闭连接
		ChannelFuture f = ctx.channel().writeAndFlush(resp);
		if (!HttpUtil.isKeepAlive(req) || resp.status() != HttpResponseStatus.OK) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("read complete");
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("exception caught");
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent stateEvent = (IdleStateEvent) evt;
			PingWebSocketFrame ping = new PingWebSocketFrame();
			switch (stateEvent.state()) {
			// 读空闲（服务器端）
			case READER_IDLE:
				System.out.println("【" + ctx.channel().remoteAddress() + "】读空闲（服务器端）");
				ctx.writeAndFlush(ping);
				break;
			// 写空闲（客户端）
			case WRITER_IDLE:
				System.out.println("【" + ctx.channel().remoteAddress() + "】写空闲（客户端）");
				ctx.writeAndFlush(ping);
				break;
			case ALL_IDLE:
				System.out.println("【" + ctx.channel().remoteAddress() + "】读写空闲");
				break;
			}
		}
	}

	// 有新通道加入的时候响应
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		for (Channel channel : channels) {
			channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));
		}
		channels.add(ctx.channel());
		System.out.println("Client:" + incoming.remoteAddress() + "加入");
	}

}
