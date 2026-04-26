package zad1.server;

import java.util.Arrays;
import java.util.Optional;

public enum Operation {
    HI,
    BYE,
    SEND,
    EVENT;

    static Optional<Operation> map(String operation) {
        try {
            return Optional.of(Operation.valueOf(operation.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
