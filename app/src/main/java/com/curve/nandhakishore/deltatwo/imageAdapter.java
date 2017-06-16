package com.curve.nandhakishore.deltatwo;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.display.VirtualDisplay;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class imageAdapter extends RecyclerView.Adapter<imageAdapter.cardHolder> {

    static Context myContext;

    public imageAdapter(Context con) {
        myContext = con;
    }

    private HomePage.OnItemClickListener onItemClickListener;
    public HomePage.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(HomePage.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public cardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_card, parent, false);
        return new cardHolder(inflatedView);
    }


    @Override
    public void onBindViewHolder(final cardHolder holder, final int position) {

        holder.pBar.setVisibility(View.VISIBLE);
        holder.cError.setVisibility(View.GONE);
        Glide
                .with(myContext)
                .load(listTools.allCards.get(position).image)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri uri, Target<GlideDrawable> target, boolean b) {
                        holder.cError.setVisibility(View.VISIBLE);
                        holder.pBar.setVisibility(View.GONE);
                        Log.e("Glide error", e.toString());
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable glideDrawable, Uri uri, Target<GlideDrawable> target, boolean b, boolean b1) {
                        holder.pBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .crossFade()
                .into(holder.cImage);

        holder.cCaption.setText(listTools.allCards.get(position).caption);
        holder.cPos.setText(String.valueOf(holder.getAdapterPosition() + 1));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(listTools.allCards.get(position));
            }
        };
        holder.cImage.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return listTools.allCards.size();
    }

    public static class cardHolder extends RecyclerView.ViewHolder {

        private ImageView cImage;
        private TextView cCaption;
        private TextView cPos;
        private ProgressBar pBar;
        private ImageView cError;

        public cardHolder(View v) {
            super(v);
            this.cImage = (ImageView) v.findViewById(R.id.image);
            this.cCaption = (TextView) v.findViewById(R.id.caption);
            this.cPos = (TextView) v.findViewById(R.id.pos);
            this.pBar = (ProgressBar) v.findViewById(R.id.progressBar);
            this.cError = (ImageView) v.findViewById(R.id.error);
        }
    }

}

