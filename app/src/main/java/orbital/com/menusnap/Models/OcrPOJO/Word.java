package orbital.com.menusnap.Models.OcrPOJO;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Abel on 6/6/2016.
 */
public class Word {

    private String boundingBox;
    private String text;
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
     * @return the bounds in array form: x, y, width, height
     */
    public String[] getBoundsArray() {
        String original = getBoundingBox();
        return original.split(",");
    }

    /**
     *
     * @return
     * The text
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @param text
     * The text
     */
    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
