package ru.romangr.lolbot.catfinder.XmlEntities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Roman 01.04.2017.
 */
@Getter
@Setter
@ToString
@XmlRootElement(name = "data")
public class Data {

    @XmlElement(name = "images")
    private List<Image> images;

}
