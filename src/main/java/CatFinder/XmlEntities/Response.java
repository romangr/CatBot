package CatFinder.XmlEntities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Roman 01.04.2017.
 */
@XmlRootElement(name = "response")
public class Response {

    private Data data;

    public Data getData() {
        return data;
    }

    @XmlElement
    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{" +
                "data=" + data +
                '}';
    }
}
