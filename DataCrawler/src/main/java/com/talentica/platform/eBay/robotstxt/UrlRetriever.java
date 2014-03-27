package com.talentica.platform.eBay.robotstxt;

import com.talentica.platform.common.UrlDownloader;
import com.talentica.platform.eBay.robotstxt.dao.listings.ListingDao;
import com.talentica.platform.eBay.robotstxt.dao.sitemaps.Sitemap;
import com.talentica.platform.eBay.robotstxt.dao.sitemaps.SitemapDao;
import com.talentica.platform.eBay.robotstxt.model.Image;
import com.talentica.platform.eBay.robotstxt.model.Url;
import com.talentica.platform.eBay.robotstxt.model.UrlSet;
import com.talentica.platform.util.ReadGZIP;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by AmitG on 21-03-2014.
 */

public class UrlRetriever implements Runnable {

    private static Logger LOGGER = Logger.getLogger(UrlRetriever.class);

    private ListingDao listingDao;
    private AtomicBoolean isUrlRetrieverFinished;
    private long totalThreads;
    private String processingDir;
    private SitemapDao sitemapDao;

    public UrlRetriever(SitemapDao sitemapDao, ListingDao listingDao, long totalThreads, AtomicBoolean isUrlRetrieverFinished, String processingDir) {
        this.listingDao = listingDao;
        this.isUrlRetrieverFinished = isUrlRetrieverFinished;
        this.totalThreads = totalThreads;
        this.processingDir = processingDir;
        this.sitemapDao = sitemapDao;
    }

    @Override
    public void run() {
        while (true) {
            List<Sitemap> sitemapList = sitemapDao.getSitemapForProcessing(5);
            if (!sitemapList.isEmpty()) {
                for (Sitemap sitemap : sitemapList) {
                    this.doWork(sitemap);
                }
            } else {
                totalThreads -= 1;
                break;
            }
        }
        if (totalThreads == 0) {
            isUrlRetrieverFinished.set(true);
        }
    }

    private void doWork(Sitemap sitemap) {
        try {
            String loc = sitemap.getUrl();
            LOGGER.debug("Started processing for url  : " + loc);
            String destinationDir = processingDir + File.separator + "ebay";
            LOGGER.debug("Download started for  : " + loc);
            new UrlDownloader().fileDownload(loc, destinationDir);
            LOGGER.debug("Download Finished for  : " + loc);
            String strSourceFilePath = destinationDir + File.separatorChar + loc.substring(loc.lastIndexOf("/") + 1);
            try {
                String extractedFilePath = ReadGZIP.unzipGZ(strSourceFilePath, destinationDir);
                LOGGER.debug("Processing Started for  : " + loc);
                this.processXml(extractedFilePath);
                LOGGER.debug("Processing Finished for  : " + loc);
            } catch (IOException ex) {
                LOGGER.error("Error in Extracting file : " + strSourceFilePath + " :: " + ex.getMessage());
                throw ex;
            }
            sitemapDao.updateStatus(sitemap.getId(), 3);
        } catch (Exception ex) {
            sitemapDao.updateStatus(sitemap.getId(), 4);
        }
    }

    private void processXml(String xmlFilePath) throws Exception {
        try {
            File inputFile = new File(xmlFilePath);
            final JAXBContext context = JAXBContext.newInstance(UrlSet.class, Url.class, Image.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final UrlSet urlSet = (UrlSet) unmarshaller.unmarshal(new StreamSource(inputFile));
            if (urlSet != null) {
                urlSet.getUrls().get(0).getLoc();
                ArrayList<Url> urls = urlSet.getUrls();
                for (Url url : urls) {
                    String loc = url.getLoc();
                    if (loc != null) {
                        try {
                            listingDao.insertListingToQueue(loc, 1);
                        } catch (Exception ex) {
                            LOGGER.error(ex.getMessage());
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Error in processing xml file : " + xmlFilePath + " :: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }
}
