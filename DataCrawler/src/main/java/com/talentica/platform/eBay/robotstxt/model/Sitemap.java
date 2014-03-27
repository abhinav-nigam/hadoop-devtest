package com.talentica.platform.eBay.robotstxt.model;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by AmitG on 20-03-2014.
 */

public class Sitemap {
    private String loc;

    @XmlElement(name = "loc")
    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }
}
