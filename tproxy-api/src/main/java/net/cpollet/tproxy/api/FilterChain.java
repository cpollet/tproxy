package net.cpollet.tproxy.api;

/**
 * @author Christophe Pollet
 */
public interface FilterChain {
    Buffer doFilter(Buffer buffer) throws Exception;
}
