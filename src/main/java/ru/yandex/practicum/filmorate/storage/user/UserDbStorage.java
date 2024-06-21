package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Friend;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@Primary
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final int USERS_FRIENDSHIP_STATUS_CONFIRMED = 1;
    private static final int USERS_FRIENDSHIP_STATUS_UNCONFIRMED = 2;
    private static final String USERS_FIND_ALL_QUERY = """
            SELECT *
            FROM USERS;
            """;
    private static final String USERS_INSERT_QUERY = """
            INSERT INTO USERS ("EMAIL", "LOGIN", "NAME", "BIRTHDAY")
                        VALUES (?, ?, ?, ?);
            """;
    private static final String USERS_UPDATE_QUERY = """
            UPDATE USERS
            SET "EMAIL" = ?,
                "LOGIN" = ?,
                "NAME" = ?,
                "BIRTHDAY" = ?
            WHERE USER_ID = ?;
            """;
    private static final String USERS_ADD_TO_FRIENDS_QUERY = """
            INSERT INTO FRIENDS (USER_ID, FRIEND_ID, STATUS_ID)
            VALUES (?, ?, ?);
            """;
    private static final String USERS_DELETE_FROM_FRIENDS_QUERY = """
            DELETE FROM FRIENDS
            WHERE USER_ID = ?
                AND FRIEND_ID = ?;
            """;
    private static final String USERS_FIND_ALL_FRIENDS_QUERY = """
            SELECT *
            FROM USERS AS u
            WHERE USER_ID IN (
                SELECT FRIEND_ID
                FROM FRIENDS
                WHERE USER_ID = ?
                );
            """;
    private static final String USERS_FIND_COMMON_FRIENDS_QUERY = """
            SELECT *
            FROM USERS AS u
            WHERE u.USER_ID IN (
                SELECT friends_of_first.friend
                FROM (
                    SELECT FRIEND_ID AS friend FROM FRIENDS WHERE USER_ID = ?
                    UNION
                    SELECT USER_ID AS friend FROM FRIENDS WHERE FRIEND_ID = ?
                    ) AS friends_of_first
                JOIN (
                    SELECT FRIEND_ID AS friend FROM FRIENDS WHERE USER_ID = ?
                    UNION
                    SELECT USER_ID AS friend FROM FRIENDS WHERE FRIEND_ID = ?
                    ) AS friends_of_second
                ON friends_of_first.friend = friends_of_second.friend
            );
            """;
    private static final String USERS_FIND_BY_ID_QUERY = """
            SELECT *
            FROM USERS
            WHERE USER_ID = ?;
            """;
    private static final String USERS_FIND_BY_EMAIL_QUERY = """
            SELECT *
            FROM USERS
            WHERE "EMAIL" = ?;
            """;
    private static final String USERS_DELETE = """
            DELETE FROM USERS
            WHERE USER_ID = ?;
            """;

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> findAll() {
        log.info("Получение списка пользователей");
        return findMany(USERS_FIND_ALL_QUERY);
    }

    @Override
    public User findById(Long id) {
        List<User> users = findMany(
                USERS_FIND_BY_ID_QUERY,
                id
        );
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.getFirst();

    }

    @Override
    public User create(User user) {
        validate(user);
        long id = insertGetKey(
                USERS_INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        log.info("Пользователь {} добавлен в список с id = {}", user.getName(), user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new NotFoundException("Id пользователя должен быть указан");
        }
        if (isUserExists(user.getId())) {
            validate(user);
            update(
                    USERS_UPDATE_QUERY,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    java.sql.Date.valueOf(user.getBirthday()),
                    user.getId()
            );
            log.info("Пользователь с id = {} обновлен", user.getId());
            return user;
        }
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
    }

    // удаление юзера по id, модифицировал связи в schema,  при удалении юзераа удаляются зависимые записи по id
    @Override
    public void delete(Long id) {
        if (!isUserExists(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        delete(USERS_DELETE, id);
        log.info("Пользователь с id = {} удален", id);
    }

    @Override
    public void addToFriends(Long id, Long friendId) {
        if (!checkUserExists(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        if (!checkUserExists(friendId))
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        if (id == friendId)
            throw new ValidationException("Нельзя добавить самого себя в друзья (id = " + id + ")");
        User user = findOne(
                USERS_FIND_BY_ID_QUERY,
                id
        ).orElse(null);
        insert(
                USERS_ADD_TO_FRIENDS_QUERY,
                id,
                friendId,
                USERS_FRIENDSHIP_STATUS_UNCONFIRMED
        );
        user.addFriend(new Friend(friendId, USERS_FRIENDSHIP_STATUS_UNCONFIRMED));
        log.info("Пользователь с id = {} и пользователь с id = {} теперь друзья", friendId, id);
    }

    @Override
    public void deleteFromFriends(Long id, Long friendId) {
        if (!checkUserExists(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        if (!checkUserExists(friendId))
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        delete(
                USERS_DELETE_FROM_FRIENDS_QUERY,
                id,
                friendId
        );
        log.info("Пользователь с id = {} и пользователь с id = {} больше не друзья", friendId, id);
    }

    @Override
    public Collection<User> findAllFriends(Long id) {
        if (!isUserExists(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        log.info("Поиск друзей пользователя с id = {}", id);
        return findMany(
                USERS_FIND_ALL_FRIENDS_QUERY,
                id
        );
    }

    @Override
    public Collection<User> findCommonFriends(Long id, Long otherId) {
        if (!isUserExists(id))
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        if (!isUserExists(otherId))
            throw new NotFoundException("Пользователь с id = " + otherId + " не найден");
        log.info("Поиск общих друзей пользователя с id = {} и пользователя с id = {}", id, otherId);
        return findMany(
                USERS_FIND_COMMON_FRIENDS_QUERY,
                id,
                id,
                otherId,
                otherId
        );
    }

    @Override
    public boolean isUserExists(Long id) {
        return findOne(
                USERS_FIND_BY_ID_QUERY,
                id).isPresent();
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

    private boolean isDuplicatedEmail(String email) {
        return findOne(
                USERS_FIND_BY_EMAIL_QUERY,
                email).isPresent();
    }

    public boolean checkUserExists(Long id) {
        return findOne(
                USERS_FIND_BY_ID_QUERY,
                id).isPresent();
    }
}