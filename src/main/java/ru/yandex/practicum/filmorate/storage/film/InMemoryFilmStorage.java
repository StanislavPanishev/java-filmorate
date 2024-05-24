package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public void create(Film film) {
        validate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    @Override
    public void update(Film newFilm) {
        if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Такого фильма нет, обновление невозможно");
        }

        validate(newFilm);
        films.put(newFilm.getId(), newFilm);
    }

    @Override
    public Film get(long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Такого фильма нет.");
        } else {
            return films.get(id);
        }
    }

    @Override
    public void delete(long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Такого фильма нет.");
        }
        films.remove(id);
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public void addLike(Long id, Long userId) {
        if (films.containsKey(id)) {
            films.get(id).getLikes().add(userId);
        } else {
            throw new NotFoundException("Такого фильма нет.");
        }

    }

    @Override
    public void deleteLike(Long id, Long userId) {
        if (films.containsKey(id)) {
            films.get(id).getLikes().remove(userId);
        } else {
            throw new NotFoundException("Такого фильма нет.");
        }
    }

    @Override
    public List<Film> getPopular(Long count) {
        return films.values().stream().filter(o -> o.getLikes() != null)
                .sorted(Comparator.comparingInt(film -> film.getLikes().size())).limit(count)
                .toList().reversed();
    }
}