package com.talentica.platform.eBay.robotstxt;

import com.talentica.platform.common.UrlDownloader;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by AmitG on 21-03-2014.
 */

public class UrlRetriever implements Runnable {

    private static Logger LOGGER = Logger.getLogger(UrlRetriever.class);

    private ConcurrentLinkedQueue<String> sitemapUrlsQueue;
    private ConcurrentLinkedQueue<String> listingUrlsQueue;
    private AtomicBoolean isUrlRetrieverFinished;
    private long totalThreads;
    private String processingDir;

    public UrlRetriever(ConcurrentLinkedQueue<String> sitemapUrlsQueue, ConcurrentLinkedQueue<String> listingUrlsQueue, long totalThreads, AtomicBoolean isUrlRetrieverFinished, String processingDir) {
        this.sitemapUrlsQueue = sitemapUrlsQueue;
        this.listingUrlsQueue = listingUrlsQueue;
        this.isUrlRetrieverFinished = isUrlRetrieverFinished;
        this.totalThreads = totalThreads;
        this.processingDir = processingDir;
    }

    @Override
    public void run() {
        while (true) {
            if(!sitemapUrlsQueue.isEmpty()){
                String loc = sitemapUrlsQueue.poll();
                this.doWork(loc);
            }else{
                totalThreads-=1;
                break;
            }
        }
        if(totalThreads ==0){
            isUrlRetrieverFinished = new AtomicBoolean(true);
        }
    }

    private void doWork(String loc) {
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
        }
    }

    private void processXml(String xmlFilePath) {
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
                        listingUrlsQueue.add(loc);
                    }
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Error in processing xml file : " + xmlFilePath + " :: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
