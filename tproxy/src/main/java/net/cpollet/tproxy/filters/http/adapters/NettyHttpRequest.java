package net.cpollet.tproxy.filters.http.adapters;

import io.netty.handler.codec.http.HttpRequest;
import net.cpollet.tproxy.filters.http.HttpRequestHeaders;

/**
 * @author Christophe Pollet
 */
public class NettyHttpRequest implements net.cpollet.tproxy.filters.http.HttpRequest {
    private final HttpRequest request;

    public NettyHttpRequest(HttpRequest request) {
        this.request = request;
    }

    @Override
    public HttpRequestHeaders headers() {
        return new NettyHttpHeaders(request.headers());
    }
}
