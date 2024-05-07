package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {
    static UserController userController = new UserController();

    @Test
    void validateUser() {
        final User validUser = new User(
                1L,
                "bunsha1973@mail.ru",
                "Бунша1973",
                "Иван Васильевич Бунша",
                LocalDate.of(1973, 9, 17));

        userController.validate(validUser);
    }


    @Test
    void validateUserLoginFail() {
        final User validUser = new User(
                1L,
                "bunsha1973@mail.ru",
                "Бунша 1973",
                "Иван Васильевич Бунша",
                LocalDate.of(1973, 9, 17));

        Exception exception = assertThrows(
                ValidationException.class, () -> userController.validate(validUser)
        );

        assertEquals("Логин не должен содержать пробелы", exception.getMessage());

    }


    @Test
    void validateUserNameIsNull() {
        final User validUser = new User(
                1L,
                "bunsha1973@mail.ru",
                "Бунша1973",
                null,
                LocalDate.of(1973, 9, 17));

        userController.validate(validUser);

        assertEquals("Бунша1973", validUser.getName());

    }
}