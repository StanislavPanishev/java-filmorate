package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> findAll();

    User findById(Long id);

    User create(User user);

    User update(User newUser);

    void delete(Long id);

    void addToFriends(Long id, Long friendId);

    void deleteFromFriends(Long id, Long friendId);

    Collection<User> findAllFriends(Long id);

    Collection<User> findCommonFriends(Long id, Long otherId);

    boolean isUserExists(Long id);
}