package ru.yandex.practicum.filmorate.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    public Map<String, String> handleIncorrectArgument(final IncorrectArgumentException e) {
        log.info("IllegalArgumentException {}", e.getMessage());
        return Map.of("Передан неверный аргумент", e.getMessage());
    }

    @ExceptionHandler
    public Map<String, String> handleError(final RuntimeException e) {
        log.info("RuntimeException {}", e.getMessage());
        return Map.of("Произошла ошибка!", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationError(final ValidationException e) {
        log.info("error 400 {}", e.getMessage());
        return Map.of("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundError(final NotFoundException e) {
        log.info("error 404 {}", e.getMessage());
        return Map.of("Ресурс не найден", e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handlerPositiveCountError(ConstraintViolationException e) {
        log.debug("Получен статус 404 Not found {}", e.getMessage(), e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}