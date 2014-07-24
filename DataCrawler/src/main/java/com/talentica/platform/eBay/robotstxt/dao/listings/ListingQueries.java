package com.talentica.platform.eBay.robotstxt.dao.listings;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

/**
 * Created by AmitG on 27-03-2014.
 */
public interface ListingQueries {

    @SqlUpdate("INSERT INTO listings_urls_queue (url, status) VALUES (:url, :status)")
    void insert(@Bind("url") String url, @Bind("status") int status);

    @RegisterMapper(ListingMapper.class)
    @SqlQuery("UPDATE listings_urls_queue SET status = 2 WHERE url IN (SELECT url FROM listings_urls_queue WHERE status = 1 or status = 4 LIMIT :batchCount ) returning *")
    List<Listing> updateSelect(@Bind("batchCount") int batchCount);

    @SqlUpdate("UPDATE listings_urls_queue SET status = :status WHERE id = :id")
    void updateStatus(@Bind("id") long id, @Bind("status") int status);

}
