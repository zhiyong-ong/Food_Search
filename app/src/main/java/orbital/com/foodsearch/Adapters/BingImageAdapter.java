package orbital.com.foodsearch.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import orbital.com.foodsearch.Models.ImageValue;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.ScrimTransformation;

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
        String title = imageValue.getName();
        String imageUrl = imageValue.getContentUrl();
        String thumbUrl = imageValue.getThumbnailUrl();
        String hostUrl = imageValue.getHostPageUrl();

        // Set image using image url
        ImageView cardImageView = holder.imageView;
        Picasso.with(mContext).load(imageValue.getThumbnailUrl())
                .fit()
                .centerCrop()
                .transform(new ScrimTransformation(mContext, cardImageView))
                .into(cardImageView);

        // Set title using the name
        holder.titleTextView.setText(title);
        holder.contentURLView.setText(imageUrl);
        holder.hostPageView.setText(hostUrl);
    }

    @Override
    public int getItemCount() {
        return mImageValues.size();
    }

//    private class PreDrawListener implements ViewTreeObserver.OnPreDrawListener{
//        private Context mContext = null;
//        private ImageView cardImageView = null;
//        private ImageValue imageValue = null;
//        private ShapeDrawable shapeDrawable = null;
//
//        PreDrawListener(Context context, ImageView cardImageView, ImageValue imageValue) {
//            mContext = context;
//            this.cardImageView = cardImageView;
//            this.imageValue = imageValue;
//        }
//
//        @Override
//        public boolean onPreDraw() {
//            cardImageView.getViewTreeObserver().removeOnPreDrawListener(this);
//            Picasso.with(mContext).load(imageValue.getThumbnailUrl())
//                    .centerCrop()
//                    .resize(cardImageView.getWidth(),
//                            cardImageView.getHeight())
//                    .transform(new ScrimTransformation(mContext, cardImageView))
//                    .into(cardImageView);
//            return true;
//        }
//    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView titleTextView;
        private TextView hostPageView;
        private TextView contentURLView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            titleTextView = (TextView) itemView.findViewById(R.id.card_title);
            hostPageView = (TextView) itemView.findViewById(R.id.card_hostpage);
            contentURLView = (TextView)itemView.findViewById(R.id.card_contentURL);
        }
    }
}
