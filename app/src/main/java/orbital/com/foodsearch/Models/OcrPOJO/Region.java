package orbital.com.foodsearch.models.OcrPOJO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abel on 6/6/2016.
 */
public class Region {

    private String boundingBox;
    private List<Line> lines = new ArrayList<Line>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The boundingBox
     */
    public String getBoundingBox() {
        return boundingBox;
    }

    /**
     *
     * @param boundingBox
     * The boundingBox
     */
    public void setBoundingBox(String boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     *
     * @return
     * The lines
     */
    public List<Line> getLines() {
        return lines;
    }

    /**
     *
     * @param lines
     * The lines
     */
    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


}
