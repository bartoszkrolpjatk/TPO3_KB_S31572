package zad1.exception.checked;

public class InvalidMessageFormatException extends Exception {
    private final String invalidMessage;

    public InvalidMessageFormatException(String message, String invalidMessage) {
        super(message);
        this.invalidMessage = invalidMessage;
    }

    public String invalidMessage() {return invalidMessage;}
}
