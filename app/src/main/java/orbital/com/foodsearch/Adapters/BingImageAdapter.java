package orbital.com.foodsearch.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
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
        final String imageUrl = imageValue.getContentUrl();
        String thumbUrl = imageValue.getThumbnailUrl();
        String hostUrl = imageValue.getHostPageUrl();

        // Set image using image url
        final ImageView cardImageView = holder.imageView;
        ViewTreeObserver vto = cardImageView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
            @Override
            public boolean onPreDraw() {
                cardImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                Picasso.with(mContext).load(imageUrl)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .centerCrop()
                        .resize(cardImageView.getMeasuredWidth(),
                                cardImageView.getMeasuredHeight())
                        .transform(new ScrimTransformation(mContext, cardImageView))
                        .into(cardImageView);
                return true;
            }
        });

        // Set title using the name
        TextView cardTitle = holder.titleTextView;
        cardTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        return mImageValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView titleTextView;
        private View scrimView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.card_image);
            titleTextView = (TextView) itemView.findViewById(R.id.card_title);
            scrimView = itemView.findViewById(R.id.scrim_view);
        }
    }
}
