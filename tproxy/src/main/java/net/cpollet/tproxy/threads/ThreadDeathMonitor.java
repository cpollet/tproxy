package net.cpollet.tproxy.threads;

import java.util.List;

/**
 * @author Christophe Pollet
 */
public class ThreadDeathMonitor extends Thread {
    private final List<Thread> threads;
    private final Runnable runnable;


    public ThreadDeathMonitor(List<Thread> threads, Runnable runnable) {
        setName(getName() + "|" + getClass().getSimpleName());
        this.threads = threads;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        threads.forEach(this::joinThread);
        runnable.run();
    }

    void joinThread(Thread thread) {
        while (true) {
            try {
                thread.join();
                return;
            }
            catch (InterruptedException e) {
                // we don't care
            }
        }
    }
}
