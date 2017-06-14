package com.curve.nandhakishore.deltatwo;

import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class imageAdapter extends RecyclerView.Adapter<imageAdapter.cardHolder> {

    Context myContext;

    public imageAdapter(Context con) {
        myContext = con;
    }

    @Override
    public cardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_card, parent, false);
        return new cardHolder(inflatedView);
    }


    @Override
    public void onBindViewHolder(final cardHolder holder, int position) {

        holder.pBar.setVisibility(View.VISIBLE);
        Glide
                .with(myContext)
                .load(listTools.allCards.get(position).image)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri uri, Target<GlideDrawable> target, boolean b) {
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
    }

    @Override
    public int getItemCount() {
        return listTools.allCards.size();
    }

    public static class cardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView cImage;
        private TextView cCaption;
        private TextView cPos;
        private ProgressBar pBar;

        public cardHolder(View v) {
            super(v);
            this.cImage = (ImageView) v.findViewById(R.id.image);
            this.cCaption = (TextView) v.findViewById(R.id.caption);
            this.cPos = (TextView) v.findViewById(R.id.pos);
            this.pBar = (ProgressBar) v.findViewById(R.id.progressBar);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

    private class ImageLoadedCallback implements Callback {
        ProgressBar progressBar;

        public  ImageLoadedCallback(ProgressBar progBar){
            progressBar = progBar;
        }

        @Override
        public void onSuccess() {
            if (progressBar != null){
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onError() {

        }
    }
}

