package com.kosmo.homespital.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kosmo.homespital.model.ItemNews;
import com.kosmo.homespital.viewholder.NewsHolder;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsHolder>{

    private List<ItemNews> mNews = new ArrayList<>();

    @NonNull
    @Override
    public NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return NewsHolder.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsHolder holder, int position) {
        holder.bind(mNews.get(position));
    }

    @Override
    public int getItemCount() {
        return mNews.size();
    }

    public void setNews(List<ItemNews> news) {
        if (news == null) {
            return;
        }

        mNews = news;
    }
}
