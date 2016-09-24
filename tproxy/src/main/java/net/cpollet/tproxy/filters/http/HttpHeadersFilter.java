package net.cpollet.tproxy.filters.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Christophe Pollet
 */
public class HttpHeadersFilter implements HttpFilter {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void filter(HttpRequest request, HttpFilterChain filterChain) {
        LOGGER.info("Filtering request");

        request.headers().replace("Host", "example.com");
        //request.headers().remove("Accept-Encoding");
        //request.headers().remove("Connection");

        filterChain.filter(request);
    }
}
