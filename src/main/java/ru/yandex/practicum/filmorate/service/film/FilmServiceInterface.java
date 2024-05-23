package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmServiceInterface {
    Collection<Film> getAll();

    void create(Film film);

    void update(Film newFilm);

    Film get(long id);

    void delete(long id);

    void addLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    List<Film> getPopular(Long count);
}
