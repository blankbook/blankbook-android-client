package com.example.jacob.blankbookandroidclient.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.api.models.Post;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupSelectionRecyclerViewAdapter extends RecyclerView.Adapter<GroupSelectionRecyclerViewAdapter.ViewHolder> {

    private List<String> groups;
    private Set<String> selectedGroups = new HashSet<>();

    public GroupSelectionRecyclerViewAdapter(List<String> groups) {
        this.groups = groups;
    }

    public Set<String> getSelectedGroups() {
        return selectedGroups;
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_selection_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setGroup(groups.get(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.group_name)
        TextView groupName;
        @BindView(R.id.select_group)
        CheckBox selectGroup;

        private View view;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
        }

        void setGroup(final String group) {
            groupName.setText(group);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectGroup.isChecked()) {
                        for (Iterator<String> it = selectedGroups.iterator(); it.hasNext();) {
                            String next = it.next();
                            if (group.equals(next)) {
                                it.remove();
                                break;
                            }
                        }
                    } else {
                        selectedGroups.add(group);
                    }
                    selectGroup.toggle();
                }
            });
        }
    }
}