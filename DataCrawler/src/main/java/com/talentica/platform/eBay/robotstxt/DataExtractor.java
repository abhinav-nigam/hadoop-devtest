package com.talentica.platform.eBay.robotstxt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentica.platform.common.RequestHandler;
import com.talentica.platform.eBay.robotstxt.model.Listing;
import com.talentica.platform.eBay.robotstxt.model.Seller;
import org.apache.log4j.Logger;
import org.htmlcleaner.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by AmitG on 21-03-2014.
 */

public class DataExtractor implements Runnable {
    // TODO add all hard coded string to some constants file
    private static Logger LOGGER = Logger.getLogger(DataExtractor.class);

    private ConcurrentLinkedQueue<String> listingUrlsQueue;
    private AtomicBoolean isUrlRetrieverFinished;
    private String processingDir;

    public DataExtractor(ConcurrentLinkedQueue<String> listingUrlsQueue, AtomicBoolean isUrlRetrieverFinished, String processingDir) {

        this.listingUrlsQueue = listingUrlsQueue;
        this.isUrlRetrieverFinished = isUrlRetrieverFinished;
        this.processingDir = processingDir;
    }

    @Override
    public void run() {
        while (!isUrlRetrieverFinished.get()) {
            try {
                if (!listingUrlsQueue.isEmpty()) {
                    String url = listingUrlsQueue.poll();
                    this.doWork(url);
                } else {
                    Thread.sleep((long) 10);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doWork(String url) {
        LOGGER.debug("Started Data Extraction for Listing Url : " + url);
        try {
            extractData(url);
        } catch (Exception ex) {
            ex.getMessage();
            LOGGER.error("Error in extracting data for url = " + url);
        }

        LOGGER.debug("Finished Data Extraction for Listing Url : " + url);
    }

    private void extractData(String url) throws Exception {
        String htmlResponse = new RequestHandler().getResponse(url);

        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties props = cleaner.getProperties();
        props.setPruneTags("script,style");
        TagNode rootHtml = cleaner.clean(htmlResponse);

        Listing listing = new Listing();

        String categoryTree = getCategoryTree(rootHtml);
        listing.setCategoryTree(categoryTree);

        String title = getTitle(rootHtml);
        listing.setTitle(title);
        listing.setItemId(url.substring(url.lastIndexOf("/") + 1));

        TagNode[] leftSummaryPanelArr = rootHtml.getElementsByAttValue("id", "LeftSummaryPanel", true, true);

        // Added to handle listings that has been expired
        if (leftSummaryPanelArr != null && leftSummaryPanelArr.length == 0) {
            leftSummaryPanelArr = rootHtml.getElementsByAttValue("id", "CenterPanelInternal", true, true);
        }

        if (leftSummaryPanelArr != null) {
            for (TagNode leftSummaryPanel : leftSummaryPanelArr) {
                org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM(leftSummaryPanel);
                XPath xpath = XPathFactory.newInstance().newXPath();
                String itemCondition = (String) xpath.evaluate("//div[@id='vi-itm-cond']/text()|//div[@id='vi-itm-cond']",
                        doc, XPathConstants.STRING);
                listing.setItemCondition(itemCondition.trim());
                String timeLeft = (String) xpath.evaluate("//span[@id='bb_tlft']",
                        doc, XPathConstants.STRING);
                listing.setTimeLeft(timeLeft.trim());
                String itemPrice = (String) xpath.evaluate("//span[@id='prcIsum_bidPrice']|//span[@id='prcIsum']|//span[@id='mm-saleDscPrc']|//span[@class='notranslate vi-VR-cvipPrice']",
                        doc, XPathConstants.STRING);
                listing.setBidPrice(itemPrice.trim());

                String bidCount = (String) xpath.evaluate("//span[@id='qty-test']|//a[@id='vi-VR-bid-lnk']/span[1]",
                        doc, XPathConstants.STRING);
                if (bidCount != null && !bidCount.isEmpty()) {
                    listing.setBidCount(Integer.parseInt(bidCount));
                }
                String itemLocation = (String) xpath.evaluate("//div[@id='itemLocation']/div[@class='u-flL iti-w75']/div[@class='iti-eu-txt iti-spacing']/div[@class='iti-eu-bld-gry']|//div[@class='vi-cviprow']/div[2]/div[@class='u-flL']",
                        doc, XPathConstants.STRING);

                listing.setItemLocation(itemLocation.trim());

                String shipsTo = (String) xpath.evaluate("//div[@id='vi-acc-shpsToLbl-cnt']",
                        doc, XPathConstants.STRING);
                listing.setShipsTo(shipsTo.trim());

                String shipping = (String) xpath.evaluate("//span[@id='shSummary']/span[1]",
                        doc, XPathConstants.STRING);

                Object[] nodes = leftSummaryPanel.evaluateXPath("//span[@id='shSummary']");
                for (Object node : nodes) {
                    TagNode tagNode = (TagNode) node;
                    shipping = tagNode.getText().toString();
                }
                listing.setShipping(shipping.trim());

                String delivery = (String) xpath.evaluate("//span[@id='delSummary']",
                        doc, XPathConstants.STRING);
                listing.setDelivery(delivery.trim());

                String returns = (String) xpath.evaluate("//span/span[@id='vi-ret-accrd-txt']",
                        doc, XPathConstants.STRING);
                listing.setReturns(returns.trim());

                String paymentMethod = (String) xpath.evaluate("//div[@id='payDet1']/img[@class='pd-img']/@alt",
                        doc, XPathConstants.STRING);
                listing.setPaymentMethod(paymentMethod.trim());

                String guarantee = (String) xpath.evaluate("//div[@class='u-flL  rpColWid ']/table[@class='vi-ebp2-logo-tbl']/tbody/tr[2]/td|//div[@class='u-flL  rpColWid ']/table[@class='vi-ebp2-logo-tbl']/tbody/tr[3]/td",
                        doc, XPathConstants.STRING);
                listing.setGuarantee(guarantee.trim());
            }
        }

        TagNode[] rightSummaryPanelArr = rootHtml.getElementsByAttValue("id", "RightSummaryPanel", true, true);
        if (rightSummaryPanelArr != null && rightSummaryPanelArr.length == 0) {
            rightSummaryPanelArr = leftSummaryPanelArr;
        }

        Seller seller = getSeller(rightSummaryPanelArr);
        listing.setSeller(seller);

        Dictionary<String, String> itemSpecifics = getItemSpecifics(rootHtml);
        listing.setItemSpecifications(itemSpecifics);

        // TODO add logic to parse in csv
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("C:\\Users\\AmitG\\Downloads\\ebay\\data\\" + listing.getItemId() + ".json"), listing);
    }

    private String getCategoryTree(TagNode rootHtml) throws XPatherException {
        String categoryTree = "";
        Object[] categories = rootHtml.evaluateXPath("//td[@id='vi-VR-brumb-lnkLst']");
        for (Object category : categories) {
            TagNode tagNode = (TagNode) category;
            List aTags = tagNode.getElementListByName("a", true);

            for (Object aTag : aTags) {
                TagNode keyTag = (TagNode) aTag;
                String key = keyTag.getText().toString().trim();
                categoryTree += key + "|";
            }
        }
        return categoryTree.substring(0, categoryTree.lastIndexOf("|"));
    }

    private String getTitle(TagNode rootHtml) throws XPatherException {
        String title = "";
        Object[] titleNodes = rootHtml.evaluateXPath("//h1[@id='itemTitle']");
        for (Object titleNode : titleNodes) {
            TagNode tagNode = (TagNode) titleNode;
            title = tagNode.getText().toString().trim();
        }
        return title;
    }

    private Seller getSeller(TagNode[] tagNodeArr) throws XPatherException, ParserConfigurationException, XPathExpressionException {
        Seller seller = new Seller();
        if (tagNodeArr != null) {
            for (TagNode rightSummaryPanel : tagNodeArr) {
                org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM(rightSummaryPanel);
                XPath xpath = XPathFactory.newInstance().newXPath();
                String sellerName = (String) xpath.evaluate("//div[@class='si-cnt si-cnt-eu vi-grBr vi-padn0 c-std']/div/div[@class='si-inner']/div[@class='si-content']/div[contains(@class,'bdg-')]/div[@class='mbg vi-VR-margBtm3']/a/span[@class='mbg-nw']|//div[@class='mbg vi-VR-margBtm3']/a/span[@class='mbg-nw']",
                        doc, XPathConstants.STRING);
                seller.setSellerName(sellerName.trim());

                String rating = (String) xpath.evaluate("//div[@class='si-cnt si-cnt-eu vi-grBr vi-padn0 c-std']/div/div[@class='si-inner']/div[@class='si-content']/div[contains(@class,'bdg-')]/div[@class='mbg vi-VR-margBtm3']/span[@class='mbg-l']/a|//div[@class='mbg vi-VR-margBtm3']/span[@class='mbg-l']/a",
                        doc, XPathConstants.STRING);
                seller.setRating(Integer.parseInt(rating.trim()));

                String feedback = (String) xpath.evaluate("//div[@class='si-cnt si-cnt-eu vi-grBr vi-padn0 c-std']/div/div[@class='si-inner']/div[@class='si-content']/div[contains(@class,'bdg-')]/div[@id='si-fb']",
                        doc, XPathConstants.STRING);
                seller.setFeedback(feedback.trim());
            }
        }
        return seller;
    }

    private Dictionary<String, String> getItemSpecifics(TagNode rootHtml) throws XPatherException {
        Object[] nodes = rootHtml.evaluateXPath("//div[@id='vi-desc-maincntr']/div[@class='itemAttr']");
        Dictionary<String, String> itemSpecifics = new Hashtable<String, String>();
        for (Object node : nodes) {
            TagNode tagNode = (TagNode) node;
            List tags = tagNode.getElementListByName("td", true);
            if (tags.size() % 2 != 0) {
                tags.remove(tags.size() - 1);
            }
            for (int j = 0; j < tags.size(); j += 2) {
                TagNode keyTag = (TagNode) tags.get(j);
                String key = keyTag.getText().toString().trim();
                TagNode valueTag = (TagNode) tags.get(j + 1);
                String value = valueTag.getText().toString().trim();

                itemSpecifics.put(key.substring(0, key.lastIndexOf(":")), value);

            }
        }
        return itemSpecifics;
    }
}
