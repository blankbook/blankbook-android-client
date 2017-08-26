package com.example.jacob.blankbookandroidclient.adapters;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.R;

import java.util.List;

public class MainDrawerRecyclerViewAdapter extends RecyclerView.Adapter<MainDrawerRecyclerViewAdapter.ViewHolder> {
    private final int NO_VIEW_HOLDER = -1;
    private final int MAIN_FEED_VIEW_HOLDER = 0;
    private final int FEEDS_HEADER_VIEW_HOLDER = 1;
    private final int FEED_VIEW_HOLDER = 2;
    private final int NEW_FEED_VIEW_HOLDER = 3;
    private final int GROUPS_HEADER_VIEW_HOLDER = 4;
    private final int GROUP_VIEW_HOLDER = 5;
    private final int NEW_GROUP_VIEW_HOLDER = 6;

    private final OnSelect onSelect;

    private List<String> feeds;
    private List<String> groups;
    private boolean mainFeedHighlighted;
    private String highlightedFeed;
    private boolean newFeedHighlighted;
    private String highlightedGroup;
    private boolean newGroupHighlighted;

    public MainDrawerRecyclerViewAdapter(List<String> feeds, List<String> groups, OnSelect onSelect) {
        this.onSelect = onSelect;
        this.feeds = feeds;
        this.groups = groups;
        clearHighlight();
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
        this.notifyDataSetChanged();
    }

    public void setFeeds(List<String> feeds) {
        this.feeds = feeds;
        this.notifyDataSetChanged();
    }

    public void highlightMainFeed() {
        clearHighlight();
        this.mainFeedHighlighted = true;
        updateHighlight();
    }

    public void highlightFeed(String feed) {
        clearHighlight();
        this.highlightedFeed = feed;
        updateHighlight();
    }

    public void highlightNewFeed() {
        clearHighlight();
        this.newFeedHighlighted = true;
        updateHighlight();
    }

    public void highlightGroup(String group) {
        clearHighlight();
        this.highlightedGroup = group;
        updateHighlight();
    }

    public void highlightNewGroup() {
        clearHighlight();
        this.newGroupHighlighted = true;
        updateHighlight();
    }

