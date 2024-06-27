package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@Primary
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {
    private static final String GENRES_FIND_ALL_QUERY = """
            SELECT *
            FROM GENRES;
            """;
    private static final String GENRES_FIND_BY_ID_QUERY = """
            SELECT *
            FROM GENRES
            WHERE GENRE_ID = ?;
            """;

    private static final String GENRES_FIND_BY_IDS_QUERY = """
            SELECT GENRE_ID FROM GENRES WHERE GENRE_ID IN (%S)
            """;

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genre> findAll() {
        log.info("Получение списка жанров");
        return findMany(GENRES_FIND_ALL_QUERY);
    }

    @Override
    public Genre findById(int id) {
        log.info("Получение жанра с id = {}", id);
        return findOne(
                GENRES_FIND_BY_ID_QUERY,
                id
        ).orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден!"));
    }

    @Override
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
}