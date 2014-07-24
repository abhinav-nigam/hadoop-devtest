package com.talentica.platform.eBay.robotstxt.dao.listings;

import java.util.List;

/**
 * Created by AmitG on 27-03-2014.
 */
public class ListingDao {
    private ListingQueries listingQueries;

    public ListingDao(ListingQueries listingQueries) {
        this.listingQueries = listingQueries;
    }

    public void insertListingToQueue(String url, int status) {
        listingQueries.insert(url, status);
    }

    public synchronized List<Listing> getListingsForProcessing(int batchCount) {
        return listingQueries.updateSelect(batchCount);
    }

    public void updateStatus(long id, int status) {
        listingQueries.updateStatus(id, status);
    }
}
