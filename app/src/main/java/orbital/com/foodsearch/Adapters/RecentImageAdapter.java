package orbital.com.foodsearch.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.DAO.PhotosContract.PhotosEntry;
import orbital.com.foodsearch.DAO.PhotosDBHelper;
import orbital.com.foodsearch.R;

/**
 * Created by zhiyong on 21/7/2016.
 */

public class RecentImageAdapter extends RecyclerView.Adapter<RecentImageAdapter.ViewHolder> {
    private static String LOG_TAG = "FOODIES";
    private Context mContext;
    private List<String> filePaths;
    private List<String> fileTitles;
    private SparseBooleanArray selectedItems;
    private PhotosDBHelper mDBHelper;

    public RecentImageAdapter(Context context, List<String> FilePaths, List<String> TimeStamp) {
        this.filePaths = FilePaths;
        this.fileTitles = TimeStamp;
        mContext = context;
        mDBHelper = new PhotosDBHelper(context);
        selectedItems = new SparseBooleanArray();

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
    public void onBindViewHolder(ViewHolder holder, int position) {

        ImageView recentImageView = holder.recentImage;
        TextView timestamp = holder.timeStamp;
        Log.e(LOG_TAG, "position is: " + position);
        timestamp.setText(fileTitles.get(position));
        String path = filePaths.get(position);
        Log.e(LOG_TAG, "path is: " + path);
        Picasso.with(mContext).load("file://" + path)
                .fit()
                //.centerCrop()
                .into(recentImageView);
    }

    public Cursor readDatabase(String fileTitle) {
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

    public void removeData(int pos) {
        File file = new File(filePaths.get(pos));
        file.delete();
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        db.delete(PhotosEntry.TABLE_NAME, PhotosEntry.COLUMN_NAME_TITLE + " = '" + fileTitles.get(pos) + "'", null);
        filePaths.remove(pos);
        fileTitles.remove(pos);
        notifyItemChanged(pos);
    }
    public void toggleSelection(int pos, View view) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            view.findViewById(R.id.remove_item_checkbox).setVisibility(View.INVISIBLE);
        }
        else {
            selectedItems.put(pos, true);
            view.findViewById(R.id.remove_item_checkbox).setVisibility(View.VISIBLE);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView recentImage;
        private TextView timeStamp;
        private ImageView checkCircle;
        public ViewHolder(View itemView) {
            super(itemView);
            recentImage = (ImageView) itemView.findViewById(R.id.recent_image_view);
            timeStamp = (TextView) itemView.findViewById(R.id.recent_image_timestamp);
            checkCircle = (ImageView) itemView.findViewById(R.id.remove_item_checkbox);
            checkCircle.setImageResource(R.drawable.ic_check_circle);
        }

    }
}
