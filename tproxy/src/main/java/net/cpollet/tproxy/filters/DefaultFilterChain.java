package net.cpollet.tproxy.filters;

import net.cpollet.tproxy.api.Buffer;
import net.cpollet.tproxy.api.Filter;
import net.cpollet.tproxy.api.FilterChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * @author Christophe Pollet
 */
public class DefaultFilterChain implements FilterChain {
    private static final Logger LOG = LogManager.getLogger();

    private final List<Filter> filters;
    private final ThreadLocal<Iterator<Filter>> currentFilter;

    public DefaultFilterChain(List<Filter> filters) {
        this.filters = filters;
        this.currentFilter = ThreadLocal.withInitial(filters::iterator);
    }

    @Override
    public Buffer doFilter(Buffer buffer) throws Exception {
        Iterator<Filter> iterator = currentFilter.get();

        if (!iterator.hasNext()) {
            currentFilter.remove();
            return buffer;
        }

        return iterator.next().filter(buffer, this);
    }
}
