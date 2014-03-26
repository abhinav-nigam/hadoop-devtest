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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by AmitG on 21-03-2014.
 */

public class DataExtractor implements Runnable {
    private static Logger LOGGER = Logger.getLogger(DataExtractor.class);

    private ConcurrentLinkedQueue<String> listingUrlsQueue;
    private AtomicBoolean isUrlRetrieverFinished;
    private String processingDir;
    private FileOutputStream fileOutputStream;

    public DataExtractor(ConcurrentLinkedQueue<String> listingUrlsQueue, AtomicBoolean isUrlRetrieverFinished, String processingDir) {

        this.listingUrlsQueue = listingUrlsQueue;
        this.isUrlRetrieverFinished = isUrlRetrieverFinished;
        this.processingDir = processingDir;
        this.createDataFile();
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
            ex.printStackTrace();
            LOGGER.error("Error in extracting data for url = " + url);
        }

        LOGGER.debug("Finished Data Extraction for Listing Url : " + url);
    }

    private void extractData(String url) throws Exception {
        String htmlResponse = new RobotstxtRequestHandler().getEbayListingPage(url);
        if (htmlResponse != null && !htmlResponse.isEmpty()) {
            HtmlCleaner cleaner = new HtmlCleaner();
            CleanerProperties props = cleaner.getProperties();
            props.setPruneTags("script,style,noscript");
            TagNode rootHtml = cleaner.clean(htmlResponse);

            Listing listing = new Listing();

            String categoryTree = getCategoryTree(rootHtml);
            listing.setCategoryTree(categoryTree);

            String title = getTitle(rootHtml);
            listing.setTitle(title);
            listing.setItemId(url.substring(url.lastIndexOf("/") + 1));
            listing.setListingUrl(url);

            listing.setImageUrl(getImageUrl(rootHtml));

            TagNode[] leftSummaryPanelArr = rootHtml.getElementsByAttValue("id", "LeftSummaryPanel", true, true);

            // Added to handle listings that has been expired
            if (leftSummaryPanelArr != null && leftSummaryPanelArr.length == 0) {
                leftSummaryPanelArr = rootHtml.getElementsByAttValue("id", "CenterPanelInternal", true, true);
            }

            if (leftSummaryPanelArr != null) {
                for (TagNode leftSummaryPanel : leftSummaryPanelArr) {
                    org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM(leftSummaryPanel);
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String itemCondition = (String) xpath.evaluate(DataExtractorConstants.XPATH_ITEM_CONDITION,
                            doc, XPathConstants.STRING);
                    listing.setItemCondition(removeSpaces(itemCondition));
                    String timeLeft = (String) xpath.evaluate(DataExtractorConstants.XPATH_ITEM_LEFT,
                            doc, XPathConstants.STRING);
                    listing.setTimeLeft(removeSpaces(timeLeft));
                    String itemPrice = (String) xpath.evaluate(DataExtractorConstants.XPATH_ITEM_PRICE,
                            doc, XPathConstants.STRING);
                    listing.setBidPrice(removeSpaces(itemPrice));

                    String bidCount = (String) xpath.evaluate(DataExtractorConstants.XPATH_BID_COUNT,
                            doc, XPathConstants.STRING);
                    if (bidCount != null && !bidCount.isEmpty()) {
                        listing.setBidCount(Integer.parseInt(bidCount));
                    }
                    String itemLocation = (String) xpath.evaluate(DataExtractorConstants.XPATH_ITEM_LOCATION,
                            doc, XPathConstants.STRING);

                    listing.setItemLocation(removeSpaces(itemLocation));

                    String shipsTo = (String) xpath.evaluate(DataExtractorConstants.XPATH_SHIPS_TO,
                            doc, XPathConstants.STRING);
                    listing.setShipsTo(removeSpaces(shipsTo));

                    String shipping = "";
                    Object[] nodes = leftSummaryPanel.evaluateXPath(DataExtractorConstants.XPATH_SHIPPING);
                    for (Object node : nodes) {
                        TagNode tagNode = (TagNode) node;
                        shipping += tagNode.getText().toString();
                    }
                    listing.setShipping(removeSpaces(shipping));

                    String delivery = (String) xpath.evaluate(DataExtractorConstants.XPATH_DELIVERY,
                            doc, XPathConstants.STRING);
                    listing.setDelivery(removeSpaces(delivery));

                    String returns = (String) xpath.evaluate(DataExtractorConstants.XPATH_RETURNS,
                            doc, XPathConstants.STRING);
                    listing.setReturns(removeSpaces(returns));

                    String paymentMethod = (String) xpath.evaluate(DataExtractorConstants.XPATH_PAYMENT_METHOD,
                            doc, XPathConstants.STRING);
                    listing.setPaymentMethod(removeSpaces(paymentMethod));

                    String guarantee = (String) xpath.evaluate(DataExtractorConstants.XPATH_GUARANTEE,
                            doc, XPathConstants.STRING);
                    listing.setGuarantee(removeSpaces(guarantee));
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
//            ObjectMapper mapper = new ObjectMapper();
//            FileOutputStream outStream = new FileOutputStream(new File("C:\\Users\\AmitG\\Downloads\\ebay\\data\\" + "amit.json"), true);
//            mapper.writeValue(outStream, listing);
            writeListingToFile(listing);
        }
    }


    private String getCategoryTree(TagNode rootHtml) throws XPatherException {
        String categoryTree = "";
        Object[] categories = rootHtml.evaluateXPath(DataExtractorConstants.XPATH_CATEGORY_TREE);
        for (Object category : categories) {
            TagNode tagNode = (TagNode) category;
            List aTags = tagNode.getElementListByName("a", true);

            for (Object aTag : aTags) {
                TagNode keyTag = (TagNode) aTag;
                String key = removeSpaces(keyTag.getText().toString());
                categoryTree += key + " > ";
            }
        }
        return categoryTree.substring(0, categoryTree.lastIndexOf(" > "));
    }

    private String getTitle(TagNode rootHtml) throws XPatherException {
        String title = "";
        Object[] titleNodes = rootHtml.evaluateXPath(DataExtractorConstants.XPATH_TITLE);
        for (Object titleNode : titleNodes) {
            TagNode tagNode = (TagNode) titleNode;
            title = removeSpaces(tagNode.getText().toString());
        }
        return title;
    }

    private Seller getSeller(TagNode[] tagNodeArr) throws XPatherException, ParserConfigurationException, XPathExpressionException {
        Seller seller = new Seller();
        if (tagNodeArr != null) {
            for (TagNode rightSummaryPanel : tagNodeArr) {
                org.w3c.dom.Document doc = new DomSerializer(new CleanerProperties()).createDOM(rightSummaryPanel);
                XPath xpath = XPathFactory.newInstance().newXPath();

                String sellerName = (String) xpath.evaluate(DataExtractorConstants.XPATH_SELLER_NAME, doc, XPathConstants.STRING);
                seller.setSellerName(removeSpaces(sellerName));

                String rating = (String) xpath.evaluate(DataExtractorConstants.XPATH_SELLER_RATING, doc, XPathConstants.STRING);
                seller.setRating(Integer.parseInt(removeSpaces(rating)));

                String feedback = (String) xpath.evaluate(DataExtractorConstants.XPATH_SELLER_FEEDBACK, doc, XPathConstants.STRING);
                seller.setFeedback(removeSpaces(feedback));
            }
        }
        return seller;
    }

    private Dictionary<String, String> getItemSpecifics(TagNode rootHtml) throws XPatherException {
        Object[] nodes = rootHtml.evaluateXPath(DataExtractorConstants.XPATH_ITEM_SPECIFICS);
        Dictionary<String, String> itemSpecifics = new Hashtable<String, String>();
        for (Object node : nodes) {
            TagNode tagNode = (TagNode) node;
            List tables = tagNode.getElementListByName("table", true);
            for (Object table : tables) {
                TagNode tableTag = (TagNode) table;
                List tdTags = tableTag.getElementListByName("td", true);
                if (tableTag.getAttributeByName("id") != null && tableTag.getAttributeByName("id").equalsIgnoreCase("itmSellerDesc")) {
                    List thTags = tableTag.getElementListByName("th", true);
                    if (tdTags.size() == thTags.size()) {
                        for (int k = 0; k < tdTags.size(); k += 1) {
                            TagNode keyTag = (TagNode) thTags.get(k);
                            String key = removeSpaces(keyTag.getText().toString());
                            TagNode valueTag = (TagNode) tdTags.get(k);
                            String value = removeSpaces(valueTag.getText().toString());

                            itemSpecifics.put(key.substring(0, key.lastIndexOf(":")), value);
                        }
                    }
                } else {
                    if (tdTags.size() % 2 != 0) {
                        tdTags.remove(tdTags.size() - 1);
                    }
                    for (int k = 0; k < tdTags.size(); k += 2) {
                        TagNode keyTag = (TagNode) tdTags.get(k);
                        String key = removeSpaces(keyTag.getText().toString());
                        TagNode valueTag = (TagNode) tdTags.get(k + 1);
                        String value = removeSpaces(valueTag.getText().toString());

                        itemSpecifics.put(key.substring(0, key.lastIndexOf(":")), value);

                    }
                }
            }
        }
        return itemSpecifics;
    }

    private String getImageUrl(TagNode rootHtml) throws XPatherException {
        String imageUrl = "";
        Object[] images = rootHtml.evaluateXPath(DataExtractorConstants.XPATH_ITEM_IMAGE);
        for (Object image : images) {
            imageUrl = (String) image;
        }
        return imageUrl;
    }

    private String removeSpaces(String input) {
        if (input != null) {
            input = input.trim();
            input = input.replaceAll("\\s+", " ");
            input = input.replaceAll("&nbsp;", "");
            input = input.replaceAll("|", "");
            input = input.replaceAll("&amp;", "&");
            input = input.replaceAll("&#034;", "''");
            input = input.replaceAll("&#039;", "'");
            input = input.replaceAll("&ldquo;", "''");
        }
        return input;
    }

    private synchronized void writeListingToFile(Listing listing) {
        try {
            this.fileOutputStream = new FileOutputStream(new File(processingDir + File.separator + "ebay" + File.separator + "data.txt"), true);

            StringBuilder headerBuilder = new StringBuilder("");
            headerBuilder.append(listing.getItemId());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getTitle());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getCategoryTree());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getItemCondition());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getListingUrl());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getImageUrl());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getCompatibility());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getTimeLeft());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getBidPrice());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getBidCount());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getShipping());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getDelivery());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getPaymentMethod());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getReturns());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getGuarantee());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getItemLocation());
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getShipsTo());
            headerBuilder.append(" | ");
            if (listing.getSeller() != null) {
                headerBuilder.append(listing.getSeller().getSellerName());
                headerBuilder.append(" | ");
                headerBuilder.append(listing.getSeller().getRating());
                headerBuilder.append(" | ");
                headerBuilder.append(listing.getSeller().getFeedback());
            } else {
                headerBuilder.append("");
                headerBuilder.append(" | ");
                headerBuilder.append("");
                headerBuilder.append(" | ");
                headerBuilder.append("");
            }
            headerBuilder.append(" | ");
            headerBuilder.append(listing.getItemSpecifications());
            headerBuilder.append("\n");
            fileOutputStream.write(headerBuilder.toString().getBytes());
            fileOutputStream.close();
        } catch (IOException ex) {
            LOGGER.error("Error in writing data to data file");
        }
    }

    private void createDataFile() {
        try {
            //TODO - read output file name from config
            this.fileOutputStream = new FileOutputStream(new File(processingDir + File.separator + "ebay" + File.separator + "data.txt"), true);
            this.writeHeadersToDataFile();
            fileOutputStream.close();
        } catch (IOException ex) {
            LOGGER.error("Error in creating output data file");
        }
    }

    private void writeHeadersToDataFile() throws IOException {
        StringBuilder headerBuilder = new StringBuilder("");
        headerBuilder.append("Item Id");
        headerBuilder.append(" | ");
        headerBuilder.append("Title");
        headerBuilder.append(" | ");
        headerBuilder.append("Category Tree");
        headerBuilder.append(" | ");
        headerBuilder.append("Item Condition");
        headerBuilder.append(" | ");
        headerBuilder.append("Listing Url");
        headerBuilder.append(" | ");
        headerBuilder.append("Image Url");
        headerBuilder.append(" | ");
        headerBuilder.append("Compatibility");
        headerBuilder.append(" | ");
        headerBuilder.append("Time Left");
        headerBuilder.append(" | ");
        headerBuilder.append("Bid Price");
        headerBuilder.append(" | ");
        headerBuilder.append("Bid Count");
        headerBuilder.append(" | ");
        headerBuilder.append("Shipping");
        headerBuilder.append(" | ");
        headerBuilder.append("Delivery");
        headerBuilder.append(" | ");
        headerBuilder.append("Payment Method");
        headerBuilder.append(" | ");
        headerBuilder.append("Returns");
        headerBuilder.append(" | ");
        headerBuilder.append("Guarantee");
        headerBuilder.append(" | ");
        headerBuilder.append("Item Location");
        headerBuilder.append(" | ");
        headerBuilder.append("Ships To");
        headerBuilder.append(" | ");
        headerBuilder.append("Seller Name");
        headerBuilder.append(" | ");
        headerBuilder.append("Seller Rating");
        headerBuilder.append(" | ");
        headerBuilder.append("Seller Feedback");
        headerBuilder.append(" | ");
        headerBuilder.append("Item Specifications");
        headerBuilder.append("\n");
        fileOutputStream.write(headerBuilder.toString().getBytes());
    }
}
