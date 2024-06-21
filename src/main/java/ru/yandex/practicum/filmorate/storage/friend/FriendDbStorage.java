package ru.yandex.practicum.filmorate.storage.friend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friend;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.Collection;

@Slf4j
@Component
@Primary
public class FriendDbStorage extends BaseDbStorage<Friend> implements FriendStorage {
    private static final String FRIENDS_FIND_BY_USER_ID_QUERY = """
            SELECT *
            FROM FRIENDS
            WHERE USER_ID = ?;
            """;

    public FriendDbStorage(JdbcTemplate jdbc, RowMapper<Friend> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Friend> findFriendsOfUser(Long id) {
        log.info("Получение списка друзей для пользователя с id = {}", id);
        return findMany(
                FRIENDS_FIND_BY_USER_ID_QUERY,
                id
        );
    }
}