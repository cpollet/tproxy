package net.cpollet.tproxy.socket;

import net.cpollet.tproxy.api.FilterChain;
import net.cpollet.tproxy.endpoints.ProxyEndpointsThread;
import net.cpollet.tproxy.threads.ThreadDeathMonitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;
import java.util.Arrays;

/**
 * @author Christophe Pollet
 */
public class ForwardingSocket {
    private static final Logger LOG = LogManager.getLogger();

    private final ProxyEndpointsThread proxyEndpointsThread;
    private final SocketForwardingThread socketForwardingThread1;
    private final SocketForwardingThread socketForwardingThread2;

    public ForwardingSocket(ProxyEndpointsThread proxyEndpointsThread, Socket socket1, Socket socket2, FilterChain filterChain) {
        this.proxyEndpointsThread = proxyEndpointsThread;

        socketForwardingThread1 = new SocketForwardingThread(this, socket1, socket2, filterChain, SocketForwardingThread.Direction.LOCAL_TO_REMOTE);
        socketForwardingThread2 = new SocketForwardingThread(this, socket2, socket1, filterChain, SocketForwardingThread.Direction.REMOTE_TO_LOCAL);
    }

    public void start() {
        socketForwardingThread2.start();
        socketForwardingThread1.start();

        new ThreadDeathMonitor(Arrays.asList(socketForwardingThread1, socketForwardingThread2), () -> {
            LOG.debug("Both stream forwarders are dead");
            proxyEndpointsThread.closed(me());
        }).start();
    }

    private ForwardingSocket me() {
        return this;
    }

    public void close() {
        socketForwardingThread1.interrupt();
        socketForwardingThread2.interrupt();
    }

    public SocketForwardingThread getPeer(SocketForwardingThread socketForwardingThread) {
        if (this.socketForwardingThread1.equals(socketForwardingThread)) {
            return this.socketForwardingThread2;
        }
        return this.socketForwardingThread1;
    }
}
