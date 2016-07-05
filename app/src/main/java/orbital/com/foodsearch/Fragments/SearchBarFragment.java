package orbital.com.foodsearch.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.zhanghai.android.materialprogressbar.IndeterminateHorizontalProgressDrawable;
import orbital.com.foodsearch.Activities.OcrActivity;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Utils.AnimUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchBarFragment extends Fragment {

    private static final String SEARCH_BAR_TRANS = "searchBarTrans";
    private int searchBarTrans;

    public SearchBarFragment() {
    }
    
    public static SearchBarFragment newInstance(int searchBarTrans) {
        Bundle args = new Bundle();
        args.putInt(SEARCH_BAR_TRANS, searchBarTrans);
        SearchBarFragment fragment = new SearchBarFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        searchBarTrans = args.getInt(SEARCH_BAR_TRANS);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_bar, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initializeBar();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initializeBar() {
        ProgressBar progressBar = (ProgressBar)getView().getRootView().findViewById(R.id.search_progress);
        progressBar.setIndeterminateDrawable(new IndeterminateHorizontalProgressDrawable(getActivity()));

        final LinearLayout searchBar = (LinearLayout)getView().findViewById(R.id.search_bar);
        final EditText editText = (EditText) searchBar.findViewById(R.id.edit_text);
        final Button searchButton = (Button) searchBar.findViewById(R.id.start_search);
        final ImageButton cancelButton = (ImageButton) searchBar.findViewById(R.id.cancel_search);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editText.setCursorVisible(true);
                } else {
                    editText.setCursorVisible(false);
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchButton.performClick();
                    return true;
                }
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchButton.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchButton.setEnabled(true);
            }
        });

        View.OnClickListener listener= new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cancel_search:
                        AnimUtils.hideSearchBar(getView().getRootView().findViewById(R.id.search_bar_container),
                                searchBarTrans);
                        break;
                    case R.id.edit_text:
                        break;
                    case R.id.start_search:
                        InputMethodManager imm = (InputMethodManager) getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        searchButton.setEnabled(false);
                        ((OcrActivity) getActivity()).search(editText.getText().toString());
                        break;
                }
            }
        };
        editText.setOnClickListener(listener);
        searchButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);
    }

}
