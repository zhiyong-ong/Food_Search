package orbital.com.foodsearch.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BingResponse {

    private String language;
    private Double textAngle;
    private String orientation;
    private List<Region> regions = new ArrayList<Region>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The language
     */
    public String getLanguage() {
        return language;
    }

    /**
     *
     * @param language
     * The language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     *
     * @return
     * The textAngle
     */
    public Double getTextAngle() {
        return textAngle;
    }

    /**
     *
     * @param textAngle
     * The textAngle
     */
    public void setTextAngle(Double textAngle) {
        this.textAngle = textAngle;
    }

    /**
     *
     * @return
     * The orientation
     */
    public String getOrientation() {
        return orientation;
    }

    /**
     *
     * @param orientation
     * The orientation
     */
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    /**
     *
     * @return
     * The regions
     */
    public List<Region> getRegions() {
        return regions;
    }

    /**
     *
     * @param regions
     * The regions
     */
    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

