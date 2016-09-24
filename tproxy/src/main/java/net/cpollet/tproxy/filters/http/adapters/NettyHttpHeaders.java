package net.cpollet.tproxy.filters.http.adapters;

import io.netty.handler.codec.http.HttpHeaders;
import net.cpollet.tproxy.filters.http.HttpRequestHeaders;

/**
 * @author Christophe Pollet
 */
public class NettyHttpHeaders implements HttpRequestHeaders {
    private final HttpHeaders headers;

    public NettyHttpHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public void replace(String name, String value) {
        remove(name);
        add(name, value);
    }

    @Override
    public void remove(String header) {
        headers.remove(header);
    }

    @Override
    public void add(String name, String value) {
        headers.add(name, value);
    }
}
