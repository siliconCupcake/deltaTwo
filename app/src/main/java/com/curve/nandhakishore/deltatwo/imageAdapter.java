package com.curve.nandhakishore.deltatwo;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    public void onBindViewHolder(cardHolder holder, int position) {
        Picasso.with(myContext).load(listTools.allCards.get(position).image).into(holder.cImage);
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

        public cardHolder(View v) {
            super(v);
            this.cImage = (ImageView) v.findViewById(R.id.image);
            this.cCaption = (TextView) v.findViewById(R.id.caption);
            this.cPos = (TextView) v.findViewById(R.id.pos);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }
}

