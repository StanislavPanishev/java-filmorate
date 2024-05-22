package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {
    InMemoryUserStorage userStorage;
    User user;

    @BeforeEach
    void setFilmController() {
        userStorage = new InMemoryUserStorage();
        user = new User();
        user.setId(1L);
        user.setEmail("bunsha1973@mail.ru");
        user.setLogin("Бунша1973");
        user.setName("Иван Васильевич Бунша");
        user.setBirthday(LocalDate.of(1973, 9, 17));
        user.setFriends(new HashSet<>());
    }

    @Test
    protected void validateUser() {
        userStorage.validate(user);
    }


    @Test
    protected void validateUserLoginFail() {
        user.setLogin("Бунша 1973");
        Exception exception = assertThrows(
                ValidationException.class, () -> userStorage.validate(user));
        assertEquals("Логин не должен содержать пробелы", exception.getMessage());
    }


    @Test
    protected void validateUserNameIsNull() {
        user.setLogin("Бунша1973");
        user.setName(null);
        userStorage.validate(user);
        assertEquals("Бунша1973", user.getName());
    }
}