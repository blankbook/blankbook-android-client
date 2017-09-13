package com.example.jacob.blankbookandroidclient;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.api.models.Group;
import com.example.jacob.blankbookandroidclient.managers.PublicGroupsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupSearchDialogFragment extends DialogFragment implements SearchView.OnQueryTextListener {
    @BindView(R.id.search_bar)
    SearchView searchBar;
    @BindView(R.id.search_results)
    RecyclerView searchResults;

    final int MIN_TIME_BETWEEN_QUERIES = 500;
    final int MIN_SEARCH_TERM_LENGTH = 3;
    final int SEARCH_STATE_SEARCHING = 0;
    final int SEARCH_STATE_COMPLETE = 1;
    final int SEARCH_STATE_INVALID_TERM = 2;

    List<Group> searchResultsList = new ArrayList<>();
    int searchState = SEARCH_STATE_INVALID_TERM;
    long lastQueryTime = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.group_search_dialog, container);
        ButterKnife.bind(this, view);

        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP | Gravity.LEFT);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        window.setAttributes(params);

        searchBar.setOnQueryTextListener(this);
        searchBar.setIconified(false);

        searchResults.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        searchResults.setAdapter(new SearchAdapter());

        return view;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        updateResults(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (System.currentTimeMillis() - lastQueryTime >= MIN_TIME_BETWEEN_QUERIES) {
            updateResults(s);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() - lastQueryTime >= MIN_TIME_BETWEEN_QUERIES) {
                        updateResults(searchBar.getQuery().toString());
                    }
                }
            }, MIN_TIME_BETWEEN_QUERIES - (System.currentTimeMillis() - lastQueryTime));
        }
        return false;
    }

    private void updateResults(final String searchTerm) {
        if (searchTerm.length() < MIN_SEARCH_TERM_LENGTH) {
            changeSearchState(SEARCH_STATE_INVALID_TERM);
            return;
        }
        changeSearchState(SEARCH_STATE_SEARCHING);
        lastQueryTime = System.currentTimeMillis();
        PublicGroupsManager.getGroupSearch(searchTerm, new PublicGroupsManager.OnGroupsRetrieval() {
            @Override
            public void onRetrieval(List<Group> groups) {
                Collections.sort(groups, new LevenshteinGroupComparator(searchTerm));
                searchResultsList = groups;
                changeSearchState(SEARCH_STATE_COMPLETE);
            }

            @Override
            public void onFailure() {
                searchResultsList = new ArrayList<>();
                changeSearchState(SEARCH_STATE_COMPLETE);
            }
        });
    }

    private void changeSearchState(int newState) {
        if (searchState != newState) {
            searchState = newState;
            searchResults.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private class LevenshteinGroupComparator implements Comparator<Group> {
        private String searchTerm;

        private LevenshteinGroupComparator(String searchTerm) {
            super();
            this.searchTerm = searchTerm;
        }

        @Override
        public int compare(Group lhs, Group rhs) {
            return levenshteinDistance(searchTerm, lhs.Name) - levenshteinDistance(searchTerm, rhs.Name);
        }

        private int levenshteinDistance(String lhs, String rhs) {
            int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

            for (int i = 0; i <= lhs.length(); i++)
                distance[i][0] = i;
            for (int j = 1; j <= rhs.length(); j++)
                distance[0][j] = j;

            for (int i = 1; i <= lhs.length(); i++)
                for (int j = 1; j <= rhs.length(); j++)
                    distance[i][j] = Math.min(Math.min(distance[i - 1][j] + 1, distance[i][j - 1] + 1),
                            distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

            return distance[lhs.length()][rhs.length()];
        }
    }

    class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private int LOADING_VIEW = 0;
        private int NO_RESULT_VIEW = 1;
        private int RESULT_VIEW = 2;

        @Override
        public int getItemCount() {
            switch (searchState) {
                case SEARCH_STATE_SEARCHING:
                case SEARCH_STATE_COMPLETE:
                    return Math.max(1, searchResultsList.size());
            }
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            switch (searchState) {
                case SEARCH_STATE_SEARCHING:
                    if (searchResultsList.isEmpty()) {
                        return LOADING_VIEW;
                    } else {
                        return RESULT_VIEW;
                    }
                case SEARCH_STATE_COMPLETE:
                    if (searchResultsList.isEmpty()) {
                        return NO_RESULT_VIEW;
                    } else {
                        return RESULT_VIEW;
                    }
            }
            return -1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == LOADING_VIEW) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.search_list_loading_item, parent, false);
                return new StaticViewHolder(view);
            } else if (viewType == NO_RESULT_VIEW) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.search_list_no_result_item, parent, false);
                return new StaticViewHolder(view);
            } else if (viewType == RESULT_VIEW) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.search_list_item, parent, false);
                return new SearchResultViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder != null && holder.getItemViewType() == RESULT_VIEW) {
                ((SearchResultViewHolder) holder).setResult(searchResultsList.get(position));
            }
        }

        class SearchResultViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.result)
            TextView resultTxt;
            Group result;

            private SearchResultViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            private void setResult(final Group result) {
                this.result = result;
                this.resultTxt.setText(result.Name);

                resultTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).onGroupSearchDialogResult(result);
                        }
                        dismiss();
                    }
                });
            }
        }

        class StaticViewHolder extends RecyclerView.ViewHolder {
            private StaticViewHolder(View view) {
                super(view);
            }
        }
    }
}
