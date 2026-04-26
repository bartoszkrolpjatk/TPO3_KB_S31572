package zad1.server;

import zad1.Log;
import zad1.exception.checked.InvalidMessageFormatException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.locks.Lock;

public class ClientHandler {

    private final Socket socket;
    private final ContextHolder context;
    private final Log serverLog;
    private final Lock lock;
    private String clientId;

    public ClientHandler(Socket socket, ContextHolder context, Log serverLog, Lock lock) {
        this.socket = socket;
        this.context = context;
        this.serverLog = serverLog;
        this.lock = lock;
        try {
            socket.setSoTimeout(30000);
            socket.setTcpNoDelay(true);
        } catch (SocketException e) {
            System.out.printf("Exception while configuring the socket, cause %s. Continuing with default socket setting\n", e.getMessage());
        }
    }

    public void handle() {
        try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                try {
                    var message = MessageValidator.validateMessageFormat(line);
                    switch (message.operation()) {
                        case HI -> loginClient(message.message());
                        case SEND -> broadcast("%s: %s".formatted(clientId, message.message()));
                        case BYE -> {
                            logoutClient();
                            return;
                        }
                        case EVENT -> System.err.println("Client can not broadcast messages. Skipping...");
                    }
                } catch (InvalidMessageFormatException e) {
                    System.err.printf("Wrong message format: %s. Cause: %s. Skipping...\n", e.invalidMessage(), e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.printf("Cannot obtain input stream. Closing the socket... Cause %s\n", e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                System.err.printf("Error while closing the socket. Cause: %s", ex.getMessage());
            }
            if (clientId != null)
                context.forget(clientId);
        }
    }

    private void logoutClient() {
        broadcast("%s logged out".formatted(clientId));
        try {
            socket.close();
            context.forget(clientId);
        } catch (IOException e) {
            System.err.printf("Cannot close client socket for %s. Client will remain logged in. Cause %s\n", clientId, e.getMessage());
        }
    }

    private void loginClient(String id) {
        lock.lock();
        try {
            context.add(id, socket);
            this.clientId = id;
            broadcast("%s logged in".formatted(id));
        } catch (IOException e) {
            System.err.printf("Cannot save client to context. Client will remain not logged in. Cause: %s\n", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private void broadcast(String message) {
        lock.lock();
        try {
            serverLog.log(message, true);
            for (ClientSession session : context.getSessions()) {
                session.out().println(message);
            }
        } finally {
            lock.unlock();
        }
    }
}
