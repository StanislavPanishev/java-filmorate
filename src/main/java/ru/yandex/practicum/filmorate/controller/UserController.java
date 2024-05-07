package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    // Получение списка всех пользователей
    @GetMapping
    public Collection<User> findAllUsers() {
        return users.values();
    }

    // Создание пользователя
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        validate(user);
        user.setId(getNextId());
        log.info("Добавлен пользователь с id={}", user.getId());
        users.put(user.getId(), user);
        return users.get(user.getId());
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    // Обновление пользователя
    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        validate(user);
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь c id=" + user.getId() + " не найден");
        }
        log.info("Изменен пользователь с id={}", user.getId());
        users.put(user.getId(), user);
        return users.get(user.getId());
    }

    public void validate(User user) {
        // проверяем необходимые условия
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }
}
