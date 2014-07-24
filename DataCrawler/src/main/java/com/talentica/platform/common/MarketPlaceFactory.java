package com.talentica.platform.common;

import com.talentica.platform.eBay.Ebay;

/**
 * Created by AmitG on 20-03-2014.
 */
public class MarketPlaceFactory {

    public enum marketplace {EBAY, AMAZON}

    public static MarketplaceBase getTargetMarketplace(String marketplaceName) {
        MarketplaceBase marketplaceBase = null;
        if (marketplaceName != null) {
            if (marketplaceName.equalsIgnoreCase(marketplace.EBAY.toString())) {
                marketplaceBase = new Ebay();
            } else if (marketplaceName.equalsIgnoreCase(marketplace.AMAZON.toString())) {
                System.out.println("Some Other Marketplace");
            }
        }
        return marketplaceBase;
    }
}
