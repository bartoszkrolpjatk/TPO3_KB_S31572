package zad1.exception.checked;

public class ClientNotLoggedInException extends Exception {
    public ClientNotLoggedInException(String message) {
        super(message);
    }
}
