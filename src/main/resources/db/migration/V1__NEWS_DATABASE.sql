
create table LINK_TOBE_PROCESSED(
    LINK varchar(200)
);

insert into LINK_TOBE_PROCESSED (LINK) values ('https://news.sina.cn');


create table LINK_ALREADY_PROCESSED(
    LINK varchar(200)
);

create table NEWS (
    ID bigint primary key auto_increment,
    TITLE varchar(1000),
    CONTENT text,
    CREATED_AT timestamp default now(),
    UPDATED_AT timestamp default now()
);