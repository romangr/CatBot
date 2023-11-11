CREATE TABLE DELAYED_POST
(
    id          varchar(36) NOT NULL,
    text        text,
    document_id text,
    video_id    text,
    photo_id    text,
    submitted   TIMESTAMP   NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE MIGRATIONS
(
    name varchar(50) primary key not null
)