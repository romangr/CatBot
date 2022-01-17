CREATE TABLE DELAYED_POST
(
    id        varchar(36) NOT NULL,
    text      text        NOT NULL,
    submitted TIMESTAMP   NOT NULL,
    PRIMARY KEY (`id`)
);
