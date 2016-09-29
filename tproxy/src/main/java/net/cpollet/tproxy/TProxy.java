package net.cpollet.tproxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import net.cpollet.tproxy.configuration.Configuration;
import net.cpollet.tproxy.configuration.ProxyConfiguration;
import net.cpollet.tproxy.configuration.json.JsonConfiguration;
import net.cpollet.tproxy.netty.ProxyFrontendHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TProxy {
    private final static Logger LOG = LogManager.getLogger();
    private final Configuration configuration;

    private TProxy(Configuration configuration) {
        this.configuration = configuration;
    }

    public static void main(String[] args) {
        LOG.info("Starting TProxy...");

        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java -jar tproxy.jar CONFIG_FILE");
        }

        try {
            TProxy tproxy = new TProxy(
                    new JsonConfiguration(args[0])
            );

            tproxy.run();
        }
        catch (Exception e) {
            LOG.error("An error occurred: {}", e.getMessage(), e);
        }
    }

    private void run() throws Exception {
        configuration.load();
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        List<ChannelFuture> channelFutures = new ArrayList<>();

        try {
            for (final ProxyConfiguration proxyConfiguration : configuration.proxies()) {
                channelFutures.add(
                        new ServerBootstrap()
                                .group(bossGroup, workerGroup)
                                .channel(NioServerSocketChannel.class)
                                .handler(new LoggingHandler())
                                .childHandler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel channel) throws Exception {
                                        channel.config().setAutoRead(false);
                                        channel.pipeline().addLast("log", new LoggingHandler());
                                        channel.pipeline().addLast("front", new ProxyFrontendHandler(
                                                proxyConfiguration.toHost(),
                                                proxyConfiguration.toPort(),
                                                proxyConfiguration.filterChain()));
                                    }
                                })
                                .bind(proxyConfiguration.fromPort())
                                .sync()
                                .channel()
                                .closeFuture()
                );
            }

            for (ChannelFuture channelFuture : channelFutures) {
                channelFuture.sync();
            }
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
