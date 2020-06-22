package com.hao.dao;

import com.hao.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class CrawlerMybatisDao implements CrawlerDao {

    private SqlSessionFactory sessionFactory;

    public CrawlerMybatisDao() {
        String resource = "db/mybatis/mybatis-config.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            this.sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("read mybatis file failed :" + resource, e);
        }
    }

    @Override
    public void addLinkAlreadyProcessed(String link) throws SQLException {
        try (SqlSession session = sessionFactory.openSession(true)) {
            session.insert("com.hao.CrawlerMapper.insertLinkAlreadyProcessed", link);
        }
    }

    @Override
    public void addLinkToBeProcessed(String link) throws SQLException {
        try (SqlSession session = sessionFactory.openSession(true)) {
            session.insert("com.hao.CrawlerMapper.insertLinkToBeProcessed", link);
        }
    }

    @Override
    public void storeNews(Document doc) {
        String title = doc.select("h1").text();
        String content = doc.select(".art_content").text();
        News news = new News(title, content);
        try (SqlSession session = sessionFactory.openSession(true)) {
            session.insert("com.hao.CrawlerMapper.storeNews", news);
        }
    }

    @Override
    public boolean isLinkAlreadyProcessed(String link) {
        try (SqlSession session = sessionFactory.openSession(true)) {
            int count = session.selectOne("com.hao.CrawlerMapper.selectCountFromAlready", link);
            return count != 0;
        }
    }

    @Override
    public synchronized String getAndRemoveToBeProcessedLink() {
        try (SqlSession session = sessionFactory.openSession(true)) {
            String link = session.selectOne("com.hao.CrawlerMapper.selectOneLink");
            if (link != null) {
                session.delete("com.hao.CrawlerMapper.deleteToBeProcessedLink", link);
            }
            return link;
        }
    }

}
