package zad1.exception.checked;

public class ConnectionClosedException extends Exception {
    public ConnectionClosedException(String message) {
        super(message);
    }
}
