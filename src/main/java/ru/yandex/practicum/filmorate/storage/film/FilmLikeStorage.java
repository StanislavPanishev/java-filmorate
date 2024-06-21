package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.FilmLike;

import java.util.Collection;

public interface FilmLikeStorage {
    Collection<FilmLike> findLikesOfFilms(String filmsId);
}