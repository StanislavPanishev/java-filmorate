package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;


import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    private Long id;

    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    private LocalDate releaseDate;

    @PositiveOrZero
    private Integer duration;

    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Collection<Long> likes = new HashSet<>();

    public void addLike(Long id) {
        likes.add(id);
    }

    public void deleteLike(Long id) {
        likes.remove(id);
    }

    public int getLikesCount() {
        return likes.size();
    }

    @NotNull
    private Mpa mpa;

    @Builder.Default
    private Collection<Genre> genres = new HashSet<>();

    public void initGenres() {
        if (genres == null) {
            genres = new HashSet<>();
        }
    }

    public void addGenre(Genre genre) {
        initGenres();
        if (genre.getId() <= 0) {
            throw new ValidationException("");
        }
        genres.add(genre);
    }
}
