package com.talentica.platform.eBay.robotstxt.dao.sitemaps;


import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by AmitG on 27-03-2014.
 */

public class SitemapMapper implements ResultSetMapper<Sitemap> {
    public Sitemap map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Sitemap(r.getLong("id"), r.getString("url"), r.getTimestamp("gz_timestamp"), r.getInt("status"));
    }
}

