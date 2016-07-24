package orbital.com.foodsearch.Fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Activities.MainActivity;
import orbital.com.foodsearch.Adapters.RecentImageAdapter;
import orbital.com.foodsearch.DAO.PhotosContract;
import orbital.com.foodsearch.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentsFragment extends android.app.Fragment implements RecyclerView.OnItemTouchListener, View.OnClickListener {

    private static final String LOG_TAG = "FOODIES";
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    private RecentImageAdapter mAdapter;
    private List<String> filePaths;
    private List<String> fileNames;
    private ActionMode actionMode;
    private GestureDetectorCompat gestureDetector;

    public RecentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_recents, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recent_images_recycler);
        filePaths = new ArrayList<>();
        fileNames = new ArrayList<>();
        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        getFiles();
        mAdapter = new RecentImageAdapter(getActivity(), filePaths, fileNames);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(0);

        mRecyclerView.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(getActivity(), new RecyclerViewOnGestureListener());
        return rootView;
    }

    private void getFiles() {
        File root = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                , "Recent_Images");
        if (!root.exists()) {
            if (!root.mkdirs()) {
                Log.e(LOG_TAG, getString(R.string.mkdir_fail_text));
            }
        }
        File[] files = root.listFiles();
        for (File file : files) {
            filePaths.add(file.getAbsolutePath());
            fileNames.add(file.getName());
        }
    }

    private void clearFiles() {
        filePaths.clear();
        fileNames.clear();
    }

    @Override
    public void onResume() {
        File root = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                , "Recent_Images");
        if (!root.exists()) {
            if (!root.mkdirs()) {
                Log.e(LOG_TAG, getString(R.string.mkdir_fail_text));
            }
        }
        File[] files = root.listFiles();
        if (files.length != 0) {
            clearFiles();
            getFiles();
            mAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        int idx = mRecyclerView.getChildAdapterPosition(view);
        if(actionMode != null) {
            myToggleSelection(idx, view);
            return;
        }
        Cursor cursor = mAdapter.readDatabase(fileNames.get(idx));
        cursor.moveToFirst();
        String data = cursor.getString(cursor.getColumnIndexOrThrow(PhotosContract.PhotosEntry.COLUMN_NAME_DATA));
        Log.e(LOG_TAG, "ENTRY TIME: " + cursor.getString(cursor.getColumnIndexOrThrow(PhotosContract.PhotosEntry.COLUMN_NAME_ENTRY_TIME)));
        ((MainActivity) getActivity()).openRecentPhoto(filePaths.get(idx), data);
    }

    private void myToggleSelection(int idx, View view) {
        mAdapter.toggleSelection(idx, view);
        String title = getString(R.string.selected_count, String.valueOf(mAdapter.getSelectedItemCount()));
        actionMode.setTitle(title);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {


        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_cab_delete_recent, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_delete:
                    List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
                    int currPos;
                    for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                        currPos = selectedItemPositions.get(i);
                        mAdapter.removeData(currPos);
                    }
                    actionMode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            mAdapter.clearSelections();
        }

    };
    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            Log.e(LOG_TAG, view.toString());
            if (actionMode != null) {
                return;
            }
            // Start the CAB using the ActionMode.Callback defined above
            AppCompatActivity activity=(AppCompatActivity)getActivity();
            actionMode = activity.startSupportActionMode(mActionModeCallback);
            int idx = mRecyclerView.getChildAdapterPosition(view);
            myToggleSelection(idx, view);

            view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorLightGrey));
            super.onLongPress(e);
        }
    }
}
