package net.cpollet.tproxy.configuration.json;

import net.cpollet.tproxy.configuration.FilterConfiguration;
import net.cpollet.tproxy.filters.Filter;
import net.cpollet.tproxy.filters.NettyFilterChain;
import net.cpollet.tproxy.filters.http.HttpRequest;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Christophe Pollet
 */
public class FiltersJsonConfiguration {
    @NotNull
    private String filterChain;
    private List<FilterJsonConfiguration> filters;
    private NettyFilterChain filterChainInstance;

    public NettyFilterChain filterChain() {
        return filterChainInstance;
    }

    public List<? extends FilterConfiguration> filters() {
        return filters;
    }

    public void initialize() throws Exception {
        filterChainInstance = (NettyFilterChain) Class.forName(filterChain).getConstructor().newInstance();
        for (FilterJsonConfiguration filter : filters) {
            filter.initialize();
            filterChainInstance.add(filter.filter());
        }
    }
}
