package orbital.com.foodsearch.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.activities.OcrActivity;
import orbital.com.foodsearch.adapters.BingImageAdapter;
import orbital.com.foodsearch.models.ImageSearchPOJO.ImageValue;
import orbital.com.foodsearch.views.SnappyRecyclerView;

public class SearchResultsFragment extends Fragment {
    private static final int IMAGE_COUNT = OcrActivity.IMAGES_COUNT;
    private final String RECYCLER_SAVED_STATE = "RECYCLERSAVEDSTATE";
    private ArrayList<ImageValue> mImageValues;
    private BingImageAdapter mAdapter;
    private SnappyRecyclerView mRecyclerView;
    private int mDotsCount = 0;
    private int accentColor = Color.WHITE;
    private ArrayList<TextView> mDotsTexts;
    private LinearLayout mDotsLayout;

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
        mDotsLayout = (LinearLayout) view.findViewById(R.id.dots_layout);
        mRecyclerView = (SnappyRecyclerView) view.findViewById(R.id.recycler_view);
        accentColor = ContextCompat.getColor(getActivity(), R.color.colorAccent);
        mImageValues = new ArrayList<>(IMAGE_COUNT);
        mDotsTexts = new ArrayList<>(IMAGE_COUNT);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(getActivity());
        layoutMgr.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutMgr);
        mAdapter = new BingImageAdapter(getActivity(), mImageValues);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new DotsOnScrollListener());
        OverScrollDecoratorHelper.setUpOverScroll(mRecyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
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
        mDotsTexts.clear();
        mDotsLayout.removeAllViewsInLayout();
        mImageValues.clear();
    }

    public void finalizeRecycler() {
        setupCustomScroll();
        mRecyclerView.scrollToPosition(0);
        mAdapter.notifyDataSetChanged();
    }

    private void setupCustomScroll() {
        mDotsCount = mImageValues.size();
        for (int i = 0; i < mDotsCount; i++) {
            TextView dot = new TextView(getActivity());
            dot.setText(".");
            dot.setTextSize(45);
            dot.setTypeface(null, Typeface.BOLD);
            dot.setTextColor(android.graphics.Color.GRAY);
            mDotsLayout.addView(dot);
            mDotsTexts.add(dot);
        }
        mDotsTexts.get(0).setTextColor(accentColor);
    }

    public void notifyRecycler() {
        mAdapter.notifyDataSetChanged();
    }

    public List<String> getUrls() {
        List<String> urls = new ArrayList<>();
        for (ImageValue imageValue : mImageValues) {
            urls.add(imageValue.getContentUrl());
        }
        return urls;
    }

    private class DotsOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int scrollX = recyclerView.computeHorizontalScrollOffset();
            int range = recyclerView.computeHorizontalScrollRange();
            float scrollPercentage = (float) scrollX / (float) range;
            int newPosition = Math.round(scrollPercentage * mImageValues.size());
            for (int i = 0; i < mDotsCount; i++) {
                mDotsTexts.get(i).setTextColor(Color.GRAY);
            }
            mDotsTexts.get(newPosition).setTextColor(accentColor);
            super.onScrolled(recyclerView, dx, dy);
        }
    }
}
