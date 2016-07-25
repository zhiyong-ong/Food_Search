package orbital.com.foodsearch.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import orbital.com.foodsearch.Activities.MainActivity;
import orbital.com.foodsearch.DAO.PhotosContract.PhotosEntry;
import orbital.com.foodsearch.DAO.PhotosDBHelper;
import orbital.com.foodsearch.R;

/**
 * Created by zhiyong on 21/7/2016.
 */

public class RecentImageAdapter extends RecyclerView.Adapter<RecentImageAdapter.ViewHolder> {
    private static String LOG_TAG = "FOODIES";
    PhotosDBHelper mDBHelper;
    private Context mContext;
    private List<String> filePaths;
    private List<String> fileTitles;
    public RecentImageAdapter(Context context, List<String> FilePaths, List<String> TimeStamp) {
        this.filePaths = FilePaths;
        this.fileTitles = TimeStamp;
        mContext = context;
        mDBHelper = new PhotosDBHelper(context);
    }

    @Override
    public RecentImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate the custom layout
        View recentImagesView = inflater.inflate(R.layout.recent_image_item,
                parent, false);
        // Return a new holder instance
        RecentImageAdapter.ViewHolder viewHolder = new RecentImageAdapter.ViewHolder(recentImagesView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ImageView recentImageView = holder.recentImage;
        TextView timestamp = holder.timeStamp;
        Log.e(LOG_TAG, "position is: " + position);
        timestamp.setText(fileTitles.get(position));
        String path = filePaths.get(position);
        Log.e(LOG_TAG, "path is: " + path);
        Picasso.with(mContext).load("file://" + path)
                .fit()
                .into(recentImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        BitmapDrawable bmpDrawable = (BitmapDrawable) recentImageView.getDrawable();
                        Palette.from(bmpDrawable.getBitmap()).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                holder.timeStamp.setTextColor(palette.getVibrantColor(Color.BLACK));
                            }
                        });
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    private Cursor readDatabase(String fileTitle) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] results = {
                PhotosEntry._ID,
                PhotosEntry.COLUMN_NAME_DATA,
                PhotosEntry.COLUMN_NAME_ENTRY_TIME};
        String sortOrder = PhotosEntry.COLUMN_NAME_ENTRY_TIME;
        Cursor c = db.query(
                PhotosEntry.TABLE_NAME,
                results,
                PhotosEntry.COLUMN_NAME_ENTRY_TIME + " = '" + fileTitle + "'",
                null,
                null,
                null,
                sortOrder);
        return c;
    }

    @Override
    public int getItemCount() {
        return filePaths.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        private View itemView;
        private ImageView recentImage;
        private TextView timeStamp;
        private PercentRelativeLayout layoutView;
        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            recentImage = (ImageView) itemView.findViewById(R.id.recent_image_view);
            timeStamp = (TextView) itemView.findViewById(R.id.recent_image_timestamp);
            layoutView = (PercentRelativeLayout) itemView.findViewById(R.id.recent_image_layout);
            layoutView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Cursor cursor = readDatabase(fileTitles.get(getAdapterPosition()));
            cursor.moveToFirst();
            String data = cursor.getString(cursor.getColumnIndexOrThrow(PhotosEntry.COLUMN_NAME_DATA));
            Log.e(LOG_TAG, "ENTRY TIME: " + cursor.getString(cursor.getColumnIndexOrThrow(PhotosEntry.COLUMN_NAME_ENTRY_TIME)));
            Log.e(LOG_TAG, "adapter pos: " + getAdapterPosition());
            ((MainActivity) mContext).openRecentPhoto(itemView, filePaths.get(getAdapterPosition()), data);

        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }
}
