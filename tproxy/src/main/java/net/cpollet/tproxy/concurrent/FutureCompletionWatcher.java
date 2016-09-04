package net.cpollet.tproxy.concurrent;

import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * @author Christophe Pollet
 */
public class FutureCompletionWatcher implements Runnable {
    private final Runnable runnable;
    private final Future<?>[] futures;

    public FutureCompletionWatcher(Runnable runnable, Future<?>... futures) {
        this.runnable = runnable;
        this.futures = futures;
    }

    @Override
    public void run() {
        while (!allFuturesCompleted()) {
            sleep();
        }
        runnable.run();
    }

    private boolean allFuturesCompleted() {
        return Stream.of(futures).map(Future::isDone).allMatch(e -> e);
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            // don't care
        }
    }
}
