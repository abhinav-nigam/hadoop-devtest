package com.talentica.platform.eBay.robotstxt.dao.listings;

/**
 * Created by AmitG on 27-03-2014.
 */
public class Listing {

    private String url;
    private long id;
    private int status;

    public Listing(long id, String url, int status) {
        this.id = id;
        this.url = url;
        this.status = status;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
