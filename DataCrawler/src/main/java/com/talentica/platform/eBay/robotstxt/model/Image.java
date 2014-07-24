package com.talentica.platform.eBay.robotstxt.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by AmitG on 21-03-2014.
 */

@XmlRootElement(name = "image:image")
public class Image {

    private String imageLoc;

    @XmlElement(name = "image:loc")
    public String getImageLoc() {
        return imageLoc;
    }

    public void setImageLoc(String imageLoc) {
        this.imageLoc = imageLoc;
    }
}
