package net.cpollet.tproxy.filters.http;

import net.cpollet.tproxy.filters.Filter;
import net.cpollet.tproxy.filters.FilterChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * @author Christophe Pollet
 */
public class HttpHeadersFilter implements Filter<HttpRequest> {
    private static final Logger LOGGER = LogManager.getLogger();

    private final HttpHeadersFilterConfiguration configuration;

    public HttpHeadersFilter(HttpHeadersFilterConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void filter(HttpRequest request, FilterChain<HttpRequest> filterChain) {
        LOGGER.info("Filtering request");

        for (Map.Entry<String, String> replacement : configuration.replacements().entrySet()) {
            request.headers().replace(replacement.getKey(), replacement.getValue());
        }

        filterChain.filter(request);
    }

    @Override
    public void initialize() {
        configuration.initialize();
    }
}