    @Override
    public int getItemCount() {
        // main feed + groups header + new group + feeds header + new feed
        final int numExtraEntries = 5;
        return groups.size() + feeds.size() + numExtraEntries;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return MAIN_FEED_VIEW_HOLDER;
        }
        position -= 1; // pop off 'main feed' from list
        if (position == 0) {
            return FEEDS_HEADER_VIEW_HOLDER;
        }
        position -= 1; // pop off 'feeds header' from list
        position -= feeds.size(); // pop off all feed list items from the list
        if (position < 0) {
            return FEED_VIEW_HOLDER;
        }
        if (position == 0) {
            return NEW_FEED_VIEW_HOLDER;
        }
        position -= 1; // pop off 'new feed' from list
        if (position == 0) {
            return GROUPS_HEADER_VIEW_HOLDER;
        }
        position -= 1; // pop off 'groups header' from list
        position -= groups.size(); // pop off all group list items from list
        if (position < 0) {
            return GROUP_VIEW_HOLDER;
        }
        if (position == 0) {
            return NEW_GROUP_VIEW_HOLDER;
        }
        return NO_VIEW_HOLDER;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case MAIN_FEED_VIEW_HOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.main_drawer_list_header, parent, false);
                return new MainFeedViewHolder(view);
            case FEEDS_HEADER_VIEW_HOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.main_drawer_list_header, parent, false);
                return new FeedsHeaderViewHolder(view);
            case NEW_FEED_VIEW_HOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.main_drawer_list_item, parent, false);
                return new NewFeedViewHolder(view);
            case FEED_VIEW_HOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.main_drawer_list_item, parent, false);
                return new FeedViewHolder(view);
            case GROUPS_HEADER_VIEW_HOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.main_drawer_list_header, parent, false);
                return new GroupsHeaderViewHolder(view);
            case GROUP_VIEW_HOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.main_drawer_list_item, parent, false);
                return new GroupViewHolder(view);
            case NEW_GROUP_VIEW_HOLDER:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.main_drawer_list_item, parent, false);
                return new NewGroupViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.viewType) {
            case FEED_VIEW_HOLDER: {
                // main feed + groups header
                final int offset = 2;
                final int i = position - offset;
                ((FeedViewHolder) holder).setFeed(this.feeds.get(i));
                break;
            }
            case GROUP_VIEW_HOLDER: {
                // main feed + groups header + new group + feed header + feeds
                final int offset = 4 + this.feeds.size();
                final int i = position - offset;
                ((GroupViewHolder) holder).setGroup(this.groups.get(i));
            }
        }
        holder.updateHighlight();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final int viewType;
        private ViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;
        }

        void updateHighlight() {}
    }

    private class MainFeedViewHolder extends ViewHolder {
        private final View view;

        private MainFeedViewHolder(View view) {
            super(view, MAIN_FEED_VIEW_HOLDER);
            this.view = view;
            ((TextView) view.findViewById(R.id.text)).setText(R.string.main_feed);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSelect.onMainFeedSelect();
                    highlightMainFeed();
                }
            });
        }

        @Override
        void updateHighlight() {
            if (mainFeedHighlighted) {
                highlightMenuItem(view);
            } else {
                removeMenuItemBackground(view);
            }
        }
    }

    private class FeedsHeaderViewHolder extends ViewHolder {
        private FeedsHeaderViewHolder(View view) {
            super(view, FEEDS_HEADER_VIEW_HOLDER);
            ((TextView) view.findViewById(R.id.text)).setText(R.string.feeds);
        }
    }

    private class FeedViewHolder extends ViewHolder {
        private final TextView header;
        private final View view;
        private String feed = "";

        private FeedViewHolder(View view) {
            super(view, FEED_VIEW_HOLDER);
            this.header = view.findViewById(R.id.text);
            this.view = view;
            removeMenuItemBackground(view);
        }

        private void setFeed(final String feed) {
            this.feed = feed;
            header.setText(feed);
            this.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSelect.onFeedSelect(feed);
                    highlightFeed(feed);
                }
            });
        }

        @Override
        void updateHighlight() {
            if (feed.equals(highlightedFeed)) {
                highlightMenuItem(view);
            } else {
                removeMenuItemBackground(view);
            }
        }
    }

    private class NewFeedViewHolder extends ViewHolder {
        private final View view;

        private NewFeedViewHolder(View view) {
            super(view, NEW_FEED_VIEW_HOLDER);
            this.view = view;
            ((TextView) view.findViewById(R.id.text)).setText(R.string.add_feed);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSelect.onNewFeedSelect();
                    highlightNewFeed();
                }
            });
        }

        @Override
        void updateHighlight() {
            if (newFeedHighlighted) {
                highlightMenuItem(view);
            } else {
                removeMenuItemBackground(view);
            }
        }
    }

    private class GroupsHeaderViewHolder extends ViewHolder {
        private GroupsHeaderViewHolder(View view) {
            super(view, GROUPS_HEADER_VIEW_HOLDER);
            ((TextView) view.findViewById(R.id.text)).setText(R.string.groups);
        }
    }

    private class GroupViewHolder extends ViewHolder {
        private final TextView header;
        private final View view;
        private String group = "";

        private GroupViewHolder(View view) {
            super(view, GROUP_VIEW_HOLDER);
            this.header = view.findViewById(R.id.text);
            this.view = view;
            removeMenuItemBackground(view);
        }

        private void setGroup(final String group) {
            this.group = group;
            header.setText(group);
            this.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSelect.onGroupSelect(group);
                    highlightGroup(group);
                }
            });
        }

        @Override
        void updateHighlight() {
            if (group.equals(highlightedGroup)) {
                highlightMenuItem(view);
            } else {
                removeMenuItemBackground(view);
            }
        }
    }

    private class NewGroupViewHolder extends ViewHolder {
        private final View view;

        private NewGroupViewHolder(View view) {
            super(view, NEW_GROUP_VIEW_HOLDER);
            this.view = view;
            ((TextView) view.findViewById(R.id.text)).setText(R.string.add_group);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSelect.onNewGroupSelect();
                    highlightNewGroup();
                }
            });
        }

        @Override
        void updateHighlight() {
            if (newGroupHighlighted) {
                highlightMenuItem(view);
            } else {
                removeMenuItemBackground(view);
            }
        }
    }

    private void highlightMenuItem(View view) {
        view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
    }

    private void removeMenuItemBackground(View view) {
        view.setBackgroundColor(Color.TRANSPARENT);
    }

    private void clearHighlight() {
        if (mainFeedHighlighted) {
            mainFeedHighlighted = false;
            updateMainFeedHighlight();
        } else if (highlightedFeed != null) {
            int i = findFeedIndex(highlightedFeed);
            highlightedFeed = null;
            updateFeedHighlight(i);
        } else if (newFeedHighlighted) {
            newFeedHighlighted = false;
            updateNewFeedHighlight();
        } else if (highlightedGroup != null) {
            int i = findGroupIndex(highlightedGroup);
            highlightedGroup = null;
            updateGroupHighlight(i);
        } else if (newGroupHighlighted) {
            newGroupHighlighted = false;
            updateNewGroupHighlight();
        }
    }

    private void updateHighlight() {
        if (mainFeedHighlighted) {
            updateMainFeedHighlight();
        } else if (highlightedFeed != null) {
            updateFeedHighlight(findFeedIndex(highlightedFeed));
        } else if (newFeedHighlighted) {
            updateNewFeedHighlight();
        } else if (highlightedGroup != null) {
            updateGroupHighlight(findGroupIndex(highlightedGroup));
        } else if (newGroupHighlighted) {
            updateNewGroupHighlight();
        }
    }

    private int findFeedIndex(String feed) {
        for (int i = 0; i < feeds.size(); ++i) {
            if (feed.equals(feeds.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findGroupIndex(String group) {
        for (int i = 0; i < groups.size(); ++i) {
            if (group.equals(groups.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void updateMainFeedHighlight() {
        notifyItemChanged(0);
    }

    private void updateFeedHighlight(int feedIndex) {
        notifyItemChanged(2 + feedIndex);
    }

    private void updateNewFeedHighlight() {
        notifyItemChanged(2 + feeds.size());
    }

    private void updateGroupHighlight(int groupIndex) {
        notifyItemChanged(4 + feeds.size() + groupIndex);
    }

    private void updateNewGroupHighlight() {
        notifyItemChanged(4 + feeds.size() + groups.size());
    }

    public interface OnSelect {
        void onMainFeedSelect();

        void onGroupSelect(String name);

        void onFeedSelect(String name);

        void onNewGroupSelect();

        void onNewFeedSelect();
    }
}
