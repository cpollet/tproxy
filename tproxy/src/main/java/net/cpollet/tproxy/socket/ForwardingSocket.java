package net.cpollet.tproxy.socket;

import net.cpollet.tproxy.endpoints.ProxyEndpointsThread;
import net.cpollet.tproxy.api.FilterChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;

/**
 * @author Christophe Pollet
 */
public class ForwardingSocket {
    private static final Logger LOG = LogManager.getLogger();

    private final ProxyEndpointsThread proxyEndpointsThread;
    private final SocketForwardingThread socketForwardingThread1;
    private final SocketForwardingThread socketForwardingThread2;

    public ForwardingSocket(int id, ProxyEndpointsThread proxyEndpointsThread, Socket socket1, Socket socket2, FilterChain filterChain) {
        this.proxyEndpointsThread = proxyEndpointsThread;

        socketForwardingThread1 = new SocketForwardingThread(id + ".0", this, socket1, socket2, filterChain, SocketForwardingThread.Direction.LOCAL_TO_REMOTE);
        socketForwardingThread2 = new SocketForwardingThread(id + ".1", this, socket2, socket1, filterChain, SocketForwardingThread.Direction.REMOTE_TO_LOCAL);
    }

    public void start() {
        synchronized (proxyEndpointsThread.getLockObject()) {
            socketForwardingThread2.start();
            socketForwardingThread1.start();
        }

        final ForwardingSocket self = this;

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        socketForwardingThread1.join();
                        socketForwardingThread2.join();
                        break;
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                LOG.info("both copier died");
                proxyEndpointsThread.closed(self);
            }
        }.start();
    }

    public void close() {
        socketForwardingThread1.interrupt();
        socketForwardingThread2.interrupt();
    }

    public Object getLockObject() {
        return proxyEndpointsThread.getLockObject();
    }

    public SocketForwardingThread getPeer(SocketForwardingThread socketForwardingThread) {
        if (this.socketForwardingThread1.equals(socketForwardingThread)) {
            return this.socketForwardingThread2;
        }
        return this.socketForwardingThread1;
    }
}
