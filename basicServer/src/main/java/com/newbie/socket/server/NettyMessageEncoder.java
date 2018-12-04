package com.newbie.socket.server;

import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class NettyMessageEncoder extends MessageToMessageEncoder<NettyMessage>{

	NettyMarshallingEncoder encoder;
	
	public NettyMessageEncoder() {
		this.encoder = MarshallingCodecFactory.buildMarshallingEncoder();
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, NettyMessage msg, List<Object> out) throws Exception {
		if(msg == null || msg.getHeader() == null){
			throw new Exception("The encode message is null");
		}
		
		ByteBuf sendBuf = Unpooled.buffer();
		sendBuf.writeInt(msg.getHeader().getCrcCode());
		sendBuf.writeInt(msg.getHeader().getLength());
		sendBuf.writeLong(msg.getHeader().getSessionId());
		sendBuf.writeByte(msg.getHeader().getType());
		sendBuf.writeByte(msg.getHeader().getPriority());
		sendBuf.writeInt(msg.getHeader().getAttachment().size());
		
		String key = null;
		byte[] keyArray = null;
		Object value = null;
		for(Map.Entry<String, Object> param: msg.getHeader().getAttachment().entrySet()){
			key = param.getKey();
			keyArray = key.getBytes("UTF-8");
			sendBuf.writeInt(keyArray.length);
			sendBuf.writeBytes(keyArray);
			value = param.getValue();
			encoder.encode(ctx, value, sendBuf);
		}
		key = null;
		keyArray = null;
		value = null;
		if(msg.getBody() != null){
			encoder.encode(ctx,msg.getBody(), sendBuf);
		}else {
			
			//sendBuf.writeInt(0);
			// 在第4个字节出写入Buffer的长度
			
		}

		int readableBytes = sendBuf.readableBytes();
		sendBuf.setInt(4, readableBytes-8);
		// 把Message添加到List传递到下一个Handler 
		out.add(sendBuf);

	}

}
