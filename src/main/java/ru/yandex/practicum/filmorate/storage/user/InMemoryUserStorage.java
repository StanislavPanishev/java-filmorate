package ru.yandex.practicum.filmorate.storage.user;

import lombok.Data;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Data
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public void create(User user) {
        user.setId(getNextId());
        user.setFriends(new TreeSet<>());
        validate(user);
        users.put(user.getId(), user);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public void update(User newUser) {
        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Такого пользователя нет, обновление невозможно");
        }

        validate(newUser);
        users.put(newUser.getId(), newUser);
    }

    @Override
    public User get(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Такого пользователя нет.");
        } else {
            return users.get(id);
        }
    }

    @Override
    public void delete(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Такого пользователя нет.");
        }

        if (!users.get(id).getFriends().isEmpty()) {
            for (Long friendId : users.get(id).getFriends()) {
                deleteFromFriends(id, friendId);
            }
        }
        users.remove(id);
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    public void validate(User user) {
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Введенная дата рождения не может быть позже сегодняшней даты");
        }

    }

    @Override
    public void addFriend(Long id, Long friendId) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователя с id={}" + id + " нет, добавление в друзья невозможно");
        }

        if (!users.containsKey(friendId)) {
            throw new NotFoundException("Пользователя с id={}" + friendId + " нет, добавление в друзья невозможно");
        }

        users.get(id).getFriends().add(friendId);
        users.get(friendId).getFriends().add(id);

    }

    @Override
    public void deleteFromFriends(Long id, Long friendId) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователя с id={}" + id + " нет, удаление невозможно");
        } else if (!users.containsKey(friendId)) {
            throw new NotFoundException("Пользователя с id={}" + friendId + " нет, удаление невозможно");
        }

        users.get(id).getFriends().remove(friendId);
        users.get(friendId).getFriends().remove(id);
    }

    @Override
    public Collection<User> getAllFriends(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Такого пользователя нет.");
        }

        Collection<User> friendsList = new ArrayList<>();
        for (Long friendId : users.get(id).getFriends()) {
            friendsList.add(users.get(friendId));
        }
        return friendsList;
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long friendId) {
        Collection<User> commonFriendsList = new ArrayList<>(getAllFriends(id));
        commonFriendsList.retainAll(getAllFriends(friendId));
        return commonFriendsList;
    }
}
