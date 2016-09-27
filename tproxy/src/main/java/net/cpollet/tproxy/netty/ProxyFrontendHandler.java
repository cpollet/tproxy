package net.cpollet.tproxy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import net.cpollet.tproxy.filters.NettyFilterChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Christophe Pollet
 */
public class ProxyFrontendHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LogManager.getLogger();

    private final String remoteHost;
    private final int remotePort;
    private final NettyFilterChain filterChain;
    private Channel outboundChannel;

    public ProxyFrontendHandler(String remoteHost, int remotePort, NettyFilterChain filterChain) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.filterChain = filterChain;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        filterChain.bind(ctx.pipeline());

        LOG.debug("front[{}]: * <-> 8080 {}", Integer.toHexString(System.identityHashCode(ctx.pipeline())), ctx.pipeline());

        ChannelFuture channelFuture = new Bootstrap()
                .group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(filterChain.backendChannelInitializer(inboundChannel))
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
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
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
