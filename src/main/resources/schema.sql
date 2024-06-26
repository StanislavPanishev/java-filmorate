DROP TABLE IF EXISTS
    FILMS,
    FILMS_GENRES,
    FRIENDS,
    GENRES,
    LIKES,
    MPA,
    USERS
    CASCADE;

CREATE TABLE IF NOT EXISTS GENRES
(
    GENRE_ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    GENRE_NAME VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS MPA
(
    RATING_ID INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    MPA_NAME  VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS FILMS
(
    FILMS_ID     BIGINT  NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME         VARCHAR NOT NULL,
    DESCRIPTION  VARCHAR NOT NULL,
    RELEASE_DATE DATE    NOT NULL,
    DURATION     INTEGER NOT NULL,
    RATING_ID    INTEGER NOT NULL REFERENCES MPA (RATING_ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS USERS
(
    USER_ID  BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    EMAIL    VARCHAR NOT NULL,
    LOGIN    VARCHAR NOT NULL,
    NAME     VARCHAR NOT NULL,
    BIRTHDAY DATE    NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS USER_EMAIL_INDEX ON USERS (EMAIL);

CREATE UNIQUE INDEX IF NOT EXISTS USER_LOGIN_INDEX ON USERS (LOGIN);

CREATE TABLE IF NOT EXISTS FRIENDS
(
    USER_ID   BIGINT NOT NULL REFERENCES USERS (USER_ID) ON DELETE CASCADE,
    FRIEND_ID BIGINT NOT NULL REFERENCES USERS (USER_ID) ON DELETE CASCADE,
    PRIMARY KEY (USER_ID)
);

CREATE TABLE IF NOT EXISTS LIKES
(
    USER_ID  BIGINT NOT NULL REFERENCES USERS (USER_ID),
    FILMS_ID BIGINT NOT NULL REFERENCES FILMS (FILMS_ID),
    CONSTRAINT PK_USER_FILMS PRIMARY KEY (USER_ID, FILMS_ID)
);

CREATE TABLE IF NOT EXISTS FILMS_GENRES
(
    FILMS_ID BIGINT  NOT NULL REFERENCES FILMS (FILMS_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    GENRE_ID INTEGER NOT NULL REFERENCES GENRES (GENRE_ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT PK_FILMS_GENRES PRIMARY KEY (FILMS_ID, GENRE_ID)
);





















