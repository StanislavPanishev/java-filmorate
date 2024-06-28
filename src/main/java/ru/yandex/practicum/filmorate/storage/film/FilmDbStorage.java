package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;


@Slf4j
@Component
@Primary
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final String FILMS_FIND_ALL_QUERY = """
            SELECT *
            FROM FILMS AS f
            LEFT JOIN MPA AS r ON  f.RATING_ID = r.RATING_ID;
            """;
    private static final String FILMS_INSERT_QUERY = """
            INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING_ID)
                        VALUES (?, ?, ?, ?, ?);
            """;
    private static final String FILMS_UPDATE_QUERY = """
            UPDATE FILMS
            SET NAME = ?,
                DESCRIPTION = ?,
                RELEASE_DATE = ?,
                DURATION = ?,
                RATING_ID = ?
            WHERE FILMS_ID = ?;
            """;
    private static final String FILMS_FIND_BY_ID_QUERY = """
            SELECT *
            FROM FILMS AS f
            LEFT JOIN MPA AS r ON  f.RATING_ID = r.RATING_ID
            WHERE f.FILMS_ID = ?;
            """;
    private static final String FILMS_ADD_LIKE_QUERY = """
            INSERT INTO LIKES (FILMS_ID, USER_ID)
                        VALUES (?, ?);
            """;
    private static final String FILMS_DELETE_LIKE_QUERY = """
            DELETE FROM LIKES
            WHERE FILMS_ID = ?
                AND USER_ID = ?;
            """;
    private static final String FILMS_GET_POPULAR_QUERY = """
            SELECT
                f.FILMS_ID AS FILMS_ID,
                f.NAME AS NAME,
                f.DESCRIPTION AS DESCRIPTION,
                f.RELEASE_DATE AS RELEASE_DATE,
                f.DURATION AS DURATION,
                r.RATING_ID AS RATING_ID,
                r.MPA_NAME AS MPA_NAME,
            COUNT(l.FILMS_ID) AS count
            FROM FILMS AS f
            LEFT JOIN LIKES AS l ON l.FILMS_ID = f.FILMS_ID
            LEFT JOIN MPA AS r ON f.RATING_ID = r.RATING_ID
            GROUP BY f.FILMS_ID
            ORDER BY count DESC
            LIMIT ?;
            """;
    private static final String FILMS_DELETE_FILMS_GENRE_QUERY = """
            DELETE FROM FILMS_GENRES
            WHERE FILMS_ID = ?;
            """;
    private static final String FILMS_INSERT_FILMS_GENRE_QUERY = """
            INSERT INTO FILMS_GENRES (FILMS_ID, GENRE_ID)
                VALUES (?, ?);
            """;

    private static final String FILMS_DELETE = """
            DELETE FROM FILMS
            WHERE FILMS_ID = ?;
            """;

    private static final String GENRES_FIND_BY_IDS_QUERY = """
            SELECT GENRE_ID FROM GENRES WHERE GENRE_ID IN (%S)
            """;

    private static final String MPA_FIND_BY_ID_QUERY = """
            SELECT *
            FROM MPA
            WHERE RATING_ID = ?;
            """;

    private final MpaStorage mpaStorage;
    private final FilmGenreStorage filmGenreStorage;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper,
                         MpaStorage mpaStorage, FilmGenreStorage filmGenreStorage) {
        super(jdbc, mapper);
        this.mpaStorage = mpaStorage;
        this.filmGenreStorage = filmGenreStorage;
    }


    @Override
    public Collection<Film> findAll() {
        log.info("Получение списка фильмов");
        Collection<Film> films = findMany(FILMS_FIND_ALL_QUERY);
        setFilmsGenres(films);
        return films;
    }

    @Override
    public Film findById(Long id) {
        log.info("Получение фильма с id = {}", id);
        Collection<Film> films = findMany(FILMS_FIND_BY_ID_QUERY, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден!");
        }
        setFilmsGenres(films);
        return films.iterator().next();
    }

    @Override
    public Film create(Film film) {
        validate(film);
        long id = insertGetKey(
                FILMS_INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        //film.getGenres().forEach(genre -> insert(FILMS_INSERT_FILMS_GENRE_QUERY, id, genre.getId()));
        updateGenres(film.getGenres(), id);
        log.info("Фильм {} добавлен в список с id = {}", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new NotFoundException("Id фильма должен быть указан");
        }
        if (isFilmExists(film.getId())) {
            update(
                    FILMS_UPDATE_QUERY,
                    film.getName(),
                    film.getDescription(),
                    Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId()
            );
            delete(
                    FILMS_DELETE_FILMS_GENRE_QUERY,
                    film.getId()
            );
            updateGenres(film.getGenres(), film.getId());
            delete(
                    film.getId()
            );
            log.info("Фильм с id = {} обновлен", film.getId());
            return film;
        }
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
    }

    @Override
    public void delete(Long id) {
        if (!isFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        delete(FILMS_DELETE, id);
        log.info("Фильм с id = {} удален", id);
    }

    @Override
    public void addLike(Long id, Long userId) {
        if (!isFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        Film film = findOne(
                FILMS_FIND_BY_ID_QUERY,
                id
        ).orElse(null);
        insert(
                FILMS_ADD_LIKE_QUERY,
                id,
                userId
        );
        log.info("Пользователь с id = {} поставил лайк фильму id = {}", userId, id);
    }

    @Override
    public void deleteLike(Long id, Long userId) {
        if (!isFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        Film film = findOne(
                FILMS_FIND_BY_ID_QUERY,
                id
        ).orElse(null);
        delete(
                FILMS_DELETE_LIKE_QUERY,
                id,
                userId
        );
        assert film != null;
        log.info("Пользователь с id = {} удалил лайк фильму id = {}", userId, id);
    }

    @Override
    public Collection<Film> getPopular(Long count) {
        if (count <= 0) throw new ValidationException("Параметр count должен быть больше 0");
        log.info("Получение списка {} популярных фильмов", count);
        return findMany(
                FILMS_GET_POPULAR_QUERY,
                count);
    }

    @Override
    public boolean isFilmExists(Long id) {
        return findOne(
                FILMS_FIND_BY_ID_QUERY,
                id).isPresent();
    }

    public void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        film.setGenres(new HashSet<>(film.getGenres()));
        checkGenresExists(film.getGenres());
        if (!mpaStorage.isMpaExists(film.getMpa().getId())) {
            throw new ValidationException("Рейтинг MPA с id = " + film.getMpa().getId() + " не найден!");
        }
    }

    public void checkGenresExists(Collection<Genre> genres) {
        if (genres.isEmpty()) {
            return;
        }

        List<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .toList();

        List<Integer> existingGenreIds = findExistingGenreIds(genreIds);

        for (Genre genre : genres) {
            if (!existingGenreIds.contains(genre.getId()))
                throw new ValidationException("Жанр с id = " + genre.getId() + " не найден!");
        }
    }

    private List<Integer> findExistingGenreIds(List<Integer> genreIds) {
        String inClause = String.join(",", Collections.nCopies(genreIds.size(), "?"));
        String query = String.format(GENRES_FIND_BY_IDS_QUERY, inClause);
        return jdbc.queryForList(query, Integer.class, genreIds.toArray());
    }


    private void setFilmsGenres(Collection<Film> films) {

        final Map<Long, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, identity()));

        String filmsId = films.stream()
                .map(film -> {
                    return film.getId().toString();
                })
                .collect(Collectors.joining(", "));

        Collection<FilmGenre> filmGenres = filmGenreStorage.findGenresOfFilms(filmsId);

        for (FilmGenre filmGenre : filmGenres) {
            Film film = filmById.get(filmGenre.getFilmId());
            if (film != null) {
                film.getGenres().add(new Genre(filmGenre.getGenreId(), filmGenre.getGenre()));
            }
        }
    }

    private void updateGenres(Set<Genre> genres, Long id) {

        if (genres.size() > 0) {

            Genre[] g = genres.toArray(new Genre[genres.size()]);

            jdbc.batchUpdate(
                    FILMS_INSERT_FILMS_GENRE_QUERY,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setInt(1, Math.toIntExact(id));
                            ps.setInt(2, g[i].getId());
                        }

                        public int getBatchSize() {
                            return genres.size();
                        }
                    });
        }
    }
}