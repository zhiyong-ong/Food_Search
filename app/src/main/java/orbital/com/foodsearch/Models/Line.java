package orbital.com.foodsearch.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Line {

    private String boundingBox;
    private List<Word> words = new ArrayList<Word>();
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
     * The words
     */
    public List<Word> getWords() {
        return words;
    }

    /**
     *
     * @param words
     * The words
     */
    public void setWords(List<Word> words) {
        this.words = words;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getText() {
        StringBuilder builder = new StringBuilder();
        List<Word> words = getWords();
        for (Word word: words) {
            builder.append(word.getText());
            builder.append(" ");
        }
        return builder.toString();
    }
}