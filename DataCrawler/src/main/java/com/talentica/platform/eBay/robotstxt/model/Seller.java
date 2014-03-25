package com.talentica.platform.eBay.robotstxt.model;

/**
 * Created by AmitG on 24-03-2014.
 */
public class Seller {

    private String sellerName;
    private int rating;
    private String feedback;

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
