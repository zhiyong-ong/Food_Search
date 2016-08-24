package orbital.com.menusnap.Fragments;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInDownAnimator;
import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;
import orbital.com.menusnap.Activities.MainActivity;
import orbital.com.menusnap.Adapters.RecentImageAdapter;
import orbital.com.menusnap.BuildConfig;
import orbital.com.menusnap.DAO.PhotosContract;
import orbital.com.menusnap.DAO.PhotosDAO;
import orbital.com.menusnap.DAO.PhotosDBHelper;
import orbital.com.menusnap.R;
import orbital.com.menusnap.Utils.AnimUtils;
import orbital.com.menusnap.Utils.FileUtils;
import orbital.com.menusnap.Utils.ViewUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentsFragment extends android.app.Fragment {

    private static final String LOG_TAG = "FOODIES";
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    private RecentImageAdapter mAdapter;
    private List<Uri> fileUris;
    private List<String> fileNames;
    private ActionMode actionMode;
    private int bottomNavHeight = -1;
    private final View.OnLayoutChangeListener mLayoutListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
            updateRecyclerScroll();
        }
    };
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
                    String title = getString(R.string.selected_count, mAdapter.getSelectedItemCount());
                    actionMode.setTitle(title);
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
            if (fileUris.isEmpty()) {
                View recentsOverlay = getView().findViewById(R.id.empty_recents_layout);
                AnimUtils.fadeIn(recentsOverlay, AnimUtils.OVERLAY_DURATION);
            }
        }
    };

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
        fileUris = new ArrayList<>();
        fileNames = new ArrayList<>();

        mAdapter = new RecentImageAdapter(getActivity(), this, fileUris, fileNames);
        setRecyclerLayout();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(0);
        initializeFiles();

