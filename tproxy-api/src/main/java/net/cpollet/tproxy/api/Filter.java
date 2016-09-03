package net.cpollet.tproxy.api;

/**
 * @author Christophe Pollet
 */
public interface Filter {
    Buffer filter(Buffer buffer, FilterChain filterChain) throws Exception;
}
