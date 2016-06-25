package orbital.com.foodsearch.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import orbital.com.foodsearch.Adapters.BingImageAdapter;
import orbital.com.foodsearch.Models.ImageValue;
import orbital.com.foodsearch.R;

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
    private String searchParam;

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
        if (savedInstanceState != null) {
            //mImageValues = savedInstanceState.getParcelableArrayList(SAVED_IMAGE_VALUES);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        if () {
//            outState.putParcelableArrayList(SAVED_IMAGE_VALUES, mImageValues);
//        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initializeRecycler();
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * This method is used to update the recyclerView with new image values
     * @param newImageValues new Image values to be displayed in recyclerView
     */
    public void updateRecycler(List<ImageValue> newImageValues) {
        mImageValues.clear();
        mImageValues.addAll(newImageValues);
        mAdapter.notifyDataSetChanged();
    }

    private void initializeRecycler() {
        RecyclerView rvImages = (RecyclerView) getView().findViewById(R.id.recycler_view);

        ImageValue test1 = new ImageValue();
        test1.setThumbnailUrl("http://s1.dmcdn.net/UkwzE.jpg");
        test1.setName("Cat");
        ImageValue test2 = new ImageValue();
        test2.setThumbnailUrl("https://i.ytimg.com/vi/mW3S0u8bj58/maxresdefault.jpg");
        test2.setName("Cat 2");
        mImageValues = new ArrayList<>(Arrays.asList(test1, test2));

        mAdapter = new BingImageAdapter(getActivity(), mImageValues);
        rvImages.setAdapter(mAdapter);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(getActivity());
        layoutMgr.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvImages.setLayoutManager(layoutMgr);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_results, container, false);
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
