package net.cpollet.tproxy.configuration;

import net.cpollet.tproxy.filters.NettyFilterChain;

import java.util.List;

/**
 * @author Christophe Pollet
 */
public interface ProxyConfiguration {
    String name();

    String fromHost();

    int fromPort();

    String toHost();

    int toPort();

    NettyFilterChain filterChain() throws Exception;

    List<? extends FilterConfiguration> filters();
}
