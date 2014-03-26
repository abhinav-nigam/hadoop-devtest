package com.talentica.platform.eBay.robotstxt;

import com.talentica.platform.Main;
import com.talentica.platform.common.CommonConfig;
import com.talentica.platform.common.RequestHandler;
import com.talentica.platform.eBay.EbayStrategyBase;
import com.talentica.platform.eBay.robotstxt.model.Sitemap;
import com.talentica.platform.eBay.robotstxt.model.SitemapIndex;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by AmitG on 20-03-2014.
 */
public class EbayRobotstxtCrawler extends EbayStrategyBase {

    private static Logger LOGGER = Logger.getLogger(EbayRobotstxtCrawler.class);
    private final int URL_RETRIEVER_WEIGHTAGE = 10;
    private final int DATA_EXTRACTOR_WEIGHTAGE = 90;

    private AtomicBoolean isUrlRetrieverFinished = new AtomicBoolean(false);

    private ConcurrentLinkedQueue<String> sitemapUrlsQueue;
    private ConcurrentLinkedQueue<String> listingUrlsQueue;

    public EbayRobotstxtCrawler() {
        this.sitemapUrlsQueue = new ConcurrentLinkedQueue<String>();
        this.listingUrlsQueue = new ConcurrentLinkedQueue<String>();
    }

    public void start(CommonConfig commonConfig) {
        LOGGER.debug("Ebay Robots txt data crawler started");
        // TODO - need to read it from configuration file
        String url = "http://www.ebay.com/lst/VIP_US_index.xml";
        String xml = new RequestHandler().getResponse(url);
        try {
            if (xml != null) {
                final JAXBContext context = JAXBContext.newInstance(Sitemap.class, SitemapIndex.class);
                final Unmarshaller unmarshaller = context.createUnmarshaller();
                final SitemapIndex sitemapIndex = (SitemapIndex) unmarshaller.unmarshal(new StreamSource(new StringReader(xml)));
                if (sitemapIndex != null) {
                    ArrayList<Sitemap> sitemaps = sitemapIndex.getSitemaps();
                    for (Sitemap sitemap : sitemaps) {
                        String loc = sitemap.getLoc();
                        if (loc != null) {
                            sitemapUrlsQueue.add(loc);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        LOGGER.debug("Ebay Robots txt data crawler Finished");
        String processingDir = commonConfig.getProcessingDir();
        startWorkers(processingDir);
    }

    public void startWorkers(String processingDir) {
        LOGGER.debug("Starting workers for Ebay Robots txt data crawler");
        int threadCount = Runtime.getRuntime().availableProcessors();
        long urlRetrieverThreadCount = Math.round((threadCount * URL_RETRIEVER_WEIGHTAGE)/100.0);
        long dataRetrieverThreadCount = Math.round((threadCount * DATA_EXTRACTOR_WEIGHTAGE)/100.0);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        Runnable urlRetriever = new UrlRetriever(sitemapUrlsQueue, listingUrlsQueue, urlRetrieverThreadCount, isUrlRetrieverFinished, processingDir);
        for (int i = 0; i < urlRetrieverThreadCount; i++) {
            executor.execute(urlRetriever);
        }

        Runnable dataExtractor = new DataExtractor(listingUrlsQueue, isUrlRetrieverFinished, processingDir);
        for (int i = 0; i < dataRetrieverThreadCount; i++) {
            executor.execute(dataExtractor);
        }
        executor.shutdown();
    }
}
