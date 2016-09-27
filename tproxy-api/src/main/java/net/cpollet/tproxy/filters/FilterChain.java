package net.cpollet.tproxy.filters;

/**
 * @author Christophe Pollet
 */
public interface FilterChain<R> {
    void filter(R request);

    void add(Filter<R> filter);
}
