package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;


import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    private Long id;

    @NotBlank
    private String name;

    @Size(max = 200)
    @NotBlank
    private String description;

    private LocalDate releaseDate;

    @Positive
    @NotNull
    private Integer duration;

    @EqualsAndHashCode.Exclude
    @Builder.Default
    private Collection<Long> likes = new TreeSet<>();

    @NotNull
    private Mpa mpa;

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
}
