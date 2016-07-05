package orbital.com.foodsearch.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Activities.OcrActivity;
import orbital.com.foodsearch.Adapters.BingImageAdapter;
import orbital.com.foodsearch.Models.ImageSearchPOJO.ImageValue;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Views.SnappyRecyclerView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchResultsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchResultsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchResultsFragment extends Fragment {
    private final String SAVED_IMAGE_VALUES = "savedImageValues";
    private ArrayList<ImageValue> mImageValues = null;
    private BingImageAdapter mAdapter = null;
    private static final int IMAGE_COUNT = OcrActivity.IMAGE_COUNT;
    private OnFragmentInteractionListener mListener;

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
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_IMAGE_VALUES)) {
            Gson gson = new Gson();
            mImageValues = gson.fromJson(
                    savedInstanceState.getString(SAVED_IMAGE_VALUES),
                    ArrayList.class);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
      if (mImageValues != null) {
          Gson gson = new Gson();
          outState.putString(SAVED_IMAGE_VALUES, gson.toJson(mImageValues));
      }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initializeRecycler();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initializeRecycler() {
        SnappyRecyclerView rvImages = (SnappyRecyclerView) getView().findViewById(R.id.recycler_view);
        // mImageValues = new ArrayList<>(Arrays.asList(test1, test2));
        mImageValues = new ArrayList<>(IMAGE_COUNT);

        mAdapter = new BingImageAdapter(getActivity(), mImageValues);
        rvImages.setAdapter(mAdapter);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(getActivity());
        layoutMgr.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvImages.setHasFixedSize(true);
        rvImages.setLayoutManager(layoutMgr);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_results, container, false);
    }

    /**
     * This method is used to update the recyclerView with new image values
     * @param newImageValues new Image values to be displayed in recyclerView
     */
    public void updateRecyclerList(List<ImageValue> newImageValues) {
        mImageValues.clear();
        mImageValues.addAll(newImageValues);

        SnappyRecyclerView recyclerView = (SnappyRecyclerView) getView().findViewById(
                R.id.recycler_view);
        recyclerView.scrollToPosition(0);
    }

    public void finalizeRecycler(String translatedText) {
        TextView translatedTitle = (TextView) getView().findViewById(R.id.translatedSearchText);
        translatedTitle.setText(translatedText);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
