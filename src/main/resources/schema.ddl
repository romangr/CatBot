CREATE TABLE DELAYED_POST
(
    id        varchar(36) NOT NULL,
    text      text        NOT NULL,
    submitted DATE        NOT NULL,
    PRIMARY KEY (`id`)
);
