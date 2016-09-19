package net.cpollet.tproxy.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author Christophe Pollet
 */
public class ForwardingHandler extends ChannelInitializer<SocketChannel> {
    private final String remoteHost;
    private final int remotePort;

    public ForwardingHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(
                new LoggingHandler(),
                new ProxyFrontendHandler(remoteHost, remotePort)
        );
    }
}
