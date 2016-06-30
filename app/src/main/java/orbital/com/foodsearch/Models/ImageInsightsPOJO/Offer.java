package orbital.com.foodsearch.Models.ImageInsightsPOJO;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class Offer {

    private String url;
    private Seller seller;
    private String availability;

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The seller
     */
    public Seller getSeller() {
        return seller;
    }

    /**
     *
     * @param seller
     * The seller
     */
    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    /**
     *
     * @return
     * The availability
     */
    public String getAvailability() {
        return availability;
    }

    /**
     *
     * @param availability
     * The availability
     */
    public void setAvailability(String availability) {
        this.availability = availability;
    }
}
