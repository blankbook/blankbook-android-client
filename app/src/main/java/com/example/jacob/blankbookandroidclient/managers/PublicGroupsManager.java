package com.example.jacob.blankbookandroidclient.managers;

import android.support.annotation.NonNull;

import com.example.jacob.blankbookandroidclient.api.RetrofitClient;
import com.example.jacob.blankbookandroidclient.api.models.Group;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicGroupsManager {
    public static void getGroup(String group, final OnGroupRetrieval callback) {
        RetrofitClient.getInstance().getBlankBookAPI().getGroup(group)
                .enqueue(new Callback<Group>() {
            @Override
            public void onResponse(@NonNull Call<Group> call, @NonNull Response<Group> response) {
                final Group group = response.body();
                if (callback != null) {
                    if (group == null) {
                        callback.onFailure();
                    } else {
                        callback.onRetrieval(group);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Group> call, @NonNull Throwable t) {
                if (callback != null) {
                    callback.onFailure();
                }
            }
        });
    }

    public static void getGroupSearch(String term, final OnGroupsRetrieval callback) {
        RetrofitClient.getInstance().getBlankBookAPI().getGroupSearch(term)
                .enqueue(new Callback<List<Group>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Group>> call, @NonNull Response<List<Group>> response) {
                        final List<Group> groups = response.body();
                        if (callback != null) {
                            if (groups == null) {
                                callback.onFailure();
                            } else {
                                callback.onRetrieval(groups);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Group>> call, @NonNull Throwable t) {
                        if (callback != null) {
                            callback.onFailure();
                        }
                    }
                });
    }

    public interface OnGroupRetrieval {
        void onRetrieval(Group group);
        void onFailure();
    }

    public interface OnGroupsRetrieval {
        void onRetrieval(List<Group> group);
        void onFailure();
    }
}
