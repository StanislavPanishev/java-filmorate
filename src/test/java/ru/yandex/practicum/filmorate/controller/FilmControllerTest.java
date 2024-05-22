package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    InMemoryFilmStorage filmStorage;
    InMemoryUserStorage userStorage;
    Film film;

    @BeforeEach
    void setFilmController() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        film = new Film();
        film.setId(1L);
        film.setName("Иван Васильевич меняет профессию");
        film.setDescription("Fantasy");
        film.setDuration(88);
        film.setLikes(new HashSet<>());
    }

    @Test
    protected void validateFilm() {
        film.setReleaseDate(LocalDate.of(1973, 9, 17));
        filmStorage.validate(film);
    }

    @Test
    protected void validateFilmReleaseDateFail() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        Exception exception = assertThrows(
                ValidationException.class, () -> filmStorage.validate(film)
        );
        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }
}