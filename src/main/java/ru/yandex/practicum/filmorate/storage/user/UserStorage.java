package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    void create(User user);

    void update(User user);

    User get(long id);

    void delete(long id);

    Collection<User> getAll();

    void addFriend(Long id, Long friendId);

    void deleteFromFriends(Long id, Long friendId);

    Collection<User> getAllFriends(Long id);

    Collection<User> getCommonFriends(Long id, Long friendId);
}