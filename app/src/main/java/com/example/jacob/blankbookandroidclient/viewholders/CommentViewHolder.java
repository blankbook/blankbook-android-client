package com.example.jacob.blankbookandroidclient.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.jacob.blankbookandroidclient.R;
import com.example.jacob.blankbookandroidclient.adapters.CommentsRecyclerViewAdapter;
import com.example.jacob.blankbookandroidclient.api.models.Post;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.group_name)
    TextView groupName;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.score)
    TextView score;

    private View view;
    private CommentsRecyclerViewAdapter.OnClickListener clickListener;
    private boolean showGroupName;

    public CommentViewHolder(View view, boolean showGroupName, CommentsRecyclerViewAdapter.OnClickListener clickListener) {
        super(view);
        ButterKnife.bind(this, view);
        this.view = view;
        this.clickListener = clickListener;
        this.showGroupName = showGroupName;
    }

    public void setPost(final Post post) {
        groupName.setText(post.GroupName);
        title.setText(post.Title);
        content.setText(post.Content);
        score.setText(String.valueOf(post.Score));
        title.setMaxLines(2);
        content.setMaxLines(3);

        if (showGroupName) {
            groupName.setVisibility(View.VISIBLE);
        } else {
            groupName.setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onClick(post, view);
            }
        });
    }
}
