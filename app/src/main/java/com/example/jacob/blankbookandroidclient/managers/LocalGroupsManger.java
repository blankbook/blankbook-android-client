package com.example.jacob.blankbookandroidclient.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArraySet;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.models.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LocalGroupsManger {
    private static LocalGroupsManger instance;
    private SharedPreferences sharedPrefs;
    private String groupsKey;
    private String groupProtectedPrefix;
    private String feedsKey;
    private String feedKeyPrefix;

    public static LocalGroupsManger getInstance() {
        if (instance == null) {
            instance = new LocalGroupsManger();
        }
        return instance;
    }

    public void init(Context context) {
        final String fileKey = context.getResources().getString(R.string.preferences_file_key);
        groupsKey = context.getResources().getString(R.string.preferences_groups_key);
        groupProtectedPrefix = context.getResources().getString(R.string.preferences_group_protected_key_prefix);
        feedsKey = context.getResources().getString(R.string.preferences_feeds_key);
        feedKeyPrefix = context.getResources().getString(R.string.preferences_feed_key_prefix);

        sharedPrefs = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);
    }

    public List<String> getGroupNames() {
        return new ArrayList<>(sharedPrefs.getStringSet(groupsKey, new HashSet<String>()));
    }

    public List<String> getFeeds() {
        return new ArrayList<>(sharedPrefs.getStringSet(feedsKey, new HashSet<String>()));
    }

    public Set<String> getFeedGroups(String feed) {
        return sharedPrefs.getStringSet(feedKeyPrefix + feed, new HashSet<String>());
    }

    public boolean isGroupProtected(String group) {
        return sharedPrefs.getBoolean(groupProtectedPrefix + group, false);
    }

    public void addGroup(Group group) {
        List<String> groupNames = getGroupNames();
        groupNames.add(group.Name);
        sharedPrefs.edit().putStringSet(groupsKey, new HashSet<>(groupNames)).apply();
        sharedPrefs.edit().putBoolean(groupProtectedPrefix + group.Name, group.Protected).apply();
    }

    public void removeGroup(String groupName) {
        List<String> groupNames = getGroupNames();
        groupNames.remove(groupName);
        sharedPrefs.edit().putStringSet(groupsKey, new HashSet<>(groupNames)).apply();
        sharedPrefs.edit().remove(groupProtectedPrefix + groupName).apply();
    }

    public void addFeed(String feed, Set<String> feedGroups) {
        List<String> feeds = getFeeds();
        feeds.add(feed);
        setFeeds(feeds);
        setFeedGroups(feed, feedGroups);
    }

    public void removeFeed(String feed) {
        List<String> feeds = getFeeds();
        for (Iterator<String> iter = feeds.iterator(); iter.hasNext();) {
            String cur = iter.next();
            if (feed.equals(cur)) {
                iter.remove();
                setFeeds(feeds);
                deleteFeedGroup(feed);
                break;
            }
        }
    }

    public void addGroupToFeed(String feed, String group) {
        Set<String> groups = getFeedGroups(feed);
        groups.add(group);
        setFeedGroups(feed, groups);
    }

    public void removeGroupFromFeed(String feed, String group) {
        Set<String> groups = getFeedGroups(feed);
        for (Iterator<String> iter = groups.iterator(); iter.hasNext();) {
            String cur = iter.next();
            if (group.equals(cur)) {
                iter.remove();
                setFeedGroups(feed, groups);
                break;
            }
        }
    }

    private void setGroups(List<Group> groups) {
        for (Group group : groups) {
            addGroup(group);
        }
    }

    private void setFeeds(List<String> feeds) {
        sharedPrefs.edit().putStringSet(feedsKey, new HashSet<>(feeds)).apply();
    }

    private void setFeedGroups(String feed, Set<String> feedGroups) {
        sharedPrefs.edit().putStringSet(feedKeyPrefix + feed, feedGroups).apply();
    }

    private void deleteFeedGroup(String feed) {
        sharedPrefs.edit().remove(feedKeyPrefix + feed).apply();
    }
}
