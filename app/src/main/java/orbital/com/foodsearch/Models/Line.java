package orbital.com.foodsearch.Models;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Line {
    private final static String LANGUAGE_EN = "en";
    private final static String LANGUAGE_CNS = "zh-Hans";
    private final static String LANGUAGE_CNT = "zh-Hant";
    private final static String LANGUAGE_JA = "ja";
    private final static String LANGUAGE_KO = "ko";

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

    public String getText(String language) {
        StringBuilder builder = new StringBuilder();
        List<Word> words = getWords();
        switch (language) {
            case LANGUAGE_CNS:case LANGUAGE_CNT:case LANGUAGE_KO:
            case LANGUAGE_JA:
                for (Word word: words) {
                    builder.append(word.getText());
                }
                break;
            default:
                Log.e("FOODIES", language);
                for (Word word: words) {
                    builder.append(word.getText());
                    builder.append(" ");
                }
        }

        return builder.toString();
    }
}