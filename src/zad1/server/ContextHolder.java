package zad1.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ContextHolder {
    private final ConcurrentHashMap<String, ClientSession> context;

    public ContextHolder() {
        context = new ConcurrentHashMap<>();
    }

    void add(String id, Socket socket) throws IOException {
        context.put(id, new ClientSession(socket));
    }

    void forget(String id) {
        context.remove(id);
    }

    Collection<ClientSession> getSessions() {
        return context.values();
    }
}
