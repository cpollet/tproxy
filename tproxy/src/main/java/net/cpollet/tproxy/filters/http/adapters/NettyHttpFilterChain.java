package net.cpollet.tproxy.filters.http.adapters;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import net.cpollet.tproxy.filters.http.HttpFilter;
import net.cpollet.tproxy.filters.http.HttpFilterChain;
import net.cpollet.tproxy.filters.http.HttpRequest;
import net.cpollet.tproxy.netty.ProxyBackendHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Christophe Pollet
 */
public class NettyHttpFilterChain implements HttpFilterChain {
    private final List<HttpFilter> filters;
    private final ThreadLocal<Iterator<HttpFilter>> filter;

    public NettyHttpFilterChain() {
        this.filters = new ArrayList<>();
        this.filter = new ThreadLocal<>();
    }

    @Override
    public void filter(HttpRequest request) {
        if (filter.get() == null) {
            filter.set(filters.iterator());
        }

        if (filter.get().hasNext()) {
            filter.get().next().filter(request, this);
        }

        filter.remove();
    }

    @Override
    public void add(HttpFilter filter) {
        filters.add(filter);
    }

    public void installFrontEnd(ChannelPipeline pipeline) {
        pipeline.addFirst("http.filterChain", new Adapter(this));
        pipeline.addFirst("http.server", new HttpServerCodec());
        pipeline.addFirst("http.aggregator", new HttpObjectAggregator(512 * 1024));
    }

    public ChannelInitializer backendChannelInitializer(Channel inboundChannel) {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline().addLast("http.client", new HttpClientCodec());
                channel.pipeline().addLast("backend", new ProxyBackendHandler(inboundChannel));
            }
        };
    }

    /**
     * @author Christophe Pollet
     */
    private class Adapter extends ChannelInboundHandlerAdapter {
        private final NettyHttpFilterChain filterChain;

        private Adapter(NettyHttpFilterChain filterChain) {
            this.filterChain = filterChain;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof io.netty.handler.codec.http.HttpRequest) {//could be HttpContent as well
                NettyHttpRequest httpRequest = new NettyHttpRequest((io.netty.handler.codec.http.HttpRequest) msg);

                filterChain.filter(httpRequest);
            }

            super.channelRead(ctx, msg);
        }
    }
}
