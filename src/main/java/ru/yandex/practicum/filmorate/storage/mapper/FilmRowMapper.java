package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final MpaMapper mpaMapper;

    public FilmRowMapper(MpaMapper mpaMapper) {
        this.mpaMapper = mpaMapper;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("FILMS_ID"));
        film.setName(rs.getString("NAME"));
        film.setDescription(rs.getString("DESCRIPTION"));
        film.setReleaseDate(rs.getDate("RELEASE_DATE").toLocalDate());
        film.setDuration(rs.getInt("DURATION"));
        film.setMpa(Objects.requireNonNull(mpaMapper.mapRow(rs, rowNum)));
        return film;
    }
}