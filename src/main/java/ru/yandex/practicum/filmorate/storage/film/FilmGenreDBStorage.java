package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.Collection;

@Component
public class FilmGenreDBStorage extends BaseDbStorage<FilmGenre> implements FilmGenreStorage {
    private static final String GENRES_FIND_BY_FILM_ID_QUERY = """
            SELECT fg.FILMS_ID, g.GENRE_ID, g.GENRE_NAME
                                                 FROM FILMS_GENRES AS fg
                                                 JOIN GENRES AS g ON fg.GENRE_ID = g.GENRE_ID
                                                 WHERE FILMS_ID IN (%s)
                                                 ORDER BY fg.FILMS_ID, g.GENRE_ID;
            """;

    public FilmGenreDBStorage(JdbcTemplate jdbc, RowMapper<FilmGenre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<FilmGenre> findGenresOfFilms(String filmsId) {
        return findMany(
                String.format(GENRES_FIND_BY_FILM_ID_QUERY, filmsId)
        );
    }
}