package com.talentica.platform.eBay.robotstxt.dao.listings;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by AmitG on 27-03-2014.
 */

public class ListingMapper implements ResultSetMapper<Listing> {
    public Listing map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Listing(r.getLong("id"), r.getString("url"), r.getInt("status"));
    }
}


