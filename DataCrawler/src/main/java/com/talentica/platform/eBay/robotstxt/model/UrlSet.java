package com.talentica.platform.eBay.robotstxt.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Created by AmitG on 21-03-2014.
 */

@XmlRootElement(name = "urlset")
public class UrlSet {

    private ArrayList<Url> urls;

    @XmlElement(name = "url")
    public ArrayList<Url> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<Url> urls) {
        this.urls = urls;
    }
}
