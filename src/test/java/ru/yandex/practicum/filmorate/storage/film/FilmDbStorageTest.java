package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/schema.sql", "/data.sql"})
public class FilmDbStorageTest {
    private final FilmDbStorage filmDbStorage;

    @Test
    public void testDeleteFilm() {
        Film film = Film
                .builder()
                .description("Fantasy")
                .name("Иван Васильевич меняет профессию")
                .releaseDate(LocalDate.of(1973, 9, 17))
                .duration(88)
                .mpa(new Mpa(1))
                .genres(new HashSet<>())
                .build();
        filmDbStorage.create(film);
        filmDbStorage.delete(1L);
        assertThrows(NotFoundException.class, () -> filmDbStorage.findById(1L));
    }

    public Film getTestFilm(int id) {
        switch (id) {
            case 1:
                Film film = Film.builder()
                        .id(null)
                        .name("Film1")
                        .description("Desc1")
                        .releaseDate(LocalDate.now())
                        .duration(88)
                        .mpa(new Mpa(1, "G"))
                        .genres(Set.of(
                                new Genre(1, "Комедия"),
                                new Genre(2, "Драма")))
                        .build();
                return film;
            case 2:
                Film film2 = Film.builder()
                        .id(null)
                        .name("Film2")
                        .description("Desc2")
                        .releaseDate(LocalDate.now().minusYears(1))
                        .duration(88)
                        .mpa(new Mpa(2, "PG"))
                        .genres(Set.of(
                                new Genre(3, "Мультфильм")))
                        .build();
                return film2;
            case 3:
                Film film3 = Film.builder()
                        .id(null)
                        .name("Film3")
                        .description("Desc3")
                        .releaseDate(LocalDate.now().minusMonths(3))
                        .duration(88)
                        .mpa(new Mpa(3, "PG-13"))
                        .genres(Set.of(
                                new Genre(4, "Триллер")))
                        .build();
                return film3;
            default:
                return null;
        }
    }

    @Test
    void findAll() {
        Film film = getTestFilm(1);
        filmDbStorage.create(film);
        Film film2 = getTestFilm(2);
        filmDbStorage.create(film2);

        Collection<Film> responseEntity = filmDbStorage.findAll();
        assertNotNull(responseEntity);
        assertEquals(2, responseEntity.size());
    }

    @Test
    void create() {
        Film film = getTestFilm(1);
        filmDbStorage.create(film);
        Collection<Film> responseEntity = filmDbStorage.findAll();
        assertNotNull(responseEntity);
        assertEquals(1, responseEntity.size());
        assertNotNull(responseEntity.iterator().next().getId());
        assertEquals(film.getName(), responseEntity.iterator().next().getName());
        assertEquals(film.getDescription(), responseEntity.iterator().next().getDescription());
        assertEquals(film.getReleaseDate(), responseEntity.iterator().next().getReleaseDate());
        assertEquals(film.getDuration(), responseEntity.iterator().next().getDuration());
    }

    @Test
    void update() {
        Film film = getTestFilm(1);
        filmDbStorage.create(film);
        Collection<Film> responseEntity = filmDbStorage.findAll();
        final Film newFilm = getTestFilm(2);
        newFilm.setId(responseEntity.iterator().next().getId());
        filmDbStorage.update(newFilm);
        responseEntity = filmDbStorage.findAll();
        assertNotNull(responseEntity);
    }


}