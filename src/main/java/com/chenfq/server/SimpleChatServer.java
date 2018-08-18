package com.chenfq.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class SimpleChatServer {

    private int port;

    public SimpleChatServer(int port) {
        this.port = port;
    }

    public void run()throws Exception{

        // 1. NioEventLoopGroup 用来处理I/O操作的多线程事件循环器，boss用来接收连接，work用来处理连接（读写socket）
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try{
            // 2. ServerBootStrap 启动Nio服务的辅助启动类
            ServerBootstrap server = new ServerBootstrap();
            server.group(boosGroup,workGroup)
                    .channel(NioServerSocketChannel.class)  //3. 指定该serverBootStrap的channel为NioServerSocketChannel类型
                    .childHandler(new SimpleChatServerInitializer()) // 4.这里的事件处理类用来处理已经接收的channel
                    .option(ChannelOption.SO_BACKLOG,128) // 5.设置制定的channel的配置参数
                    .childOption(ChannelOption.SO_KEEPALIVE,true);// 6.
            System.out.println("SimpleChatServer 启动了");

            //绑定端口，开始接收进来的连接
            ChannelFuture f = server.bind(port).sync(); // 7.绑定端口同步的启动服务

            //等待服务器socket关闭
            f.channel().closeFuture().sync();

        }finally {
            workGroup.shutdownGracefully();
            boosGroup.shutdownGracefully();
            System.out.println("SimpleChatServer 关闭了");
        }
    }

    public static void main(String[] args) throws Exception{
        int port = 8080;
        new SimpleChatServer(port).run();
    }
}
