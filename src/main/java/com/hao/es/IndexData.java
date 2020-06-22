package com.hao.es;

import com.hao.News;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//1.索引所有的Mysql数据到Es
//2.单个索引添加
public class IndexData {

    public static void main(String[] args) throws IOException {
        migrate();
    }

    private static void migrate() throws IOException {

        SqlSessionFactory sessionFactory = getSessionFactory();
        List<News> newses;
        try (SqlSession session = sessionFactory.openSession(true)) {
            newses = session.selectList("com.hao.CrawlerMapper.allNews");
        }
        if (newses != null && newses.size() > 0) {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost("localhost", 9200, "http")
                            ));
            IndexRequest request = new IndexRequest("news");
            int count = 0;
            for (News news : newses) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", news.getId());
                map.put("title", news.getTitle());
                map.put("content", news.getContent());
                Random r = new Random();
                map.put("created_at", news.getCreatedAt().plusSeconds(r.nextInt(60*60*24*365)));
                map.put("updated_at", news.getUpdatedAt().plusSeconds(r.nextInt(60*60*24*365)));
                request.source(map, XContentType.JSON);
                client.index(request, RequestOptions.DEFAULT);
                System.out.println("index: " + (++count));
            }
            client.close();
        }

    }

    private static SqlSessionFactory getSessionFactory() {
        String resource = "db/mybatis/mybatis-config.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            return new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("read mybatis file failed :" + resource, e);
        }
    }
}
