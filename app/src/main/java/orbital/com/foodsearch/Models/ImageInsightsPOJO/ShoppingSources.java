package orbital.com.foodsearch.Models.ImageInsightsPOJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class ShoppingSources {
    @SerializedName("offers")
    @Expose
    private List<Offer> offers = new ArrayList<Offer>();

    /**
     *
     * @return
     * The offers
     */
    public List<Offer> getOffers() {
        return offers;
    }

    /**
     *
     * @param offers
     * The offers
     */
    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }
}
