package net.cpollet.tproxy.configuration.json;

import net.cpollet.tproxy.configuration.FilterConfiguration;
import net.cpollet.tproxy.configuration.ProxyConfiguration;
import net.cpollet.tproxy.filters.NettyFilterChain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @author Christophe Pollet
 */
public class ProxyJsonConfiguration implements ProxyConfiguration {
    private String name;

    @NotNull
    @Pattern(regexp = "[a-zA-Z0-9.-]+:[0-9]+")
    private String from;

    @NotNull
    @Pattern(regexp = "[a-zA-Z0-9.-]+:[0-9]+")
    private String to;

    private FiltersJsonConfiguration filters;

    @Override
    public String name() {
        return name;
    }

    @Override
    public String fromHost() {
        return host(from);
    }

    private String host(String hostAndPort) {
        return hostAndPort.split(":")[0];
    }

    @Override
    public int fromPort() {
        return port(from);
    }

    private int port(String hostAndPort) {
        return Integer.parseInt(hostAndPort.split(":")[1]);
    }

    @Override
    public String toHost() {
        return host(to);
    }

    @Override
    public int toPort() {
        return port(to);
    }

    @Override
    public NettyFilterChain filterChain() throws Exception {
        return filters.filterChain();
    }

    @Override
    public List<? extends FilterConfiguration> filters() {
        return filters.filters();
    }

    public void initialize() throws Exception {
        filters.initialize();
    }
}
