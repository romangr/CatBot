package CatFinder.XmlEntities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

/**
 * Roman 01.04.2017.
 */
@XmlRootElement(name = "data")
public class Data {
    private List<Image> images;

    public List<Image> getImages() {
        return images;
    }

    @XmlElement(name = "images")
    public void setImages(List<Image> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "Data{" +
                "images=" + images +
                '}';
    }
}