//        Log.e(LOG_TAG, "file paths: " + fileUris.size());
//        Log.e(LOG_TAG, "file names: " + fileNames.size());
        return rootView;
    }

    public void setRecyclerLayout() {
        RecyclerView.ItemAnimator animator = null;
        switch (MainActivity.viewType) {
            case ViewUtils.GRID_VIEW_ID:
                mLayoutManager = new GridLayoutManager(getActivity(), 3);
                animator = new FadeInDownAnimator();
                animator.setMoveDuration(AnimUtils.FAST_FADE);
                mRecyclerView.setItemAnimator(animator);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mRecyclerView.getLayoutParams();
                params.setMargins(ViewUtils.dpToPx(6), ViewUtils.dpToPx(8), ViewUtils.dpToPx(6), ViewUtils.dpToPx(8));
                break;
            case ViewUtils.LIST_VIEW_ID:
                mLayoutManager = new LinearLayoutManager(getActivity());
                ((LinearLayoutManager) mLayoutManager).setOrientation(LinearLayoutManager.VERTICAL);
                animator = new FadeInLeftAnimator();
                animator.setMoveDuration(AnimUtils.FAST_FADE);
                mRecyclerView.setItemAnimator(animator);
                params = (FrameLayout.LayoutParams) mRecyclerView.getLayoutParams();
                params.setMargins(ViewUtils.dpToPx(0), ViewUtils.dpToPx(4), ViewUtils.dpToPx(0), ViewUtils.dpToPx(4));
                break;
        }
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.requestLayout();
        if (mAdapter != null) {
            mAdapter.setViewType(MainActivity.viewType);
        }
    }

    private void initializeFiles() {
        ((MainActivity) getActivity()).savedNewImage = false;
        final File[] newFiles = getFiles();
        clearFiles();
        addAllFiles(newFiles);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        final File[] newFiles = getFiles();
        if (newFiles.length == 0) {
            getView().findViewById(R.id.empty_recents_layout).setVisibility(View.VISIBLE);
            clearFiles();
            mAdapter.notifyDataSetChanged();
        } else {
            if (!((MainActivity) getActivity()).savedNewImage) {
                clearFiles();
                addAllFiles(newFiles);
                mAdapter.notifyDataSetChanged();
                // After loading the full set of files, then trim recents.
                trimRecents();
            } else {
                ((MainActivity) getActivity()).savedNewImage = false;
                // Only scroll to top when not scrollable so that animations still show
                if (isRecyclerScrollable()) {
                    scrollToTop();
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int numberDeleted = fileNames.size() + 1 - newFiles.length;
                        addFile(newFiles[0]);
                        // scroll to top after file is added to show animation
                        scrollToTop();
                        if (numberDeleted > 0) {
                            mAdapter.notifyItemRangeRemoved(removeLastFiles(numberDeleted), numberDeleted);
                        }
                        mAdapter.notifyItemInserted(0);
                    }
                }, 550);
            }
            getView().findViewById(R.id.empty_recents_layout).setVisibility(View.INVISIBLE);
        }
    }

    private void trimRecents() {
        if (MainActivity.IMAGE_RECENTS_COUNT < fileNames.size()) {
            final int numberToTrim = fileNames.size() - MainActivity.IMAGE_RECENTS_COUNT;
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mAdapter.trimData(numberToTrim);
                    mAdapter.notifyItemRangeRemoved(removeLastFiles(numberToTrim), numberToTrim);
                    if (mAdapter.getItemCount() == 0) {
                        View recentsOverlay = getView().findViewById(R.id.empty_recents_layout);
                        AnimUtils.fadeIn(recentsOverlay, AnimUtils.OVERLAY_DURATION);
                    }
                    return true;
                }
            });
        }
    }

    private File[] getFiles() {
        File root = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                , "Recent_Images");
        if (!root.exists()) {
            if (!root.mkdirs()) {
                Log.e(LOG_TAG, getString(R.string.mkdir_fail_text));
            }
        }
        File[] files = root.listFiles();
        FileUtils.sortFileByTime(files);
        return files;
    }

    private void addAllFiles(File[] files) {
        for (File file : files) {
            Uri fileUri = FileProvider.getUriForFile(getActivity(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            fileUris.add(fileUri);
            fileNames.add(file.getName());
        }
    }

    private void addFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(getActivity(),
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        fileUris.add(0, fileUri);
        fileNames.add(0, file.getName());
    }

    private int removeLastFiles(int deleteNumber) {
        int deletedIndex = fileUris.size() - deleteNumber;
        fileUris.subList(deletedIndex, fileUris.size()).clear();
        fileNames.subList(deletedIndex, fileNames.size()).clear();
        return deletedIndex;
    }

    private void clearFiles() {
        fileUris.clear();
        fileNames.clear();
    }

    public void smoothScrollToTop() {
        if (fileUris.size() > 0) {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    public void scrollToTop() {
        if (fileUris.size() > 0) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    public void updateRecyclerScroll() {
        if (isRecyclerScrollable()) {
            ((MainActivity) getActivity()).enableScroll();
        } else {
            ((MainActivity) getActivity()).disableScroll();
        }
    }

    private boolean isRecyclerScrollable() {
        if (bottomNavHeight <= 0) {
            bottomNavHeight = getActivity().findViewById(R.id.bottom_navigation).getHeight();
        }
        int range = mRecyclerView.computeVerticalScrollRange();
        return range > mRecyclerView.getHeight() - bottomNavHeight;
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
            PhotosDBHelper mDBHelper = new PhotosDBHelper(getActivity());
            Cursor cursor = PhotosDAO.readDatabaseGetRow(fileNames.get(position), mDBHelper);
            cursor.moveToFirst();
            String data = cursor.getString(cursor.getColumnIndexOrThrow(PhotosContract.PhotosEntry.COLUMN_NAME_DATA));
//            Log.e(LOG_TAG, "ENTRY TIME: " + cursor.getString(cursor.getColumnIndexOrThrow(PhotosContract.PhotosEntry.COLUMN_NAME_ENTRY_TIME)));
            cursor.close();
            ((MainActivity) getActivity()).openRecentPhoto(itemView, fileUris.get(position), data);
        }
    }

    public void finishActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void toggleSelection(int pos) {
        mAdapter.toggleSelection(pos);
        if (actionMode != null) {
            String title = getString(R.string.selected_count, mAdapter.getSelectedItemCount());
            actionMode.setTitle(title);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecyclerView.addOnLayoutChangeListener(mLayoutListener);
    }

    @Override
    public void onStop() {
        if (mRecyclerView == null) {
            return;
        }
        mRecyclerView.removeOnLayoutChangeListener(mLayoutListener);
        super.onStop();
    }
}
