--待处理链接
create table LINK_TOBE_PROCESSED(
    LINK varchar(200)
);
--初始化进去一个链接
insert into LINK_TOBE_PROCESSED (LINK) values ('https://news.sina.cn');

--已处理链接
create table LINK_ALREADY_PROCESSED(
    LINK varchar(200)
);

--新闻表
create table NEWS (
    id bigint primary key auto_increment,
    title varchar(1000),
    content clob,
    created_at timestamp,
    updated_at timestamp
);