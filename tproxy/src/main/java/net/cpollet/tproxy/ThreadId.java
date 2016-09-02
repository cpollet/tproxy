package net.cpollet.tproxy;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Christophe Pollet
 */
public class ThreadId {
    private AtomicInteger integer = new AtomicInteger(0);

    public String get() {
        return String.valueOf(integer.getAndIncrement());
    }
}
