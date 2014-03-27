package com.talentica.platform.eBay;

import com.talentica.platform.common.CommonConfig;
import com.talentica.platform.common.MarketplaceBase;

/**
 * Created by AmitG on 20-03-2014.
 */
public class Ebay extends MarketplaceBase {

    public void crawl(CommonConfig commonConfig) {
        String strategyName = commonConfig.getStrategyName();
        if (strategyName != null) {
            EbayStrategyBase ebayStrategy = EbayStrategyFactory.getCrawler(strategyName);
            ebayStrategy.start(commonConfig);
        }
    }
}
