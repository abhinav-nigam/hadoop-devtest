package com.talentica.platform.eBay.robotstxt.config;

/**
 * Created by AmitG on 27-03-2014.
 */
public class RobotstxtConfig {

    private String dbUrl;
    private boolean shouldExecuteUrlRetriever;
    private boolean shouldExecuteListingsRetriever;
    private String sitemapUrl;

    public String getSitemapUrl() {
        return sitemapUrl;
    }

    public void setSitemapUrl(String sitemapUrl) {
        this.sitemapUrl = sitemapUrl;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public boolean isShouldExecuteUrlRetriever() {
        return shouldExecuteUrlRetriever;
    }

    public void setShouldExecuteUrlRetriever(boolean shouldExecuteUrlRetriever) {
        this.shouldExecuteUrlRetriever = shouldExecuteUrlRetriever;
    }

    public boolean isShouldExecuteListingsRetriever() {
        return shouldExecuteListingsRetriever;
    }

    public void setShouldExecuteListingsRetriever(boolean shouldExecuteListingsRetriever) {
        this.shouldExecuteListingsRetriever = shouldExecuteListingsRetriever;
    }
}
