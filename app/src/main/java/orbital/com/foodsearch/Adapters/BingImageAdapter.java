package orbital.com.foodsearch.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import orbital.com.foodsearch.Helpers.BingTranslate;
import orbital.com.foodsearch.Models.ImageInsightsPOJO.BestRepresentativeQuery;
import orbital.com.foodsearch.Models.ImageInsightsPOJO.ImageCaption;
import orbital.com.foodsearch.Models.ImageSearchPOJO.ImageValue;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.ScrimTransformation;
import orbital.com.foodsearch.Utils.AnimUtils;

/**
 * Created by Abel on 6/14/2016.
 */

public class BingImageAdapter
        extends RecyclerView.Adapter<BingImageAdapter.ViewHolder> {

    private Context mContext;
    private List<ImageValue> mImageValues;

    public BingImageAdapter(Context context, List<ImageValue> imageValues) {
        mContext = context;
        mImageValues = imageValues;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        // Inflate the custom layout
        View cardView = inflater.inflate(R.layout.card_item,
                parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(cardView);
        return viewHolder;
    }

    // To populate data into item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageValue imageValue = mImageValues.get(position);
        String contentUrl = imageValue.getContentUrl();
        String thumbUrl = imageValue.getThumbnailUrl();
        String hostUrl = imageValue.getHostPageUrl();

        BestRepresentativeQuery brq = imageValue.getRepresentativeQuery();
        ImageCaption imageCaption = imageValue.getImageCaption();
        String title = imageValue.getName();
        String desc = title;
        if (brq != null && imageCaption != null) {
            title = brq.getText();
            desc = imageCaption.getCaption();
            hostUrl = imageCaption.getDataSourceUrl();
        }
        // Set image using image url
        ImageView cardImageView = holder.imageView;
        Picasso.with(mContext).load(thumbUrl)
                .fit()
                .centerCrop()
                .transform(new ScrimTransformation(mContext, cardImageView))
                .into(cardImageView);

        // Set title using the name
        holder.titleTextView.setText(title);
        holder.descView.setText(desc);
        // Set formatted URL on UrlView
        setTextViewUrl(holder.hostUrlView, hostUrl);
        holder.translateBtn.setTextColor(
                Color.parseColor("#" + imageValue.getAccentColor()));
    }

    /**
     * This method formats the URL such that only the host domain is showed
     * but still links to the hostUrl
     * @param textView Viewholder holding the textview
     * @param hostUrl URL to be linked to and formatted
     */
    private void setTextViewUrl(TextView textView, String hostUrl) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"");
        sb.append(hostUrl);
        sb.append("\">");
        URL url = null;
        try {
            url = new URL(hostUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        sb.append(url.getHost());
        sb.append("</a>");
        textView.setText(Html.fromHtml(sb.toString()));
    }

    @Override
    public int getItemCount() {
        return mImageValues.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private TextView titleTextView;
        private TextView hostUrlView;
        private TextView descView;
        private Button translateBtn;
        private ProgressBar progressBar;
        private FrameLayout overlay;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            titleTextView = (TextView) itemView.findViewById(R.id.card_title);
            hostUrlView = (TextView) itemView.findViewById(R.id.card_hostpage);
            descView = (TextView) itemView.findViewById(R.id.card_description);
            translateBtn = (Button) itemView.findViewById(R.id.translate_button);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar_card);
            overlay = (FrameLayout) itemView.findViewById(R.id.card_overlay);
            translateBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            final String titleText = titleTextView.getText().toString();
            final String descText = descView.getText().toString();
            class background extends AsyncTask<Void, Void, Void> {

                private String translatedTitle = titleText;
                private String translatedDesc = descText;

                @Override
                protected Void doInBackground(Void... params) {
                    translatedTitle = BingTranslate.getTranslatedText(titleText);
                    translatedDesc = BingTranslate.getTranslatedText(descText);
                    return null;
                }
                @Override
                protected void onPostExecute(Void result) {
                    titleTextView.setText(translatedTitle);
                    descView.setText(translatedDesc);
                    progressBar.setVisibility(View.GONE);
                    AnimUtils.brightenOverlay(overlay);
                    super.onPostExecute(result);
                }
            }
            new background().execute();
            progressBar.setVisibility(View.VISIBLE);
            AnimUtils.darkenOverlay(overlay);
        }
    }
}
