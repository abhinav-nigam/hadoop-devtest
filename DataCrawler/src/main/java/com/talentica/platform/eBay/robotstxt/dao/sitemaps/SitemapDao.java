package com.talentica.platform.eBay.robotstxt.dao.sitemaps;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by AmitG on 27-03-2014.
 */
public class SitemapDao {
    private SitemapQueries sitemapQueries;

    public SitemapDao(SitemapQueries sitemapQueries) {
        this.sitemapQueries = sitemapQueries;
    }

    public void insertSitemapToQueue(String url, Timestamp gz_timestamp, int status) {
        sitemapQueries.insert(url, gz_timestamp, status);
    }

    public synchronized List<Sitemap> getSitemapForProcessing(int batchCount) {
        return sitemapQueries.updateSelect(batchCount);
    }

    public void updateStatus(long id, int status) {
        sitemapQueries.updateStatus(id, status);
    }
}
