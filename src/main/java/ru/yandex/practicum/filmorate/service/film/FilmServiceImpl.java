package ru.yandex.practicum.filmorate.service.film;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;


import java.time.LocalDate;
import java.util.Collection;


@Service
@AllArgsConstructor
public class FilmServiceImpl implements FilmService {

    private FilmStorage filmStorage;

    static final LocalDate dateOfFirstFilm = LocalDate.of(1895, 1, 28);

    @Override
    public Collection<Film> getAll() {
        return filmStorage.findAll();
    }

    @Override
    public void create(Film film) {
        validate(film);
        filmStorage.create(film);
    }

    @Override
    public void update(Film film) {
        validate(film);
        filmStorage.update(film);
    }

    @Override
    public Film get(long id) {
        return filmStorage.findById(id);
    }

    @Override
    public void delete(long id) {
        filmStorage.delete(id);
    }

    @Override
    public void addLike(Long id, Long userId) {
        filmStorage.addLike(id, userId);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        filmStorage.deleteLike(id, userId);
    }

    @Override
    public Collection<Film> getPopular(Long count) {
        return filmStorage.getPopular(count);
    }

    private void validate(Film film) {
        if (film != null) {
            if (film.getReleaseDate().isBefore(dateOfFirstFilm)) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
            }
        }
    }


}