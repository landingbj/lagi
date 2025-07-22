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
    knowledge_base_id int(20)   null,
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

CREATE TABLE IF NOT EXISTS model_develop_info (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id varchar(64),
  model_path varchar(200),
  template varchar(64),
  adapter_path varchar(200),
  finetuning_type varchar(64),
  port varchar(20),
  running INTEGER,
  inference_id varchar(64)
);

CREATE TABLE IF NOT EXISTS model_manager (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id varchar(64),
    model_name varchar(64),
    online INTEGER,
    api_key varchar(64),
    model_type varchar(64),
    endpoint varchar(200),
    status INTEGER
);

CREATE TABLE IF NOT EXISTS user_rag_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id varchar(64),
    file_type varchar(64),
    category varchar(64),
    chunk_size Integer,
    temperature REAL
);

CREATE TABLE IF NOT EXISTS user_rag_vector (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id VARCHAR(64),
    default_category VARCHAR(255),
    similarity_top_k INT,
    similarity_cutoff DOUBLE,
    parent_depth INT,
    child_depth INT
);


CREATE TABLE IF NOT EXISTS knowledge_base (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    region TEXT,
    category TEXT,
    settings_id INTEGER,
    is_public INTEGER CHECK(is_public IN (0,1)) NOT NULL DEFAULT 0,
    enable_fulltext INTEGER CHECK(enable_fulltext IN (0,1)) NOT NULL DEFAULT 0,
    enable_graph INTEGER CHECK(enable_graph IN (0,1)) NOT NULL DEFAULT 0,
    enable_text2qa INTEGER CHECK(enable_text2qa IN (0,1)) NOT NULL DEFAULT 0,
    wenben_chunk_size INTEGER NOT NULL DEFAULT 500,
    biaoge_chunk_size INTEGER NOT NULL DEFAULT 200,
    tuwen_chunk_size INTEGER NOT NULL DEFAULT 300,
    similarity_top_k INTEGER NOT NULL DEFAULT 5,
    similarity_cutoff REAL NOT NULL DEFAULT 0.7,
    create_time INTEGER NOT NULL,
    update_time INTEGER NOT NULL
);
