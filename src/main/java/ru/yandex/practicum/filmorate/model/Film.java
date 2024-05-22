package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
@RequiredArgsConstructor
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
    private Set<Long> likes = new HashSet<>();
}
