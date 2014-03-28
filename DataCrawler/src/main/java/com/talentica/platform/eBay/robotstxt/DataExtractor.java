package com.talentica.platform.eBay.robotstxt;

import com.talentica.platform.eBay.robotstxt.dao.listings.ListingDao;
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
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by AmitG on 21-03-2014.
 */

public class DataExtractor implements Runnable {
    private static Logger LOGGER = Logger.getLogger(DataExtractor.class);

    private ListingDao listingDao;
    private AtomicBoolean isUrlRetrieverFinished;
    private String processingDir;
    private FileOutputStream fileOutputStream;
    private final String DELIMITER = "\t";
    private String outputDataFilePath;

    public DataExtractor(ListingDao listingDao, AtomicBoolean isUrlRetrieverFinished, String processingDir) {

        this.listingDao = listingDao;
        this.isUrlRetrieverFinished = isUrlRetrieverFinished;
        this.processingDir = processingDir;
        outputDataFilePath = this.createDataFile();
    }

    @Override
    public void run() {
        while (!isUrlRetrieverFinished.get()) {
            try {
                List<com.talentica.platform.eBay.robotstxt.dao.listings.Listing> listings = listingDao.getListingsForProcessing(5);
                if (!listings.isEmpty()) {
                    for (com.talentica.platform.eBay.robotstxt.dao.listings.Listing listing : listings) {
                        this.doWork(listing);
                    }
                } else {
                    Thread.sleep((long) 10);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doWork(com.talentica.platform.eBay.robotstxt.dao.listings.Listing listing) {
        String url = listing.getUrl();
        LOGGER.debug("Started Data Extraction for Listing Url : " + url);
        try {
            extractData(url);
            listingDao.updateStatus(listing.getId(), 3);
        } catch (Exception ex) {
            ex.getMessage();
            ex.printStackTrace();
            LOGGER.error("Error in extracting data for url = " + url);
            listingDao.updateStatus(listing.getId(), 4);
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
            if (tagNode != null) {
                tagNode = tagNode.getParent();
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

    private synchronized void writeListingToFile(Listing listing) throws Exception {
        try {
            this.fileOutputStream = new FileOutputStream(new File(outputDataFilePath), true);

            StringBuilder headerBuilder = new StringBuilder("");
            headerBuilder.append(listing.getItemId());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getTitle());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getCategoryTree());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getItemCondition());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getListingUrl());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getImageUrl());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getTimeLeft());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getBidPrice());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getBidCount());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getShipping());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getDelivery());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getPaymentMethod());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getReturns());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getGuarantee());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getItemLocation());
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getShipsTo());
            headerBuilder.append(DELIMITER);
            if (listing.getSeller() != null) {
                headerBuilder.append(listing.getSeller().getSellerName());
                headerBuilder.append(DELIMITER);
                headerBuilder.append(listing.getSeller().getRating());
                headerBuilder.append(DELIMITER);
                headerBuilder.append(listing.getSeller().getFeedback());
            } else {
                headerBuilder.append("");
                headerBuilder.append(DELIMITER);
                headerBuilder.append("");
                headerBuilder.append(DELIMITER);
                headerBuilder.append("");
            }
            headerBuilder.append(DELIMITER);
            headerBuilder.append(listing.getItemSpecifications());
            headerBuilder.append("\n");
            fileOutputStream.write(headerBuilder.toString().getBytes());
            fileOutputStream.close();
        } catch (IOException ex) {
            LOGGER.error("Error in writing data to data file");
            throw ex;
        }
    }

    private String createDataFile() {
        File outputDataFile = null;
        try {
            File dataDirectory = new File(processingDir + File.separator + "ebay");
            if (!dataDirectory.exists()) {
                dataDirectory.mkdirs();
            }
            outputDataFile = new File(processingDir + File.separator + "ebay" + File.separator + "data.xls");
            this.fileOutputStream = new FileOutputStream(outputDataFile, true);
            this.writeHeadersToDataFile();
            fileOutputStream.close();
        } catch (IOException ex) {
            LOGGER.error("Error in creating output data file");
        }
        return outputDataFile.getPath();
    }

    private void writeHeadersToDataFile() throws IOException {
        StringBuilder headerBuilder = new StringBuilder("");
        headerBuilder.append("Item Id");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Title");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Category Tree");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Item Condition");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Listing Url");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Image Url");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Time Left");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Bid Price");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Bid Count");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Shipping");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Delivery");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Payment Method");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Returns");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Guarantee");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Item Location");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Ships To");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Seller Name");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Seller Rating");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Seller Feedback");
        headerBuilder.append(DELIMITER);
        headerBuilder.append("Item Specifications");
        headerBuilder.append("\n");
        fileOutputStream.write(headerBuilder.toString().getBytes());
    }
}
