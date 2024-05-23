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
public class FilmService implements FilmServiceInterface {

    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;

    @Override
    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    @Override
    public void create(Film film) {
        filmStorage.create(film);
    }

    @Override
    public void update(Film newFilm) {
        filmStorage.update(newFilm);
    }

    @Override
    public Film get(long id) {
        return filmStorage.get(id);
    }

    @Override
    public void delete(long id) {
        filmStorage.delete(id);
    }

    @Override
    public void addLike(Long id, Long userId) {
        if (userStorage.getUsers().containsKey(userId)) {
            filmStorage.addLike(id, userId);
        } else {
            throw new NotFoundException("Такого пользователя нет.");
        }
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        if (userStorage.getUsers().containsKey(userId)) {
            filmStorage.deleteLike(id, userId);
        } else {
            throw new NotFoundException("Такого пользователя нет.");
        }
    }

    @Override
    public List<Film> getPopular(Long count) {
        return filmStorage.getPopular(count);
    }

}