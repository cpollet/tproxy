package net.cpollet.tproxy.socket;

import net.cpollet.tproxy.api.FilterChain;
import net.cpollet.tproxy.concurrent.FutureCompletionWatcher;
import net.cpollet.tproxy.endpoints.ProxyEndpointsThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Socket;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

/**
 * @author Christophe Pollet
 */
public class ForwardingSocket {
    private static final Logger LOG = LogManager.getLogger();

    private final ProxyEndpointsThread proxyEndpointsThread;
    private final ExecutorCompletionService<Void> executorService;
    private final SocketForwardingRunnable socketForwardingRunnable1;
    private final SocketForwardingRunnable socketForwardingRunnable2;

    public ForwardingSocket(ProxyEndpointsThread proxyEndpointsThread, ExecutorCompletionService<Void> executorService, Socket socket1, Socket socket2, FilterChain filterChain) {
        this.proxyEndpointsThread = proxyEndpointsThread;
        this.executorService = executorService;

        socketForwardingRunnable1 = new SocketForwardingRunnable(this, socket1, socket2, filterChain, SocketForwardingRunnable.Direction.LOCAL_TO_REMOTE);
        socketForwardingRunnable2 = new SocketForwardingRunnable(this, socket2, socket1, filterChain, SocketForwardingRunnable.Direction.REMOTE_TO_LOCAL);
    }

    public void start() {
        Future<Void> future1 = executorService.submit(socketForwardingRunnable1, null);
        Future<Void> future2 = executorService.submit(socketForwardingRunnable2, null);

        executorService.submit(new FutureCompletionWatcher(() -> {
            LOG.debug("Both stream forwarders are dead");
            proxyEndpointsThread.closed(me());
        }, future1, future2), null);
    }

    private ForwardingSocket me() {
        return this;
    }

    public void terminate() {
        socketForwardingRunnable1.terminate();
        socketForwardingRunnable2.terminate();
    }

    public SocketForwardingRunnable getPeer(SocketForwardingRunnable socketForwardingRunnable) {
        if (this.socketForwardingRunnable1.equals(socketForwardingRunnable)) {
            return this.socketForwardingRunnable2;
        }
        return this.socketForwardingRunnable1;
    }
}
