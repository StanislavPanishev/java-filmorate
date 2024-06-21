package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserServiceImpl;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserServiceImpl userService;

    // Получение списка всех пользователей
    @GetMapping
    public Collection<User> findAllUsers() {
        return userService.getAll();
    }

    // Создание пользователя
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        userService.create(user);
        return user;
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable long id) {
        return userService.get(id);
    }

    // Обновление пользователя
    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Изменен пользователь с id={}", user.getId());
        userService.update(user);
        return user;
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable long id) {
        userService.delete(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User create(@PathVariable long id, @PathVariable long friendId) {
        userService.addFriend(id, friendId);
        log.info("Пользователь добавлен в список друзей");
        return userService.get(friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFromFriends(@PathVariable long id, @PathVariable long friendId) {
        userService.deleteFromFriends(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getAllFriends(@PathVariable long id) {
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

}
