package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserService {
    Collection<User> getAll();

    void create(User user);

    void update(User newUser);

    User get(long id);

    void delete(long id);

    void addFriend(Long id, Long friendId);

    void deleteFromFriends(Long id, Long friendId);

    Collection<User> getAllFriends(Long id);

    Collection<User> getCommonFriends(Long id, Long friendId);
}
