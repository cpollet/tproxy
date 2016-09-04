package net.cpollet.tproxy.stream;

import net.cpollet.tproxy.ProxyThread;
import net.cpollet.tproxy.StreamCopyThread;
import net.cpollet.tproxy.api.FilterChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;

/**
 * @author Christophe Pollet
 */
public class ForwardingSocket {
    private static final Logger LOG = LogManager.getLogger();

    private final ProxyThread proxyThread;
    private final StreamCopyThread streamCopyThread1;
    private final StreamCopyThread streamCopyThread2;

    public ForwardingSocket(int id, ProxyThread proxyThread, Socket socket1, Socket socket2, FilterChain filterChain) {
        this.proxyThread = proxyThread;

        streamCopyThread1 = new StreamCopyThread(id + ".0", this, socket1, socket2, filterChain, StreamCopyThread.Direction.LOCAL_TO_REMOTE);
        streamCopyThread2 = new StreamCopyThread(id + ".1", this, socket2, socket1, filterChain, StreamCopyThread.Direction.REMOTE_TO_LOCAL);
    }

    public void start() {
        synchronized (proxyThread.getLockObject()) {
            streamCopyThread2.start();
            streamCopyThread1.start();
        }

        final ForwardingSocket self = this;

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        streamCopyThread1.join();
                        streamCopyThread2.join();
                        break;
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                LOG.info("both copier died");
                proxyThread.closed(self);
            }
        }.start();
    }

    public void close() {
        streamCopyThread1.interrupt();
        streamCopyThread2.interrupt();
    }

    public Object getLockObject() {
        return proxyThread.getLockObject();
    }

    public StreamCopyThread getPeer(StreamCopyThread streamCopyThread) {
        if (this.streamCopyThread1.equals(streamCopyThread)) {
            return this.streamCopyThread2;
        }
        return this.streamCopyThread1;
    }
}
