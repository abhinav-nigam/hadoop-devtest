package com.talentica.platform.eBay.robotstxt;

import com.talentica.platform.common.RequestHandler;

/**
 * Created by AmitG on 26-03-2014.
 */
public class RobotstxtRequestHandler extends RequestHandler {
    private final int MAX_RETRY_COUNT = 5;

    public String getEbayListingPage(String url) throws InterruptedException {
        String response = "";
        int retryCount = 0;
        while (true) {
            response = getResponse(url);
            if (response != null && !response.isEmpty()) {
                break;
            } else if (retryCount > MAX_RETRY_COUNT) {
                break;
            } else {
                // Sleep for some time then try to get the response
                Thread.sleep((long) 2000);
            }
            retryCount += 1;
        }
        return response;
    }
}
