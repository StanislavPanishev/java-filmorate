package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

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
    private static final String FILMS_GET_POPULAR_QUERY_BY_GENRE = """
            SELECT
                f.FILMS_ID AS FILMS_ID,
                f.NAME AS NAME,
                f.DESCRIPTION AS DESCRIPTION,
                f.RELEASE_DATE AS RELEASE_DATE,
                f.DURATION AS DURATION,
                r.RATING_ID AS RATING_ID,
                r.MPA_NAME AS MPA_NAME,
                COUNT(f.FILMS_ID) AS count
            FROM FILMS AS f
            LEFT JOIN MPA AS r ON f.RATING_ID = r.RATING_ID
            LEFT JOIN FILMS_GENRES AS fg ON fg.FILMS_ID = f.FILMS_ID
            WHERE fg.GENRE_ID = ?
            GROUP BY FILMS_ID
            ORDER BY count DESC
            LIMIT ?;
            """;
    private static final String FILMS_GET_POPULAR_QUERY_BY_YEAR = """
            SELECT
                f.FILMS_ID AS FILMS_ID,
                f.NAME AS NAME,
                f.DESCRIPTION AS DESCRIPTION,
                f.RELEASE_DATE AS RELEASE_DATE,
                f.DURATION AS DURATION,
                r.RATING_ID AS RATING_ID,
                r.MPA_NAME AS MPA_NAME,
                COUNT(f.FILMS_ID) AS count
            FROM FILMS AS f
            LEFT JOIN MPA AS r ON f.RATING_ID = r.RATING_ID
            WHERE EXTRACT(YEAR FROM f.RELEASE_DATE) = ?
            GROUP BY FILMS_ID
            ORDER BY count DESC
            LIMIT ?;
            """;
    private static final String FILMS_GET_POPULAR_QUERY_BY_YEAR_AND_GENRE = """
            SELECT
                f.FILMS_ID AS FILMS_ID,
                f.NAME AS NAME,
                f.DESCRIPTION AS DESCRIPTION,
                f.RELEASE_DATE AS RELEASE_DATE,
                f.DURATION AS DURATION,
                r.RATING_ID AS RATING_ID,
                r.MPA_NAME AS MPA_NAME,
                COUNT(f.FILMS_ID) AS count
            FROM FILMS AS f
            LEFT JOIN MPA AS r ON f.RATING_ID = r.RATING_ID
            LEFT JOIN FILMS_GENRES AS fg ON fg.FILMS_ID = f.FILMS_ID
            WHERE EXTRACT(YEAR FROM f.RELEASE_DATE) = ? AND fg.GENRE_ID = ?
            GROUP BY FILMS_ID
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

    private static final String FILMS_GET_POPULAR_QUERY_WITH_GENRE = """
            SELECT
                        f.FILMS_ID AS FILMS_ID,
                        f.NAME AS NAME,
                        f.DESCRIPTION AS DESCRIPTION,
                        f.RELEASE_DATE AS RELEASE_DATE,
                        f.DURATION AS DURATION,
                        r.RATING_ID AS RATING_ID,
                        r.MPA AS MPA,
                        COUNT(l.FILMS_ID) AS count
                    FROM FILMS AS f
                    LEFT JOIN LIKES AS l ON l.FILMS_ID = f.FILMS_ID
                    LEFT JOIN MPA AS r ON f.RATING_ID = r.RATING_ID
                    LEFT JOIN FILMS_GENRES AS fg ON fg.FILMS_ID = f.FILMS_ID
                    LEFT JOIN GENRES AS g ON g.GENRE_ID = fg.GENRE_ID
                    WHERE fg.GENRE_ID = ?
                    GROUP BY FILMS_ID
                    ORDER BY count DESC
                    LIMIT ?;
            """;

    private static final String FILMS_GET_POPULAR_QUERY_WITH_YEAR = """
            SELECT
                        f.FILMS_ID AS FILMS_ID,
                        f.NAME AS NAME,
                        f.DESCRIPTION AS DESCRIPTION,
                        f.RELEASE_DATE AS RELEASE_DATE,
                        f.DURATION AS DURATION,
                        r.RATING_ID AS RATING_ID,
                        r.MPA AS MPA,
                        COUNT(l.FILMS_ID) AS count
                    FROM FILMS AS f
                    LEFT JOIN LIKES AS l ON l.FILMS_ID = f.FILMS_ID
                    LEFT JOIN MPA AS r ON f.RATING_ID = r.RATING_ID
                    LEFT JOIN FILMS_GENRES AS fg ON fg.FILMS_ID = f.FILMS_ID
                    LEFT JOIN GENRES AS g ON g.GENRE_ID = fg.GENRE_ID
                    WHERE EXTRACT(YEAR FROM f.RELEASE_DATE) = ?
                    GROUP BY FILMS_ID
                    ORDER BY count DESC
                    LIMIT ?;
            """;

    private static final String FILMS_GET_POPULAR_QUERY_WITH_YEAR_AND_GENRE = """
            SELECT
                        f.FILMS_ID AS FILMS_ID,
                        f.NAME AS NAME,
                        f.DESCRIPTION AS DESCRIPTION,
                        f.RELEASE_DATE AS RELEASE_DATE,
                        f.DURATION AS DURATION,
                        r.RATING_ID AS RATING_ID,
                        r.MPA AS MPA,
                        COUNT(l.FILMS_ID) AS count
                    FROM FILMS AS f
                    LEFT JOIN LIKES AS l ON l.FILMS_ID = f.FILMS_ID
                    LEFT JOIN MPA AS r ON f.RATING_ID = r.RATING_ID
                    LEFT JOIN FILMS_GENRES AS fg ON fg.FILMS_ID = f.FILMS_ID
                    LEFT JOIN GENRES AS g ON g.GENRE_ID = fg.GENRE_ID
                    WHERE EXTRACT(YEAR FROM f.RELEASE_DATE) = ? AND fg.GENRE_ID = ?
                    GROUP BY FILMS_ID
                    ORDER BY count DESC
                    LIMIT ?;
            """;
    private static final String FILMS_SEARCH_BY_TITLE = """
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
            LEFT JOIN MPA AS r ON  f.RATING_ID = r.RATING_ID
            WHERE LOWER(f.NAME) LIKE LOWER('%' || ? || '%')
            GROUP BY f.NAME, f.FILMS_ID
            ORDER BY FILMS_ID;
            """;


    private static final String FILMS_DELETE = """
            DELETE FROM FILMS
            WHERE FILMS_ID = ?;
            """;

    private static final String GET_FILMS_RECOMMENDATIONS = """
            SELECT
                         f.FILMS_ID AS FILMS_ID,
                         f.NAME AS NAME,
                         f.DESCRIPTION AS DESCRIPTION,
                         f.RELEASE_DATE AS RELEASE_DATE,
                         f.DURATION AS DURATION,
                         r.RATING_ID AS RATING_ID,
                         r.MPA_NAME AS MPA_NAME
             FROM FILMS f
             LEFT JOIN MPA AS r ON f.RATING_ID = r.RATING_ID
             LEFT JOIN LIKES l ON f.FILMS_ID = l.FILMS_ID
             WHERE l.USER_ID IN
             (SELECT USER_ID FROM LIKES WHERE NOT USER_ID = ? AND FILMS_ID IN
             (SELECT FILMS_ID FROM LIKES WHERE USER_ID = ?)
             GROUP BY USER_ID order by COUNT(FILMS_ID) desc LIMIT 1)
             AND NOT l.FILMS_ID  IN (SELECT FILMS_ID FROM LIKES WHERE USER_ID = ?)
             LIMIT 1;
            """;

    private static final String GET_COMMON_FILMS = """
            SELECT *
            FROM FILMS AS f
            LEFT JOIN MPA AS r ON  f.RATING_ID = r.RATING_ID
            WHERE FILMS_ID IN (
                SELECT l1.FILMS_ID
                FROM LIKES AS l1
                LEFT JOIN LIKES AS l2 ON l1.FILMS_ID = l2.FILMS_ID
                WHERE l1.USER_ID = ? AND l2.USER_ID = ?
            )
            ORDER BY FILMS_ID
            """;

    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final FilmLikeStorage filmLikeStorage;
    private final FilmGenreStorage filmGenreStorage;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, UserStorage userStorage, GenreStorage genreStorage,
                         MpaStorage mpaStorage, FilmLikeStorage likeStorage, FilmGenreStorage filmGenreStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.filmLikeStorage = likeStorage;
        this.filmGenreStorage = filmGenreStorage;
    }


    @Override
    public Collection<Film> findAll() {
        log.info("Получение списка фильмов");
        Collection<Film> films = findMany(FILMS_FIND_ALL_QUERY);
        setFilmsGenres(films);
        setFilmsLikes(films);
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
        setFilmsLikes(films);
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
        film.setGenres(film.getGenres().stream()
                .distinct()
                .sorted(Comparator.comparingInt(Genre::getId))
                .toList());
        for (Genre genre : film.getGenres()) {
            insert(
                    FILMS_INSERT_FILMS_GENRE_QUERY,
                    film.getId(),
                    genre.getId()
            );
        }
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
            film.setGenres(film.getGenres().stream()
                    .distinct()
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .toList());
            for (Genre genre : film.getGenres()) {
                insert(
                        FILMS_INSERT_FILMS_GENRE_QUERY,
                        film.getId(),
                        genre.getId()
                );
            }
            delete(
                    film.getId()
            );
            log.info("Фильм с id = {} обновлен", film.getId());
            return film;
        }
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
    }

    // удаление фильма по id, модифицировал связи в schema, при удалении фильма удаляются зависимые записи по id
    @Override
    public void delete(Long id) {
        if (!isFilmExists(id))
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        delete(FILMS_DELETE, id);
        log.info("Фильм с id = {} удален", id);
    }

    @Override
    public Film addLike(Long id, Long userId) {
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
        //assert film != null;
        film.addLike(userId);
        log.info("Пользователь с id = {} поставил лайк фильму id = {}", userId, id);
        return film;
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
        film.deleteLike(userId);
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
        genreStorage.checkGenresExists(film.getGenres());
        film.setGenres(new HashSet<>(film.getGenres()));
        if (!mpaStorage.isMpaExists(film.getMpa().getId())) {
            throw new ValidationException("Рейтинг MPA с id = " + film.getMpa().getId() + " не найден!");
        }
    }

    private void setFilmsGenres(Collection<Film> films) {
        String filmsId = films.stream()
                .map(film -> {
                    return film.getId().toString();
                })
                .collect(Collectors.joining(", "));
        Collection<FilmGenre> filmGenres = filmGenreStorage.findGenresOfFilms(filmsId);
        for (Film film : films) {
            film.setGenres(filmGenres.stream()
                    .filter(filmGenre -> film.getId() == filmGenre.getFilmId())
                    .map(filmGenre -> new Genre(
                            filmGenre.getGenreId(),
                            filmGenre.getGenre())
                    )
                    .collect(Collectors.toList()));
        }
    }

    private void setFilmsLikes(Collection<Film> films) {
        String filmsId = films.stream()
                .map(film -> {
                    return film.getId().toString();
                })
                .collect(Collectors.joining(", "));
        Collection<FilmLike> filmLikes = filmLikeStorage.findLikesOfFilms(filmsId);
        for (Film film : films) {
            film.setLikes(filmLikes.stream()
                    .filter(filmLike -> film.getId() == filmLike.getFilmId())
                    .map(filmLike -> filmLike.getUserId())
                    .collect(Collectors.toList()));
        }
    }



}