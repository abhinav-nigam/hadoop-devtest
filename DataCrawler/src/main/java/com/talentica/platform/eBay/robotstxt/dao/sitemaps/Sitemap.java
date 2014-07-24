package com.talentica.platform.eBay.robotstxt.dao.sitemaps;

import java.sql.Timestamp;

/**
 * Created by AmitG on 27-03-2014.
 */
public class Sitemap {
    private long id;
    private String url;
    private Timestamp timestamp;
    private int status;

    public Sitemap(long id, String url, Timestamp timestamp, int status) {
        this.id = id;
        this.url = url;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
