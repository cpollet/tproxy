package net.cpollet.tproxy.filters;

import net.cpollet.tproxy.api.Buffer;
import net.cpollet.tproxy.api.Filter;
import net.cpollet.tproxy.api.FilterChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Christophe Pollet
 */
public class LoggingFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public Buffer filter(Buffer buffer, FilterChain filterChain) throws Exception {
        LOG.info("\n{}", buffer);
        return filterChain.doFilter(buffer);
    }
}
