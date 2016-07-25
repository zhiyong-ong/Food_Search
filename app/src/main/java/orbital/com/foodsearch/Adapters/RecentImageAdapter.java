package orbital.com.foodsearch.Adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.DAO.PhotosContract.PhotosEntry;
import orbital.com.foodsearch.DAO.PhotosDBHelper;
import orbital.com.foodsearch.Fragments.RecentsFragment;
import orbital.com.foodsearch.R;

/**
 * Created by zhiyong on 21/7/2016.
 */

public class RecentImageAdapter extends RecyclerView.Adapter<RecentImageAdapter.ViewHolder> {
    private static String LOG_TAG = "FOODIES";
    private Context mContext;
    private RecentsFragment mRecentsFragment;
    private List<String> filePaths;
    private List<String> fileTitles;
    private SparseBooleanArray selectedItems;
    private PhotosDBHelper mDBHelper;

    public RecentImageAdapter(Context context, RecentsFragment recentsFragment, List<String> FilePaths, List<String> TimeStamps) {
        this.filePaths = FilePaths;
        this.fileTitles = TimeStamps;
        mContext = context;
        mRecentsFragment = recentsFragment;
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ImageView recentImageView = holder.recentImage;
        TextView timestamp = holder.timeStamp;
        Log.e(LOG_TAG, "position is: " + position);
        String title = fileTitles.get(position);
        timestamp.setText(title);

        Cursor cursor = readDatabase(title);
        String formattedDate = cursor.getString(cursor.getColumnIndex(PhotosEntry.COLUMN_NAME_FORMATTED_DATE));
        String formattedString = cursor.getString(cursor.getColumnIndex(PhotosEntry.COLUMN_NAME_FORMATTED_STRING));

        String path = filePaths.get(position);
        Log.e(LOG_TAG, "path is: " + path);
        if (selectedItems.get(position, false)) {
            holder.layoutView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorSelected));
        } else {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = mContext.obtainStyledAttributes(attrs);
            int backgroundResource = typedArray.getResourceId(0, 0);
            holder.layoutView.setBackgroundResource(backgroundResource);
            typedArray.recycle();
        }

        Picasso.with(mContext).load("file://" + path)
                .fit()
                .into(recentImageView);
    }

    public Cursor readDatabase(String fileTitle) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String[] results = {
                PhotosEntry._ID,
                PhotosEntry.COLUMN_NAME_DATA,
                PhotosEntry.COLUMN_NAME_ENTRY_TIME,
                PhotosEntry.COLUMN_NAME_FORMATTED_DATE,
                PhotosEntry.COLUMN_NAME_FORMATTED_STRING};
        Cursor c = db.query(
                PhotosEntry.TABLE_NAME,
                results,
                PhotosEntry.COLUMN_NAME_ENTRY_TIME + " = '" + fileTitle + "'",
                null,
                null,
                null,
                null);
        return c;
    }

    @Override
    public int getItemCount() {
        return filePaths.size();
    }

    public void deleteData(int pos) {
        File file = new File(filePaths.get(pos));
        file.delete();
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        db.delete(PhotosEntry.TABLE_NAME, PhotosEntry.COLUMN_NAME_TITLE + " = '" + fileTitles.get(pos) + "'", null);
        filePaths.remove(pos);
        fileTitles.remove(pos);
        notifyItemChanged(pos);
    }

    public void selectAll() {
        int total = filePaths.size();
        for (int pos = 0; pos < total; pos++) {
            selectedItems.put(pos, true);
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            notifyDataSetChanged();
            if (selectedItems.size() == 0) {
                mRecentsFragment.finishActionMode();
            }
            //selectedView.findViewById(R.id.remove_item_checkbox).setVisibility(View.INVISIBLE);
        } else {
            selectedItems.put(pos, true);
            notifyDataSetChanged();
            //selectedView.findViewById(R.id.remove_item_checkbox).setVisibility(View.VISIBLE);
        }
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public void deleteSelections() {
        List<Integer> selectedItemKeys = getSelectedItemsKeys();
        for (int i = selectedItemKeys.size() - 1; i >= 0; i--) {
            int currPos = selectedItemKeys.get(i);
            deleteData(currPos);
        }
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItemsKeys() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private PercentRelativeLayout layoutView;
        private ImageView recentImage;
        private TextView timeStamp;
        private ImageView checkCircle;
        public ViewHolder(View itemView) {
            super(itemView);
            layoutView = (PercentRelativeLayout) itemView.findViewById(R.id.recent_image_layout);
            layoutView.setOnClickListener(this);
            layoutView.setOnLongClickListener(this);
            recentImage = (ImageView) itemView.findViewById(R.id.recent_image_view);
            timeStamp = (TextView) itemView.findViewById(R.id.recent_image_timestamp);
            checkCircle = (ImageView) itemView.findViewById(R.id.remove_item_checkbox);
            checkCircle.setImageResource(R.drawable.ic_check_circle);
        }

        @Override
        public void onClick(View view) {
            mRecentsFragment.itemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            mRecentsFragment.startActionMode(view, getAdapterPosition());
            return true;
        }
    }
}
