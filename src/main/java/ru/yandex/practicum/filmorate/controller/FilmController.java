package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    // Получение всех фильмов.
    @GetMapping
    public Collection<Film> findAllFilms() {
        return films.values();
    }

    // Добавление фильма
    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        validate(film);
        // формируем дополнительные данные
        film.setId(getNextId());
        log.info("Добавлен фильм с id={}", film.getId());
        // сохраняем новую публикацию в памяти приложения
        films.put(film.getId(), film);
        return film;
    }

    // вспомогательный метод для генерации идентификатора нового фильма
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    // Обновление фильма
    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        // проверяем необходимые условия
        validate(film);
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("film c id=" + film.getId() + " не найден");
        }
        log.info("Изменен фильм с id={}", film.getId());
        films.put(film.getId(), film);
        return films.get(film.getId());
    }

    public void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }
}
