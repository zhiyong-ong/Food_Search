package orbital.com.foodsearch.Fragments;


import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Adapters.RecentImageAdapter;
import orbital.com.foodsearch.R;

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
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        getFiles();
        mAdapter = new RecentImageAdapter(getActivity(), filePaths, fileNames);
        mRecyclerView.setAdapter(mAdapter);
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
            clearFiles();
            getFiles();
            mAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

}
