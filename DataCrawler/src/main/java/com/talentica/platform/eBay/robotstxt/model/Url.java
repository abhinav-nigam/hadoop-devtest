package com.talentica.platform.eBay.robotstxt.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by AmitG on 21-03-2014.
 */

@XmlRootElement(name = "url")
public class Url {
    private String loc;
    private String expires;
    private String changefreq;
    private String priority;
    private String image;

    @XmlElement(name = "loc")
    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    @XmlElement(name = "expires")
    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    @XmlElement(name = "changefreq")
    public String getChangefreq() {
        return changefreq;
    }

    public void setChangefreq(String changefreq) {
        this.changefreq = changefreq;
    }

    @XmlElement(name = "priority")
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @XmlElement(name = "image")
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
