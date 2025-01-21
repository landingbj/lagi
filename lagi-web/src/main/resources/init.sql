create table if not exists lagi_user
(
    id                   INTEGER     not null
        constraint lagi_user_pk
            primary key autoincrement,
    category             varchar(32) not null,
    category_create_time datetime    not null
);

create table if not exists lagi_upload_file
(
    id       Integer
        primary key autoincrement,
    file_id  char(32)     not null
        constraint lagi_upload_file_file_id_uindex
            unique,
    filename varchar(50)  not null,
    filepath varchar(250) not null,
    category varchar(50)  not null,
    create_time int(20)    not null,
    user_id varchar(20)    not null
);


create table if not exists lagi_user_preference
(
    id       Integer
    primary key autoincrement,
    finger  varchar(64) not null,
    user_id  varchar(64) ,
    llm varchar(64)  ,
    tts varchar(64) ,
    asr varchar(64)  ,
    img2Text varchar(64)  ,
    imgGen varchar(64) ,
    imgEnhance varchar(64)  ,
    img2Video varchar(64)  ,
    text2Video varchar(64) ,
    videoEnhance varchar(64)  ,
    videoTrack varchar(64)
);

create table if not exists lagi_agent_trace
(
    id       INTEGER
        primary key autoincrement,
    name     varchar(100)      not null,
    agent_id INTEGER           not null,
    count    INTEGER default 0 not null,
    unique (name, agent_id)
);

create table if not exists lagi_llm_trace
(
    id       INTEGER
    primary key autoincrement,
    name     varchar(100)      not null,
    count    INTEGER default 0 not null,
    unique (name)
    );