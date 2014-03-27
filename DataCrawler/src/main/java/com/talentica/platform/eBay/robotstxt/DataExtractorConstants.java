package com.talentica.platform.eBay.robotstxt;

/**
 * Created by AmitG on 26-03-2014.
 */
public class DataExtractorConstants {
    public static final String XPATH_ITEM_CONDITION = "//div[@id='vi-itm-cond']/text()|//div[@id='vi-itm-cond']";
    public static final String XPATH_ITEM_LEFT = "//span[@id='bb_tlft']";
    public static final String XPATH_ITEM_PRICE = "//span[@id='prcIsum_bidPrice']|//span[@id='prcIsum']|//span[@id='mm-saleDscPrc']|//span[@class='notranslate vi-VR-cvipPrice']";
    public static final String XPATH_BID_COUNT = "//span[@id='qty-test']|//a[@id='vi-VR-bid-lnk']/span[1]";
    public static final String XPATH_ITEM_LOCATION = "//div[@id='itemLocation']/div[@class='u-flL iti-w75']/div[@class='iti-eu-txt iti-spacing']/div[@class='iti-eu-bld-gry']|//div[@class='vi-cviprow']/div[2]/div[@class='u-flL']";
    public static final String XPATH_SHIPS_TO = "//div[@id='vi-acc-shpsToLbl-cnt']";
    public static final String XPATH_SHIPPING = "//span[@id='shSummary']";
    public static final String XPATH_DELIVERY = "//span[@id='delSummary']";
    public static final String XPATH_RETURNS = "//span/span[@id='vi-ret-accrd-txt']";
    public static final String XPATH_PAYMENT_METHOD = "//div[@id='payDet1']/img[@class='pd-img']/@alt";
    public static final String XPATH_GUARANTEE = "//div[@class='u-flL  rpColWid ']/table[@class='vi-ebp2-logo-tbl']/tbody/tr[2]/td|//div[@class='u-flL  rpColWid ']/table[@class='vi-ebp2-logo-tbl']/tbody/tr[3]/td";
    public static final String XPATH_CATEGORY_TREE = "//td[@id='vi-VR-brumb-lnkLst']";
    public static final String XPATH_TITLE = "//h1[@id='itemTitle']";
    public static final String XPATH_SELLER_NAME = "//div[@class='si-cnt si-cnt-eu vi-grBr vi-padn0 c-std']/div/div[@class='si-inner']/div[@class='si-content']/div[contains(@class,'bdg-')]/div[@class='mbg vi-VR-margBtm3']/a/span[@class='mbg-nw']|//div[@class='mbg vi-VR-margBtm3']/a/span[@class='mbg-nw']";
    public static final String XPATH_SELLER_RATING = "//div[@class='si-cnt si-cnt-eu vi-grBr vi-padn0 c-std']/div/div[@class='si-inner']/div[@class='si-content']/div[contains(@class,'bdg-')]/div[@class='mbg vi-VR-margBtm3']/span[@class='mbg-l']/a|//div[@class='mbg vi-VR-margBtm3']/span[@class='mbg-l']/a";
    public static final String XPATH_SELLER_FEEDBACK = "//div[@class='si-cnt si-cnt-eu vi-grBr vi-padn0 c-std']/div/div[@class='si-inner']/div[@class='si-content']/div[contains(@class,'bdg-')]/div[@id='si-fb']";
    public static final String XPATH_ITEM_SPECIFICS = "//div[@class='section']/h2[@class='secHd']";
    public static final String XPATH_ITEM_IMAGE = "//img[@id='icImg']/@src";

}
