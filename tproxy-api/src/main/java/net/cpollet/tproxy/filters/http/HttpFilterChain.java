package net.cpollet.tproxy.filters.http;

/**
 * @author Christophe Pollet
 */
public interface HttpFilterChain {
    void filter(HttpRequest request);

    void add(HttpFilter filter);
}
