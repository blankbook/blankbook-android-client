package com.example.jacob.blankbookandroidclient.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.LongSparseArray;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.ContributorId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContributorIdManager {
    private static ContributorIdManager instance;
    private SharedPreferences sharedPrefs;
    private String contributorIdPrefix;
    // https://material.io/guidelines/style/color.html#color-color-palette
    private final List<String> contributorColors = new ArrayList<>(Arrays.asList(
            "F44336",
            "E91E63",
            "9C27B0",
            "673AB7",
            "3F51B5",
            "2196F3",
            "03A9F4",
            "00BCD4",
            "009688",
            "4CAF50",
            "8BC34A",
            "CDDC39",
            "FFEB3B",
            "FFC107",
            "FF9800",
            "FF5722",
            "795548"
    ));

    public static ContributorIdManager getInstance() {
        if (instance == null) {
            instance = new ContributorIdManager();
        }
        return instance;
    }

    public void init(Context context) {
        final String fileKey = context.getResources().getString(R.string.preferences_file_key);
        contributorIdPrefix = context.getResources().getString(R.string.preferences_contributor_id_prefix);
        sharedPrefs = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
    }

    public void getContributorId(Long postId, final OnContributorIdRetrievalListener listener) {
        final String key = contributorIdPrefix + postId.toString();
        final int sharedPrefsResult = sharedPrefs.getInt(key, -1);
        if (sharedPrefsResult != -1) {
            listener.onRetrieval(sharedPrefsResult);
        } else {
            RetrofitClient.getInstance().getBlankBookAPI().getContributorid(postId)
                    .enqueue(new Callback<ContributorId>() {
                        @Override
                        public void onResponse(Call<ContributorId> call, Response<ContributorId> response) {
                            if (response.code() == 200 && response.body() != null) {
                                int id = response.body().id;
                                listener.onRetrieval(id);
                                sharedPrefs.edit().putInt(key, id).apply();
                            } else {
                                listener.onFailure();
                            }
                        }

                        @Override
                        public void onFailure(Call<ContributorId> call, Throwable t) {
                            listener.onFailure();
                        }
                    });
        }
    }

    public String getColorForId(int id) {
        return contributorColors.get(id % contributorColors.size());
    }

    public interface OnContributorIdRetrievalListener {
        void onRetrieval(Integer contributorId);
        void onFailure();
    }
}
