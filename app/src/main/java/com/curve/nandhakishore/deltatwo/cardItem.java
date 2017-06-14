package com.curve.nandhakishore.deltatwo;

import android.net.Uri;

public class cardItem {

    int place;
    String caption;
    Uri image;

    public cardItem(Uri bmp, String cap, int p) {
        image = bmp;
        caption = cap;
        place = p;
    }

}

