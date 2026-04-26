package zad1.server;

import zad1.server.Operation;

public record Message(Operation operation, String message) {
    @Override
    public String toString() {
        return "Message: %s %s".formatted(operation, message);
    }
}
