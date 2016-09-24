package net.cpollet.tproxy.filters.http;

/**
 * @author Christophe Pollet
 */
public interface HttpFilter {
    void filter(HttpRequest request, HttpFilterChain filterChain);
}
