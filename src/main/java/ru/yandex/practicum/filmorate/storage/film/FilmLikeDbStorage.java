package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmLike;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.Collection;

@Slf4j
@Component
public class FilmLikeDbStorage extends BaseDbStorage<FilmLike> implements FilmLikeStorage {
    private static final String LIKES_FIND_BY_FILM_ID_QUERY = """
            SELECT *
            FROM LIKES
            WHERE FILMS_ID IN (%s);
            """;

    public FilmLikeDbStorage(JdbcTemplate jdbc, RowMapper<FilmLike> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<FilmLike> findLikesOfFilms(String filmsId) {
        log.info("Получение списка лайков для фильма с id = {}", filmsId);
        return findMany(
                String.format(LIKES_FIND_BY_FILM_ID_QUERY, filmsId)
        );
    }
}