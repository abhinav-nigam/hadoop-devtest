package com.talentica.platform.eBay;

import com.talentica.platform.eBay.robotstxt.EbayRobotstxtCrawler;

/**
 * Created by AmitG on 20-03-2014.
 */
public class EbayStrategyFactory {

    public enum ebayStrategy {ROBOTSTXT, UISEARCH, API}

    public static EbayStrategyBase getCrawler(String strategyName) {
        EbayStrategyBase ebayStrategyBase = null;
        if (strategyName != null) {
            if (strategyName.equalsIgnoreCase(ebayStrategy.ROBOTSTXT.toString())) {
                ebayStrategyBase = new EbayRobotstxtCrawler();
            } else if (strategyName.equalsIgnoreCase(ebayStrategy.UISEARCH.toString())) {
                System.out.println("will implement later");
            } else if (strategyName.equalsIgnoreCase(ebayStrategy.API.toString())) {
                System.out.println("will implement later");
            }
        }
        return ebayStrategyBase;
    }
}
