package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    FilmController filmController;

    @BeforeEach
    void setFilmController() {
        filmController = new FilmController();
    }

    @Test
    void validateFilm() {
        final Film film = new Film(
                1L,
                "Иван Васильевич меняет профессию",
                "Fantasy",
                LocalDate.of(1973, 9, 17),
                Duration.ofMinutes(88));

        filmController.validate(film);
    }

    @Test
    void validateFilmReleaseDateFail() {
        final Film film = new Film(
                1L,
                "Иван Васильевич меняет профессию",
                "Fantasy",
                LocalDate.of(1895, 12, 27),
                Duration.ofMinutes(88));

        Exception exception = assertThrows(
                ValidationException.class, () -> filmController.validate(film)
        );

        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());

    }
}