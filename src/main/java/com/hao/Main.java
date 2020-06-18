package com.hao;

import com.hao.dao.CrawlerDao;
import com.hao.dao.CrawlerMybatisDao;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        new Crawler(new CrawlerMybatisDao()).start();
        new Crawler(new CrawlerMybatisDao()).start();
        new Crawler(new CrawlerMybatisDao()).start();
        new Crawler(new CrawlerMybatisDao()).start();

    }
}
