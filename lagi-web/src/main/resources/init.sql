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
    category varchar(50)  not null
);