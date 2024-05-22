package ru.yandex.practicum.filmorate.service.film;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class FilmService {

    InMemoryFilmStorage filmStorage;
    InMemoryUserStorage userStorage;

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void create(Film film) {
        filmStorage.create(film);
    }

    public void update(Film newFilm) {
        filmStorage.update(newFilm);
    }

    public Film get(long id) {
        return filmStorage.get(id);
    }

    public void delete(long id) {
        filmStorage.delete(id);
    }

    public void addLike(Long id, Long userId) {
        if (userStorage.getUsers().containsKey(userId)) {
            filmStorage.addLike(id, userId);
        } else {
            throw new NotFoundException("Такого пользователя нет.");
        }
    }

    public void deleteLike(Long id, Long userId) {
        if (userStorage.getUsers().containsKey(userId)) {
            filmStorage.deleteLike(id, userId);
        } else {
            throw new NotFoundException("Такого пользователя нет.");
        }
    }

    public List<Film> getPopular(Long count) {
        return filmStorage.getPopular(count);
    }

}