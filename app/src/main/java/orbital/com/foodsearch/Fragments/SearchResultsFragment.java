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
import android.widget.ImageView;

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
    private static final String ARG_PARAM1 = "param1";
    private List<ImageValue> mImageValues = null;
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
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, searchParam);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchParam = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initializeRecycler();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initializeRecycler() {
        RecyclerView rvImages = (RecyclerView) getView().findViewById(R.id.recycler_view);

        ImageValue test1 = new ImageValue();
        test1.setContentUrl("http://www.telegraph.co.uk/content/dam/science/2016/03/14/cat_3240574b-large_trans++pJliwavx4coWFCaEkEsb3kvxIt-lGGWCWqwLa_RXJU8.jpg");
        test1.setName("Cat");
        ImageValue test2 = new ImageValue();
        test2.setContentUrl("https://i.ytimg.com/vi/mW3S0u8bj58/maxresdefault.jpg");
        test2.setName("Cat 2");
        mImageValues = new ArrayList<>(Arrays.asList(test1, test2));

        ImageView cardImageView = (ImageView)getView().findViewById(R.id.card_image);
        BingImageAdapter adapter = new BingImageAdapter(getActivity(), mImageValues);
        rvImages.setAdapter(adapter);
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
