package CatFinder.XmlEntities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Roman 01.04.2017.
 */
@XmlRootElement(name = "image")
public class Image {
    /*
    http://24.media.tumblr.com/tumblr_lsyr3iw20P1r4xjo2o1_500.jpg</url><id>5gq</id>
    <source_url>http://thecatapi.com/?id=5gq</source_url>
     */

    private String id;

    private String url;

    private String source_url;


    public String getId() {
        return id;
    }
    @XmlElement
    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }
    @XmlElement
    public void setUrl(String url) {
        this.url = url;
    }
    @XmlElement(type = String.class)
    public String getSource_url() {
        return source_url;
    }

    public void setSource_url(String source_url) {
        this.source_url = source_url;
    }

    @Override
    public String toString() {
        return "Image{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", source_url='" + source_url + '\'' +
                '}';
    }
}
