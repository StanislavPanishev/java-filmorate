package ru.yandex.practicum.filmorate.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    InMemoryUserStorage userStorage;

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void create(User user) {
        userStorage.create(user);
        log.info("Добавлен новый пользователь, id={}", user.getId());
    }

    public void update(User newUser) {
        userStorage.update(newUser);
    }

    public User get(long id) {
        return userStorage.get(id);
    }

    public void delete(long id) {
        userStorage.delete(id);
    }

    public void addFriend(Long id, Long friendId) {
        userStorage.addFriend(id, friendId);
        log.info("Добавлен новый друг с id={}", friendId);
    }

    public void deleteFromFriends(Long id, Long friendId) {
        userStorage.deleteFromFriends(id, friendId);
        log.info("Удален друг с id={}", friendId);
    }

    public Collection<User> getAllFriends(Long id) {
        return userStorage.getAllFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long friendId) {
        return userStorage.getCommonFriends(id, friendId);
    }
}