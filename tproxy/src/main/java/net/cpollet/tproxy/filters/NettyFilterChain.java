package net.cpollet.tproxy.filters;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

/**
 * @author Christophe Pollet
 */
public interface NettyFilterChain<R> extends FilterChain<R> {
    void bind(ChannelPipeline pipeline);

    ChannelInitializer backendChannelInitializer(Channel inboundChannel);
}
