package zad1.server;

import zad1.exception.checked.InvalidMessageFormatException;

import java.util.Optional;

class MessageValidator {

    private static final String DELIMITER = ":";

    static Message validateMessageFormat(String message) throws InvalidMessageFormatException {
        if (message.isBlank())
            throw new InvalidMessageFormatException("Empty message!", message);

        var split = message.split(DELIMITER, 2);
        if (split.length != 2)
            throw new InvalidMessageFormatException("Expected ':' character. Expected format: <operation>:<message>", message);

        Optional<Operation> operation = Operation.map(split[0]);
        if (operation.isEmpty())
            throw new InvalidMessageFormatException("Unsupported operation", message);

        return new Message(operation.get(), split[1]);
    }
}
