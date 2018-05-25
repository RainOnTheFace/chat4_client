package com.cn.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.cn.client.swing.Swingclient;
import com.cn.common.core.codc.RequestEncoder;
import com.cn.common.core.codc.ResponseDecoder;
import com.cn.common.core.model.Request;

/**
 * netty客户端入门
 * 
 * @author -琴兽-
 * 
 */
@Component
public class Client {

	public static Logger log = Logger.getLogger(Client.class);
	/**
	 * 界面
	 */
	@Autowired
	private Swingclient swingclient;

	/**
	 * 服务类
	 */
	Bootstrap bootstrap = new Bootstrap();

	/**
	 * 会话
	 */
	private Channel channel;

	/**
	 * 线程池
	 */
	private EventLoopGroup workerGroup = new NioEventLoopGroup();

	/**
	 * 初始化
	 * 
	 * 被@PostConstruct修饰的方法会在服务器加载Servle的时候运行，并且只会被服务器执行一次。
	 * PostConstruct在构造函数之后执行,init()方法之前执行。PreDestroy（）方法在destroy()方法执行执行之后执行
	 * 
	 * 注解多少会影响服务器的启动速度。服务器在启动的时候，会遍历Web应用的WEB-INF/classes下的所有class文件与WEB-INF/
	 * lib下的所有jar文件，以检查哪些类使用了注解。如果程序中没有使用任何注解，可以在web.xml中设置
	 * <web-app>的metadatacomplete属性为true来关掉服务器启动时的例行检查。
	 */
	@PostConstruct
	public void init() {
		log.info("*****Client  init 初始化");

		// 设置循环线程组事例
		bootstrap.group(workerGroup);

		// 设置channel工厂
		bootstrap.channel(NioSocketChannel.class);

		// 设置管道
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ResponseDecoder());
				ch.pipeline().addLast(new RequestEncoder());
				ch.pipeline().addLast(new ClientHandler(swingclient));
			}
		});
	}

	/**
	 * 连接
	 * 
	 * @param ip
	 * @param port
	 * @throws InterruptedException
	 */
	public void connect() throws InterruptedException {

		// 连接服务端
		ChannelFuture connect = bootstrap.connect(new InetSocketAddress("127.0.0.1", 10102));
		connect.sync();
		channel = connect.channel();
	
	}

	/**
	 * 关闭
	 */
	public void shutdown() {
		workerGroup.shutdownGracefully();
	}

	/**
	 * 获取会话
	 * 
	 * @return
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * 发送消息
	 * 
	 * @param request
	 * @throws InterruptedException
	 */
	public void sendRequest(Request request) throws InterruptedException {
		if (channel == null || !channel.isActive()) {
			log.info("*****未连接服务器，连接");
			connect();
		}else{
			log.info("*****已连接服务器");
		}
		channel.writeAndFlush(request);
	}

}
