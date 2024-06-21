package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    // Получение всех фильмов.
    @GetMapping
    public Collection<Film> findAllFilms() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable long id) {
        return filmService.get(id);
    }

    // Добавление фильма
    @PostMapping
    //@ResponseStatus(HttpStatus.OK)
    public Film create(@Valid @RequestBody Film film) {
        log.info("Добавлен фильм с id={}", film.getId());
        // сохраняем новую публикацию в памяти приложения
        filmService.create(film);
        return film;
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable long id) {
        filmService.delete(id);
        log.info("Удален фильм с id {}", id);
    }

    // Обновление фильма
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Изменен фильм с id={}", film.getId());
        filmService.update(film);
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
        return filmService.get(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable long id, @PathVariable long userId) {
        filmService.deleteLike(id, userId);
        return filmService.get(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopular(
            @RequestParam(defaultValue = "10", required = false) Long count) {
        return filmService.getPopular(count);
    }
}
