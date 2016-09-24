package net.cpollet.tproxy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import net.cpollet.tproxy.filters.http.HttpHeadersFilter;
import net.cpollet.tproxy.filters.http.adapters.NettyHttpFilterChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Christophe Pollet
 */
public class ProxyFrontendHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LogManager.getLogger();

    private final String remoteHost;
    private final int remotePort;
    private Channel outboundChannel;

    public ProxyFrontendHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LOG.info("Forwarding to {}:{}", remoteHost, remotePort);
        final Channel inboundChannel = ctx.channel();

        NettyHttpFilterChain httpFilterChain = new NettyHttpFilterChain();
        httpFilterChain.add(new HttpHeadersFilter());
        httpFilterChain.installFrontEnd(ctx.pipeline());

        LOG.debug("front[{}]: * <-> 8080 {}", Integer.toHexString(System.identityHashCode(ctx.pipeline())), ctx.pipeline());

        ChannelFuture channelFuture = new Bootstrap()
                .group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(httpFilterChain.backendChannelInitializer(inboundChannel))
                .option(ChannelOption.AUTO_READ, false)
                .connect(remoteHost, remotePort);

        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                inboundChannel.read();
            } else {
                // Close the connection if the connection attempt has failed.
                inboundChannel.close();
            }
        });

        outboundChannel = channelFuture.channel();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("channelUnregistered");
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("channelReadComplete");
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        LOG.debug("channelRead");
        if (outboundChannel.isActive()) {
            outboundChannel
                    .writeAndFlush(msg)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            // was able to flush out data, start to read the next chunk
                            ctx.channel().read();
                        } else {
                            future.channel().close();
                        }
                    });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    private void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }
}