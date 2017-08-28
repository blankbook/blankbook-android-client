package com.example.jacob.blankbookandroidclient.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.jacob.blankbookandroidclient.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class LocalGroupsManger {
    private LocalGroupsManger instance;
    private SharedPreferences sharedPrefs;
    private String groupsKey;
    private String feedsKey;
    private String feedKeyPrefix;
    private Set<String> groups;
    private Set<String> feeds;
    private HashMap<String, Set<String>> feedsGroups = new HashMap<>();

    public LocalGroupsManger getInstance() {
        if (instance == null) {
            instance = new LocalGroupsManger();
        }
        return instance;
    }

    public void init(Context context) {
        final String fileKey = context.getResources().getString(R.string.preferences_file_key);
        groupsKey = context.getResources().getString(R.string.preferences_groups_key);
        feedsKey = context.getResources().getString(R.string.preferences_feeds_key);
        feedKeyPrefix = context.getResources().getString(R.string.preferences_feed_key_prefix);

        sharedPrefs = context.getSharedPreferences(fileKey, Context.MODE_PRIVATE);

        groups = sharedPrefs.getStringSet(groupsKey, new HashSet<String>());
        feeds = sharedPrefs.getStringSet(feedsKey, new HashSet<String>());
        for (String feed : feeds) {
            feedsGroups.put(feed, sharedPrefs.getStringSet(feedKeyPrefix + feed, new HashSet<String>()));
        }
    }

    public Set<String> getGroups() {
        return groups;
    }

    public Set<String> getFeeds() {
        return feeds;
    }

    public Set<String> getFeedGroups(String feed) {
        return feedsGroups.get(feed);
    }

    public void addGroup(String group) {
        Set<String> groups = getGroups();
        groups.add(group);
        setGroups(groups);
    }

    public void removeGroup(String group) {
        Set<String> groups = getGroups();
        for (Iterator<String> iter = groups.iterator(); iter.hasNext();) {
            String cur = iter.next();
            if (group.equals(cur)) {
                iter.remove();
                setGroups(groups);
                break;
            }
        }
    }

    public void addFeed(String feed, Set<String> feedGroups) {
        Set<String> feeds = getFeeds();
        feeds.add(feed);
        setFeeds(feeds);
        setFeedGroups(feed, feedGroups);
    }

    public void removeFeed(String feed) {
        Set<String> feeds = getFeeds();
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
                setGroups(groups);
                break;
            }
        }
    }

    private void setGroups(Set<String> groups) {
        sharedPrefs.edit().putStringSet(groupsKey, groups).apply();
        this.groups = groups;
    }

    private void setFeeds(Set<String> feeds) {
        sharedPrefs.edit().putStringSet(feedsKey, feeds).apply();
        this.feeds = feeds;
    }

    private void setFeedGroups(String feed, Set<String> feedGroups) {
        sharedPrefs.edit().putStringSet(feedKeyPrefix + feed, feedGroups).apply();
        this.feedsGroups.put(feed, feedGroups);
    }

    private void deleteFeedGroup(String feed) {
        sharedPrefs.edit().remove(feedKeyPrefix + feed).apply();
        this.feedsGroups.remove(feed);
    }
}
