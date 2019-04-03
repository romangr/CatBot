package ru.romangr.lolbot.catfinder.XmlEntities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Roman 01.04.2017.
 */
@Getter
@Setter
@ToString
@XmlRootElement(name = "image")
public class Image {
    /*
    http://24.media.tumblr.com/tumblr_lsyr3iw20P1r4xjo2o1_500.jpg</url><id>5gq</id>
    <sourceUrl>http://thecatapi.com/?id=5gq</sourceUrl>
     */

    @XmlElement
    private String id;

    @XmlElement
    private String url;

    @XmlElement(type = String.class, name = "sourceUrl")
    private String sourceUrl;

}
