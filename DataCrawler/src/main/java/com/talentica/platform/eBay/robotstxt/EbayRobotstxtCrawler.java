package com.talentica.platform.eBay.robotstxt;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.talentica.platform.common.CommonConfig;
import com.talentica.platform.common.RequestHandler;
import com.talentica.platform.eBay.EbayStrategyBase;
import com.talentica.platform.eBay.robotstxt.config.RobotstxtConfig;
import com.talentica.platform.eBay.robotstxt.dao.listings.ListingDao;
import com.talentica.platform.eBay.robotstxt.dao.listings.ListingQueries;
import com.talentica.platform.eBay.robotstxt.dao.sitemaps.SitemapDao;
import com.talentica.platform.eBay.robotstxt.dao.sitemaps.SitemapQueries;
import com.talentica.platform.eBay.robotstxt.model.Sitemap;
import com.talentica.platform.eBay.robotstxt.model.SitemapIndex;
import org.apache.log4j.Logger;
import org.skife.jdbi.v2.DBI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by AmitG on 20-03-2014.
 */
public class EbayRobotstxtCrawler extends EbayStrategyBase {

    private static Logger LOGGER = Logger.getLogger(EbayRobotstxtCrawler.class);
    private int URL_RETRIEVER_WEIGHTAGE = 10;
    private int DATA_EXTRACTOR_WEIGHTAGE = 90;

    private final String CONFIG_FILE_PATH = "\\src\\main\\java\\com\\talentica\\platform\\eBay\\robotstxt\\config\\robotstxt-configuration.yml";

    private AtomicBoolean isUrlRetrieverFinished = new AtomicBoolean(false);
    private SitemapDao sitemapDao;
    private ListingDao listingDao;
    private RobotstxtConfig robotstxtConfig;

    public EbayRobotstxtCrawler() {
        final String sourceDir = System.getProperty("user.dir");
        this.robotstxtConfig = loadConfiguration(sourceDir + File.separator + CONFIG_FILE_PATH);
        DBI dbi = new DBI(robotstxtConfig.getDbUrl());
        SitemapQueries sitemapQueries = dbi.onDemand(SitemapQueries.class);
        sitemapDao = new SitemapDao(sitemapQueries);
        ListingQueries listingQueries = dbi.onDemand(ListingQueries.class);
        listingDao = new ListingDao(listingQueries);
    }

    public void start(CommonConfig commonConfig) {
        LOGGER.debug("Ebay Robots txt data crawler started");
        String processingDir = commonConfig.getProcessingDir();
        startWorkers(processingDir);
        LOGGER.debug("Ebay Robots txt data crawler Finished");
    }

    private void getNewEntriesFromSitemap() {
        String url = robotstxtConfig.getSitemapUrl();
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
                            Pattern p = Pattern.compile("(\\d{4}|\\d{2})-(\\d{1,2})-(\\d{1,2})");
                            Matcher m = p.matcher(loc);
                            while (m.find()) {
                                String timestamp = m.group();
                                try {
                                    sitemapDao.insertSitemapToQueue(loc, Timestamp.valueOf(timestamp + " 00:00:00"), 1);
                                } catch (Exception ex) {
                                    LOGGER.error(ex.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void startWorkers(String processingDir) {
        LOGGER.debug("Starting workers for Ebay Robots txt data crawler");
        int threadCount = Runtime.getRuntime().availableProcessors();
        if (!robotstxtConfig.isShouldExecuteListingsRetriever() && !robotstxtConfig.isShouldExecuteUrlRetriever()) {
            URL_RETRIEVER_WEIGHTAGE = 0;
            DATA_EXTRACTOR_WEIGHTAGE = 0;
        } else if (!robotstxtConfig.isShouldExecuteListingsRetriever()) {
            URL_RETRIEVER_WEIGHTAGE = 100;
            DATA_EXTRACTOR_WEIGHTAGE = 0;
        } else if (!robotstxtConfig.isShouldExecuteUrlRetriever()) {
            URL_RETRIEVER_WEIGHTAGE = 0;
            DATA_EXTRACTOR_WEIGHTAGE = 100;
        }
        long urlRetrieverThreadCount = Math.round((threadCount * URL_RETRIEVER_WEIGHTAGE) / 100.0);
        long dataRetrieverThreadCount = Math.round((threadCount * DATA_EXTRACTOR_WEIGHTAGE) / 100.0);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        if (urlRetrieverThreadCount == 0 && robotstxtConfig.isShouldExecuteUrlRetriever()) {
            urlRetrieverThreadCount = 1;
        }
        if (dataRetrieverThreadCount == 0 && robotstxtConfig.isShouldExecuteListingsRetriever()) {
            dataRetrieverThreadCount = 1;
        }
        if(urlRetrieverThreadCount > 0) {
            this.getNewEntriesFromSitemap();
            Runnable urlRetriever = new UrlRetriever(sitemapDao, listingDao, urlRetrieverThreadCount, isUrlRetrieverFinished, processingDir);
            for (int i = 0; i < urlRetrieverThreadCount; i++) {
                executor.execute(urlRetriever);
            }
        }
        if(dataRetrieverThreadCount > 0) {
            Runnable dataExtractor = new DataExtractor(listingDao, isUrlRetrieverFinished, processingDir);
            for (int i = 0; i < dataRetrieverThreadCount; i++) {
                executor.execute(dataExtractor);
            }
            executor.shutdown();
        }
    }

    private RobotstxtConfig loadConfiguration(String filePath) {
        RobotstxtConfig config = new RobotstxtConfig();
        Yaml yaml = new Yaml();
        try {
            InputStream in = Files.newInputStream(Paths.get(filePath));
            config = yaml.loadAs(in, RobotstxtConfig.class);
        } catch (IOException e) {
            LOGGER.error("Error in opening the file");
        }
        return config;
    }
}
