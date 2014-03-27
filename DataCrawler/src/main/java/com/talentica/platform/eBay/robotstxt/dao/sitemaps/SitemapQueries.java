package com.talentica.platform.eBay.robotstxt.dao.sitemaps;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by AmitG on 27-03-2014.
 */
public interface SitemapQueries {

    @SqlUpdate("INSERT INTO sitemap_urls_queue (url, gz_timestamp, status) VALUES (:url, :timestamp, :status)")
    void insert(@Bind("url") String url, @Bind("timestamp") Timestamp gz_timestamp, @Bind("status") int status);

    @RegisterMapper(SitemapMapper.class)
    @SqlQuery("UPDATE sitemap_urls_queue SET status = 2 WHERE url IN (SELECT url FROM sitemap_urls_queue WHERE status = 1 or status = 4 LIMIT :batchCount ) returning *")
    List<Sitemap> updateSelect(@Bind("batchCount") int batchCount);

    @SqlUpdate("UPDATE sitemap_urls_queue SET status = :status WHERE id = :id")
    void updateStatus(@Bind("id") long id, @Bind("status") int status);


}
