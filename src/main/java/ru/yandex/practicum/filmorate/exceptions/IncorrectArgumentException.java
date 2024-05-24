package ru.yandex.practicum.filmorate.exceptions;

public class IncorrectArgumentException extends IllegalArgumentException {
    public IncorrectArgumentException(String message) {
        super(message);
    }
}