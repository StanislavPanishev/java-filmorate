package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.Collection;

@Slf4j
@Component
@Primary
public class MpaDbStorage extends BaseDbStorage<Mpa> implements MpaStorage {
    private static final String MPA_FIND_ALL_QUERY = """
            SELECT *
            FROM MPA;
            """;
    private static final String MPA_FIND_BY_ID_QUERY = """
            SELECT *
            FROM MPA
            WHERE RATING_ID = ?;
            """;

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Mpa> findAll() {
        log.info("Получение списка рейтингов");
        return findMany(MPA_FIND_ALL_QUERY);
    }

    @Override
    public Mpa findById(int id) {
        log.info("Получение рейтинга с id = {}", id);
        return findOne(
                MPA_FIND_BY_ID_QUERY,
                id
        ).orElseThrow(() -> new NotFoundException("Рейтинг MPA с id = " + id + " не найден!"));
    }

    @Override
    public boolean isMpaExists(int id) {
        return findOne(
                MPA_FIND_BY_ID_QUERY,
                id).isPresent();
    }
}