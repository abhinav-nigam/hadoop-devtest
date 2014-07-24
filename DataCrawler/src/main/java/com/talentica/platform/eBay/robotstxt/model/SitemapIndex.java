package com.talentica.platform.eBay.robotstxt.model;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * Created by AmitG on 20-03-2014.
 */

@XmlRootElement(name = "sitemapindex")
public class SitemapIndex {

    private String xmlns;
    private ArrayList<Sitemap> sitemaps;

    @XmlAttribute()
    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    @XmlElement(name = "sitemap")
    public ArrayList<Sitemap> getSitemaps() {
        return sitemaps;
    }

    public void setSitemaps(ArrayList<Sitemap> sitemaps) {
        this.sitemaps = sitemaps;
    }
}
