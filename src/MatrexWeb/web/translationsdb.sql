CREATE DATABASE TranslationsDB;

USE TranslationsDB;

CREATE TABLE translations
(
    transid int NOT NULL PRIMARY KEY,
    `time` time,
    `date` date,
    `source` varchar(30),
    target varchar(30),
    `domain` varchar(30),
    `input` varchar(10000),
    `output` varchar(10000),
    edits varchar(10000),
    timeInMS int
);

CREATE TABLE edits
(
    editid int,
    transid int,
    text varchar(10000)
);