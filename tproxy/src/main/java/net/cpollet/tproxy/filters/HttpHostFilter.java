package net.cpollet.tproxy.filters;

import net.cpollet.tproxy.DefaultBuffer;
import net.cpollet.tproxy.api.Buffer;
import net.cpollet.tproxy.api.Filter;
import net.cpollet.tproxy.api.FilterChain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Christophe Pollet
 */
public class HttpHostFilter implements Filter {
    @Override
    public Buffer filter(Buffer buffer, FilterChain filterChain) throws Exception {
        String content = buffer.toString();

        Pattern pattern = Pattern.compile("\r\nHost:.*\r\n");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            content = matcher.replaceFirst("\r\nHost: example.com\r\n");
            return filterChain.doFilter(new DefaultBuffer(content));
        }

        return filterChain.doFilter(buffer);
    }
}
