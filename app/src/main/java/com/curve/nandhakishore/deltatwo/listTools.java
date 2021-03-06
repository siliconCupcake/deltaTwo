package com.curve.nandhakishore.deltatwo;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;

public class listTools {

    public static ArrayList<cardItem> allCards;
    public static CoordinatorLayout coordinatorLayout;
    public static int ID;

    public static void init_cards() {
        allCards = new ArrayList<>();
    }

    public static void getFromGal(Context context, Uri imageUri) {
        openAlert(context, imageUri);
    }

    public static void getFromCam (Context context, Uri imageUri) {
        openAlert(context, imageUri);
    }

    private static void openAlert(final Context context, final Uri picture) {

        final databaseManage dbData = new databaseManage(context);
        dbData.open();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        final View inflator = LayoutInflater.from(context).inflate(R.layout.caption_dialog, null);
        alertDialogBuilder.setView(inflator);
        alertDialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                EditText cString = (EditText) inflator.findViewById(R.id.dialog_et) ;
                String text = cString.getText().toString();
                if (!TextUtils.isEmpty(text)){
                    cardItem new_item = new cardItem(picture, text, listTools.ID);
                    listTools.allCards.add(new_item);
                    dbData.createEntry(new_item);
                    Log.e("db", "Added " + new_item.caption + " with id " + new_item.place + " at " + new_item.image.toString());
                    listTools.ID++;
                    dbData.close();
                }
                else {
                    Toast.makeText(context, "Enter some caption", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
