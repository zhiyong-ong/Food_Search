package orbital.com.foodsearch.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import orbital.com.foodsearch.Activities.OcrActivity;
import orbital.com.foodsearch.Helpers.BingTranslate;
import orbital.com.foodsearch.Misc.ScrimTransformation;
import orbital.com.foodsearch.Models.ImageInsightsPOJO.BestRepresentativeQuery;
import orbital.com.foodsearch.Models.ImageInsightsPOJO.ImageCaption;
import orbital.com.foodsearch.Models.ImageSearchPOJO.ImageValue;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Utils.AnimUtils;
import orbital.com.foodsearch.Utils.NetworkUtils;

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
        return new ViewHolder(cardView);
    }

    // To populate data into item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageValue imageValue = mImageValues.get(position);
        String imageUrl = imageValue.getContentUrl();
        String thumbUrl = imageValue.getThumbnailUrl();
        String hostUrl = imageValue.getHostPageUrl();
        String displayUrl = "http://" + Html.fromHtml(imageValue.getHostPageDisplayUrl()).toString()
                .replace(".html", "/").replace("www.", "");
        // Set holder string values
        holder.clearState();
        holder.thumbUrl = thumbUrl;
        holder.imageUrl = imageUrl;

        // Use IMAGE_KEY captions for title and description if available
        BestRepresentativeQuery brq = imageValue.getRepresentativeQuery();
        ImageCaption imageCaption = imageValue.getImageCaption();
        String title = imageValue.getName();
        String desc = title;
        if (brq != null && imageCaption != null) {
            title = brq.getText();
            desc = imageCaption.getCaption();
            hostUrl = displayUrl = imageCaption.getDataSourceUrl().replace("www.", "");
        }

        // Set IMAGE_KEY using IMAGE_KEY url
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
        setTextViewUrl(holder.hostUrlView, hostUrl, displayUrl);
        final String finalHostUrl = hostUrl;
        holder.hostUrlView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(finalHostUrl));
                mContext.startActivity(browserIntent);
            }
        });

        // Set overlay and button colors
        int accentColor = Color.parseColor("#" + imageValue.getAccentColor());
        holder.translateBtn.setTextColor(accentColor);
        holder.undoBtn.setTextColor(accentColor);
        // Set overlay color with 0.9 opacity for better effect
        holder.overlay.setBackgroundColor(accentColor);
        holder.overlay.setAlpha(0.9f);
    }

    /**
     * This method formats the URL such that only the host domain is showed
     * but still links to the linkUrl
     *
     * @param textView Viewholder holding the textview
     * @param linkUrl  URL to be linked to
     * @param displayUrl URL to be displayed
     */
    private void setTextViewUrl(TextView textView, String linkUrl, String displayUrl) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        StringBuilder sb = new StringBuilder();
        sb.append("View page: ");
        sb.append("<a href=\"");
        sb.append(linkUrl);
        sb.append("\">");
        URL url = null;
        try {
            url = new URL(displayUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url != null) {
            sb.append(url.getHost());
        } else {
            sb.append(displayUrl);
        }
        sb.append("</a>");
        textView.setText(Html.fromHtml(sb.toString()), TextView.BufferType.EDITABLE);
    }

    @Override
    public int getItemCount() {
        return mImageValues.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View itemView;
        private ImageView imageView;
        private TextView titleTextView;
        private TextView hostUrlView;
        private TextView descView;
        private Button translateBtn;
        private Button undoBtn;
        private ImageButton closeBtn;
        private ImageButton fullscreenBtn;
        private ProgressBar progressBar;
        private FrameLayout overlay;
        private String imageUrl;
        private String thumbUrl;
        private String mOriginalTitle;
        private String mOriginalDesc;
        private String mTranslatedTitle;
        private String mTranslatedDesc;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            titleTextView = (TextView) itemView.findViewById(R.id.card_title);
            hostUrlView = (TextView) itemView.findViewById(R.id.card_hostpage);
            descView = (TextView) itemView.findViewById(R.id.card_description);
            translateBtn = (Button) itemView.findViewById(R.id.translate_button);
            undoBtn = (Button) itemView.findViewById(R.id.undo_button);
            closeBtn = (ImageButton) itemView.findViewById(R.id.close_button);
            fullscreenBtn = (ImageButton) itemView.findViewById(R.id.fullscrn_button);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar_card);
            overlay = (FrameLayout) itemView.findViewById(R.id.translate_overlay);
            translateBtn.setOnClickListener(this);
            closeBtn.setOnClickListener(this);
            fullscreenBtn.setOnClickListener(this);
            undoBtn.setOnClickListener(this);
        }

        private void clearState() {
            undoBtn.setVisibility(View.GONE);
            translateBtn.setVisibility(View.VISIBLE);
            mOriginalTitle = null;
            mOriginalDesc = null;
            mTranslatedTitle = null;
            mTranslatedDesc = null;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.translate_button:
                    translateResults();
                    break;
                case R.id.undo_button:
                    undoTranslate();
                    break;
                case R.id.close_button:
                    ((OcrActivity) mContext).onBackPressed();
                    break;
                case R.id.fullscrn_button:
                    ((OcrActivity) mContext).openPhotoView(itemView, imageUrl, thumbUrl, getAdapterPosition());
                    break;
            }
        }

        private void undoTranslate() {
            titleTextView.setText(mOriginalTitle);
            descView.setText(mOriginalDesc);
            undoBtn.setVisibility(View.GONE);
            translateBtn.setVisibility(View.VISIBLE);
        }

        private void translateResults() {
            // Check for network connection and throw snackbar error to prompt user if no internet connection.
            if (!NetworkUtils.isNetworkAvailable(mContext) || !NetworkUtils.isOnline()) {
                Snackbar snackbar = Snackbar.make(itemView.getRootView().findViewById(R.id.activity_ocr),
                        R.string.internet_error_text, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        translateResults();
                    }
                });
                snackbar.show();
                return;
            }
            // Save original titles first for undo translate later
            mOriginalTitle = titleTextView.getText().toString();
            mOriginalDesc = descView.getText().toString();
            // If never translated before or we have null values, perform translate bg task.
            // Otherwise, set the new translated texts on the corresponding views.
            if (mTranslatedDesc == null || mTranslatedTitle == null) {
                AnimUtils.circularReveal(translateBtn, overlay, itemView, null);
                AnimUtils.fadeIn(progressBar, AnimUtils.PROGRESS_BAR_DURATION);
                translateBtn.setEnabled(false);
                new translateBackground().execute();
            } else {
                titleTextView.setText(mTranslatedTitle);
                descView.setText(mTranslatedDesc);
                translateBtn.setVisibility(View.GONE);
                undoBtn.setVisibility(View.VISIBLE);
            }
        }

        // Asynctask class to translate in background and finish animations and layout
        // changes on post execute
        private class translateBackground extends AsyncTask<Void, Void, Void> {

            private String translatedTitle = mOriginalTitle;
            private String translatedDesc = mOriginalDesc;

            // Translate in the background both the title and description
            @Override
            protected Void doInBackground(Void... params) {
                translatedTitle = BingTranslate.getTranslatedText(mOriginalTitle);
                translatedDesc = BingTranslate.getTranslatedText(mOriginalDesc);
                return null;
            }

            // Fade out progress and overlay, set texts, change button visibilities
            // and save values on obtaining translated result
            @Override
            protected void onPostExecute(Void result) {
                translateBtn.setVisibility(View.GONE);
                translateBtn.setEnabled(true);
                undoBtn.setVisibility(View.VISIBLE);
                AnimUtils.fadeOut(overlay, AnimUtils.OVERLAY_DURATION);
                AnimUtils.fadeOut(progressBar, AnimUtils.PROGRESS_BAR_DURATION);
                titleTextView.setText(translatedTitle);
                descView.setText(translatedDesc);
                if (!translatedTitle.equals(mOriginalTitle) && !translatedDesc.equals(mOriginalDesc)) {
                    mTranslatedDesc = translatedDesc;
                    mTranslatedTitle = translatedTitle;
                }
                super.onPostExecute(result);
            }
        }

    }
}
