/**
 *
 *  @author Król Bartosz s31572
 *
 */

package zad1;

import zad1.exception.unchecked.InternalServerException;
import zad1.server.ClientHandler;
import zad1.server.ContextHolder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatServer {
    private static final int DEFAULT_BACKLOG = 100;

    private final ExecutorService executorService;
    private final Thread acceptingThread;
    private final Log log;
    private final ServerSocket serverSocket;

    public ChatServer(int port) {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        var context = new ContextHolder();
        this.log = new Log();
        try {
            this.serverSocket = new ServerSocket(port, DEFAULT_BACKLOG);
        } catch (IOException e) {
            throw new InternalServerException("Exception while creating ServerSocket", e);
        }
        this.acceptingThread = Thread.ofVirtual()
                .unstarted(() -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Socket socket = serverSocket.accept();
                            executorService.execute(() -> new ClientHandler(socket, context, log).handle());
                        } catch (IOException ignored) {}
                    }
                });
    }

    public void startServer() {
        acceptingThread.start();
        System.out.println("Server started");
    }

    public void stopServer() {
        executorService.shutdown();
        try {
            var executorClosed = executorService.awaitTermination(1, TimeUnit.SECONDS);
            if (!executorClosed)
                executorService.shutdownNow();
        } catch (InterruptedException e) {
            System.out.printf("Exception while awaiting executor termination cause: %s. Stopping the server brutally...", e.getMessage());
            executorService.shutdownNow();
        }
        acceptingThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new InternalServerException("Exception while closing server socket", e);
        }
        log.log("ChatServer: chat closed", true);
        System.out.println("Server stopped");
    }

    public String getServerLog() {
        return log.getLog();
    }
}  
