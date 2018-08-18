package com.chenfq.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

// 1. 定义服务端的处理器Handler
public class SimpleChatServerHandler extends SimpleChannelInboundHandler {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 2. 每当服务端接收到新的客户端连接时，将客户端的channel存入channelGroup中并告诉别的客户端channel
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();

        //广播消息对其他channel
        channels.writeAndFlush("[SERVER] - " +incoming.remoteAddress() +" 加入 \n");
        channels.add(ctx.channel());
    }

    // 3. 当客户端断开时，自动从channelGroup中移除，通知其他channel
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        channels.writeAndFlush("[SERVER] - "+ incoming.remoteAddress() + "离开 \n");
    }

    // 4.每当服务端读到客户端写的信息时，将信息转发给其他客户端的channel
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

        Channel incoming = channelHandlerContext.channel();
        for(Channel channel : channels){
            if (channel != incoming){
                channel.writeAndFlush("["+ channel.remoteAddress() +"]" + o + "\n");
            }else {
                channel.writeAndFlush("[you]" + o +"\n");
            }
        }
    }

    // 5. 服务端监听客户端活动
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient: "+incoming.remoteAddress()+"OnLine");
    }

    // 6. 服务端监听客户端channel断开
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("SimpleChatClient: " +ctx.channel().remoteAddress()+"OffLine");
    }

    // 7. 当出现Throwable对象才会被调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("SimpleChatClient: " + ctx.channel().remoteAddress()+"Exception ");
        cause.printStackTrace();
        ctx.close();
    }
}
