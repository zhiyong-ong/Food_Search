package orbital.com.foodsearch.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Activities.OcrActivity;
import orbital.com.foodsearch.Adapters.BingImageAdapter;
import orbital.com.foodsearch.Models.ImageSearchPOJO.ImageValue;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Views.SnappyRecyclerView;

public class SearchResultsFragment extends Fragment {
    private static final int IMAGE_COUNT = OcrActivity.IMAGES_COUNT;
    private final String RECYCLER_SAVED_STATE = "RECYCLERSAVEDSTATE";
    private ArrayList<ImageValue> mImageValues;
    private BingImageAdapter mAdapter;
    private SnappyRecyclerView mRecyclerView;

    public SearchResultsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param searchParam Parameter 1.
     * @return A new instance of fragment SearchResultsFragment.
     */
    public static SearchResultsFragment newInstance(String searchParam) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        if (mRecyclerView == null) {
            initializeRecycler(view);
        } else {
            Log.e("adapterList: ", mImageValues.toString());
        }
        return view;
    }

    private void initializeRecycler(View view) {
        mRecyclerView = (SnappyRecyclerView) view.findViewById(R.id.recycler_view);
        mImageValues = new ArrayList<>(IMAGE_COUNT);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(getActivity());
        layoutMgr.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutMgr);
        mAdapter = new BingImageAdapter(getActivity(), mImageValues);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);
    }

    /**
     * This method is used to update the recyclerView with new IMAGE_KEY values
     *
     * @param newImageValue new Image values to be displayed in recyclerView
     */
    public void updateRecyclerList(ImageValue newImageValue) {
        mImageValues.add(newImageValue);
    }

    public void clearRecycler() {
        mImageValues.clear();
    }
    public void finalizeRecycler() {
        mRecyclerView.scrollToPosition(0);
        mAdapter.notifyDataSetChanged();
    }

    public List<String> getUrls() {
        List<String> urls = new ArrayList<>();
        for (ImageValue imageValue : mImageValues) {
            urls.add(imageValue.getContentUrl());
        }
        return urls;
    }
}
