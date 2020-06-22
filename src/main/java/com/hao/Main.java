package com.hao;

import com.hao.dao.CrawlerMybatisDao;

public class Main {
    public static void main(String[] args) {
        new Crawler(new CrawlerMybatisDao()).start();
        new Crawler(new CrawlerMybatisDao()).start();
        new Crawler(new CrawlerMybatisDao()).start();
        new Crawler(new CrawlerMybatisDao()).start();
    }
}
