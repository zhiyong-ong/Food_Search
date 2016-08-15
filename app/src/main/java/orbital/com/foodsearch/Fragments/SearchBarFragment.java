package orbital.com.foodsearch.Fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.zhanghai.android.materialprogressbar.IndeterminateHorizontalProgressDrawable;
import orbital.com.foodsearch.Activities.MainActivity;
import orbital.com.foodsearch.Activities.OcrActivity;
import orbital.com.foodsearch.Helpers.BingTranslate;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Utils.AnimUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchBarFragment extends Fragment {

    private static final String LOG_TAG = "FOODIES";
    private SharedPreferences sharedPreferencesSettings = null;
    private String[] langValuesArr = null;
    private String[] langKeysArr = null;
    private ImageButton translateBtn;
    private String[] listLanguages;
    private String[] translationTitles;

    public SearchBarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_bar, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeBar();
        sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    /**
     * This method is approximately 30cm long.
     */
    private void initializeBar() {
        ProgressBar progressBar = (ProgressBar) getView().getRootView().findViewById(R.id.searchbar_progress);
        progressBar.setIndeterminateDrawable(new IndeterminateHorizontalProgressDrawable(getActivity()));

        final PercentRelativeLayout searchBar = (PercentRelativeLayout) getView().findViewById(R.id.search_bar);
        final EditText editText = (EditText) searchBar.findViewById(R.id.searchbar_edit_text);
        final Button searchButton = (Button) searchBar.findViewById(R.id.searchbar_start_search);
        final ImageButton cancelButton = (ImageButton) searchBar.findViewById(R.id.cancel_searchbar);
        final View translateLayout = searchBar.findViewById(R.id.searchbar_translate_layout);
        final TextView translateTitleView = (TextView) searchBar.findViewById(R.id.searchbar_translate_title);
        final TextView translateTextView = (TextView) searchBar.findViewById(R.id.searchbar_translate_text);
        translateBtn = (ImageButton) searchBar.findViewById(R.id.searchbar_translate_btn);
        final ImageButton translateCloseBtn = (ImageButton) searchBar.findViewById(R.id.searchbar_translate_close);
        final InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

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

        View.OnClickListener listener = new View.OnClickListener() {
            private boolean translateOpen = false;

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cancel_searchbar:
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        ((OcrActivity) getActivity()).cancelSearch();
                        break;
                    case R.id.searchbar_edit_text:
                        break;
                    case R.id.searchbar_start_search:
                        if (v.isEnabled()) {
                            searchButton.setEnabled(false);
                            editText.clearFocus();
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            ((OcrActivity) getActivity()).startSearch(editText.getText().toString().trim());
                        }
                        break;
                    case R.id.searchbar_translate_btn:
                        editText.clearFocus();
                        translateTitleView.setText(getTitle());
                        AnimUtils.circularReveal(v, translateLayout, searchBar, null);
                        translateOpen = true;
                        break;
                    case R.id.searchbar_translate_close:
                        if (translateOpen) {
                            editText.clearFocus();
                            AnimUtils.exitReveal(translateLayout, translateBtn, null);
                            translateOpen = false;
                        }
                        break;
                    case R.id.searchbar_translate_text:
                    case R.id.searchbar_translate_title:
                        if (translateOpen) {
                            editText.clearFocus();
                            AnimUtils.exitReveal(translateLayout, editText, new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    AnimUtils.fadeIn(editText, AnimUtils.TEXT_UPDATE);
                                }
                            });
                            editText.setText(translateTextView.getText());
                            editText.setVisibility(View.INVISIBLE);
                            translateOpen = false;
                        }
                        break;
                }
            }
        };
        editText.setOnClickListener(listener);
        searchButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);
        translateBtn.setOnClickListener(listener);
        translateCloseBtn.setOnClickListener(listener);
        translateTextView.setOnClickListener(listener);
        translateTitleView.setOnClickListener(listener);
        translateBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showPopup(view);
                return true;
            }
        });
    }

    /**
     * Find out whats the current index and set that item to a checkbox.
     * Then setup and show the popup menu.
     *
     * @param anchoredView view to be anchored to showpopup
     */
    private void showPopup(View anchoredView) {
        if (langValuesArr == null || langKeysArr == null) {
            langValuesArr = getActivity().getResources().getStringArray(R.array.listLanguagesValues);
            langKeysArr = getActivity().getResources().getStringArray(R.array.listLanguages);
        }
        PopupMenu popup = new PopupMenu(getActivity(), anchoredView);
        popup.setOnMenuItemClickListener(new LanguageMenuListener());
        //anchoredView.setOnTouchListener(popup.getDragToOpenListener());
        Menu menu = popup.getMenu();
        String currentLangValue = sharedPreferencesSettings.getString(getActivity().getString(R.string.select_lang_key), "en");
        Log.e(LOG_TAG, "current lang: " + currentLangValue);
        for (int i = 0; i < langKeysArr.length; i++) {
            String key = langKeysArr[i];
            String value = langValuesArr[i];
            MenuItem item = menu.add(Menu.NONE, i, i, key);
            if (value.equals(currentLangValue)) {
                Log.e(LOG_TAG, "current value: " + value);
                item.setCheckable(true).setChecked(true);
            }
        }
        popup.show();

    }

    private String getTitle() {
        if (listLanguages == null) {
            listLanguages = getResources().getStringArray(R.array.listLanguagesValues);
        }
        if (translationTitles == null) {
            translationTitles = getResources().getStringArray(R.array.translationTitle);
        }
        int selectedIndex = 11;
        for (int i = 0; i < listLanguages.length; i++) {
            if (MainActivity.BASE_LANGUAGE.equals(listLanguages[i])) {
                selectedIndex = i;
            }
        }
        return translationTitles[selectedIndex];
    }

    /**
     * On menu item clicked, we check for language keys to find selected index.
     * Then using index find the selected value. Use this value for translate task.
     * And then set value on shared preferences accordingly.
     */
    private class LanguageMenuListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (langValuesArr == null || langKeysArr == null) {
                langValuesArr = getActivity().getResources().getStringArray(R.array.listLanguagesValues);
                langKeysArr = getActivity().getResources().getStringArray(R.array.listLanguages);
            }
            String selectedValue = langValuesArr[menuItem.getItemId()];
            EditText editText = (EditText) getView().findViewById(R.id.searchbar_edit_text);
            getView().findViewById(R.id.searchbar_translate_btn).setEnabled(false);
            TranslateTask task = new TranslateTask(getView());
            task.execute(editText.getText().toString(), selectedValue);
            sharedPreferencesSettings.edit()
                    .putString(getActivity().getString(R.string.select_lang_key), selectedValue)
                    .apply();
            return true;
        }
    }

    /**
     * This task performs a translation job using the given params.
     * OnPostExecute, set translate button to enable and perform click
     * on translate button to show the translated view.
     */
    private class TranslateTask extends AsyncTask<String, Void, String> {
        View rootView;

        TranslateTask(View rootView) {
            this.rootView = rootView;
        }

        @Override
        protected String doInBackground(String... params) {
            return BingTranslate.getTranslatedText(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            ((TextView) rootView.findViewById(R.id.searchbar_translate_text)).setText(result);
            translateBtn.setEnabled(true);
            translateBtn.performClick();
            super.onPostExecute(result);
        }
    }

}
