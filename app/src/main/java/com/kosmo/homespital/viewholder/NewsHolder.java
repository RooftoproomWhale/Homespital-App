package com.kosmo.homespital.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kosmo.homespital.R;
import com.kosmo.homespital.model.ItemNews;

public class NewsHolder extends RecyclerView.ViewHolder{

    private TextView mTitleView;

    public static NewsHolder newInstance(ViewGroup container) {
        View root = LayoutInflater.from(container.getContext()).inflate(R.layout.corona_news_layout,
                container, false);

        return new NewsHolder(root);
    }


    public NewsHolder(@NonNull View itemView) {
        super(itemView);

        mTitleView =  itemView.findViewById(R.id.news_title);
    }

    public void bind(ItemNews news) {
        mTitleView.setText(news.getTitle());
    }

}
