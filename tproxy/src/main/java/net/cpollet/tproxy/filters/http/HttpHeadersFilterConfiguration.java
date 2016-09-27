package net.cpollet.tproxy.filters.http;

import java.util.Map;

/**
 * @author Christophe Pollet
 */
public interface HttpHeadersFilterConfiguration {
    Map<String, String> replacements();

    void initialize();
}
