package com.example.rahhal;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.ImageViewHolder> {
    private List<String> imagePaths;
    private OnImageClickListener onImageClickListener;
    private DBHelper db;

    public RowAdapter(List<String> paths, OnImageClickListener onImageClickListener, Context c) {
        this.imagePaths = paths;
        this.onImageClickListener = onImageClickListener;
        db = new DBHelper(c);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String path = imagePaths.get(position);
        String[] items = db.getRowByPath(path);
        String desc = items[2];
        desc = cutOffText(desc, 120);
        holder.titleTextView.setText(items[1]);
        holder.snippetTextView.setText(desc);
        Bitmap b = CameraFragment.resizeImage(path, 1000, 100);
        if (path != "") {
            holder.imageView.setVisibility(View.VISIBLE);

            // Load the image into the ImageView using an image loading library like Glide or Picasso
            // Example using Glide:
            Glide.with(holder.imageView.getContext())
                    .load(b)
                    .into(holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.imageView.setPadding(0, 0, 0, 0);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onImageClickListener != null) {
                    onImageClickListener.onImageClick(path);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView titleTextView;
        public TextView snippetTextView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            snippetTextView = itemView.findViewById(R.id.snippetTextView);
        }
    }

    private String cutOffText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        } else {
            return text;
        }
    }
}