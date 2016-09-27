package net.cpollet.tproxy.configuration;

import net.cpollet.tproxy.filters.Filter;

/**
 * @author Christophe Pollet
 */
public interface FilterConfiguration {
    Filter<?> filter() throws Exception;
}
