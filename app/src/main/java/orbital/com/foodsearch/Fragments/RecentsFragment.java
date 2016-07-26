package orbital.com.foodsearch.Fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import orbital.com.foodsearch.Activities.MainActivity;
import orbital.com.foodsearch.Adapters.RecentImageAdapter;
import orbital.com.foodsearch.DAO.PhotosContract;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Utils.AnimUtils;
import orbital.com.foodsearch.Utils.FileUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentsFragment extends android.app.Fragment {

    private static final String LOG_TAG = "FOODIES";
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    private RecentImageAdapter mAdapter;
    private List<String> filePaths;
    private List<String> fileNames;
    private ActionMode actionMode;
    private GestureDetectorCompat gestureDetector;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        boolean deleted = false;

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
            deleted = false;
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_check_all:
                    mAdapter.selectAll();
                    return true;
                case R.id.menu_delete:
                    deleted = true;
                    mAdapter.deleteSelected();
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
            mAdapter.clearAllSelection();
            if (!deleted) {
                mAdapter.notifyDataSetChanged();
            }
            if (filePaths.isEmpty()) {
                mAdapter.notifyDataSetChanged();
                AnimUtils.fadeIn(getView().findViewById(R.id.empty_recents_layout), AnimUtils.OVERLAY_DURATION);
            }
        }
    };
    private int bottomNavHeight = -1;

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
        getFiles();

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new RecentImageAdapter(getActivity(), this, filePaths, fileNames);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new SlideInRightAnimator() {
            @Override
            public void onAnimationFinished(RecyclerView.ViewHolder viewHolder) {
                updateRecyclerLayout();
                super.onAnimationFinished(viewHolder);
            }
        });
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

        Log.e(LOG_TAG, "file paths: " + filePaths.size());
        Log.e(LOG_TAG, "file names: " + fileNames.size());
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
        FileUtils.sortFileByTime(files);
        for (File file : files) {
            filePaths.add(file.getAbsolutePath());
            fileNames.add(file.getName());
        }

    }

    private void clearFiles() {
        filePaths.clear();
        fileNames.clear();
    }

    public void smoothScrollToTop() {
        if (filePaths.size() > 0) {
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    public void scrollToTop() {
        if (filePaths.size() > 0) {
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }
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
            refreshRecents();
            getView().findViewById(R.id.empty_recents_layout).setVisibility(View.INVISIBLE);
        } else {
            getView().findViewById(R.id.empty_recents_layout).setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    private void refreshRecents() {
        clearFiles();
        getFiles();
        mAdapter.notifyDataSetChanged();
        updateRecyclerLayout();
    }

    public void updateRecyclerLayout() {
        if (isRecyclerScrollable(mRecyclerView)) {
            ((MainActivity) getActivity()).enableScroll();
        } else {
            ((MainActivity) getActivity()).disableScroll();
        }
    }

    private boolean isRecyclerScrollable(RecyclerView recyclerView) {
        if (bottomNavHeight < 0) {
            bottomNavHeight = getActivity().findViewById(R.id.bottom_navigation).getHeight();
        }
        return recyclerView.computeVerticalScrollRange() > recyclerView.getHeight() - bottomNavHeight;
    }

    public void startActionMode(int pos) {
        if (actionMode != null) {
            return;
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        actionMode = activity.startSupportActionMode(mActionModeCallback);
        toggleSelection(pos);
    }

    public void itemClick(View itemView, int position) {
        if (actionMode != null) {
            toggleSelection(position);
        } else {
            Cursor cursor = mAdapter.readDatabase(fileNames.get(position));
            cursor.moveToFirst();
            String data = cursor.getString(cursor.getColumnIndexOrThrow(PhotosContract.PhotosEntry.COLUMN_NAME_DATA));
            Log.e(LOG_TAG, "ENTRY TIME: " + cursor.getString(cursor.getColumnIndexOrThrow(PhotosContract.PhotosEntry.COLUMN_NAME_ENTRY_TIME)));
            ((MainActivity) getActivity()).openRecentPhoto(itemView, filePaths.get(position), data);
        }
    }

    public void finishActionMode() {
        actionMode.finish();
    }

    private void toggleSelection(int pos) {
        mAdapter.toggleSelection(pos);
        if (actionMode != null) {
            String title = getString(R.string.selected_count, String.valueOf(mAdapter.getSelectedItemCount()));
            actionMode.setTitle(title);
        }
    }
}
