package orbital.com.foodsearch.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import orbital.com.foodsearch.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HintDialogFragment extends Fragment {


    public HintDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hint_dialog, container, false);
    }

}
