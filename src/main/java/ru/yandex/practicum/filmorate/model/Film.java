package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


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

    @NotNull
    private Mpa mpa;

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
}
