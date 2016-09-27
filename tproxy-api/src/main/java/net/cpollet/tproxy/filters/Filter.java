package net.cpollet.tproxy.filters;

/**
 * @author Christophe Pollet
 */
public interface Filter<R> {
    void filter(R request, FilterChain<R> filterChain);

    void initialize();
}
