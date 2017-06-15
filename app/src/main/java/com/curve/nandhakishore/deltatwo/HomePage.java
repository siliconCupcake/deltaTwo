package com.curve.nandhakishore.deltatwo;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.Manifest.permission;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import static android.support.v4.content.FileProvider.getUriForFile;


public class HomePage extends AppCompatActivity {

    RecyclerView list_images;
    RecyclerView.LayoutManager list_manager;
    imageAdapter list_adapter;
    Toolbar mToolbar;
    FloatingActionsMenu addMenu;
    databaseManage dbData = new databaseManage(this);
    int undo = 0, permCount = 0, fileno = 0;
    String today = null, filename = null;
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        dbData.open();
        listTools.init_cards();
        listTools.allCards = dbData.getData();
        listTools.coordinatorLayout = (CoordinatorLayout) findViewById(R.id.cLayout);

        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy");
        String new_date = df.format(Calendar.getInstance().getTime());
        SharedPreferences sPrefs = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        if (!sPrefs.getString("date", "default").equals(new_date))
            fileno = 0;
        else
            fileno = sPrefs.getInt("count", 0);

        today = df.format(Calendar.getInstance().getTime());
        filename = "IMG-" + today + "-DTC" + String.format("%04d", fileno) + ".jpg";

        list_images = (RecyclerView) findViewById(R.id.list);
        list_manager = new LinearLayoutManager(this);
        list_images.setLayoutManager(list_manager);

        list_adapter = new imageAdapter(this);
        list_images.setAdapter(list_adapter);
        list_images.setOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });
        list_adapter.notifyDataSetChanged();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.app_name));
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        FloatingActionButton addGallery = (FloatingActionButton) findViewById(R.id.add_gallery);
        FloatingActionButton addCamera = (FloatingActionButton) findViewById(R.id.add_camera);
        addMenu = (FloatingActionsMenu) findViewById(R.id.add_menu);
        addGallery.setSize(FloatingActionButton.SIZE_MINI);
        addCamera.setSize(FloatingActionButton.SIZE_MINI);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        final cardItem temp = listTools.allCards.get(viewHolder.getAdapterPosition());
                        final int pos = viewHolder.getAdapterPosition();
                        listTools.allCards.remove(pos);
                        list_adapter.notifyItemRemoved(pos);
                        Snackbar sbar = Snackbar.make(listTools.coordinatorLayout, "Image removed", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        undo = 1;
                                        listTools.allCards.add(pos, temp);
                                        list_adapter.notifyItemInserted(pos);
                                        undo = 1;
                                    }
                                })
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        if (undo != 1) {
                                            dbData.removeRow(temp);
                                            Log.e("db", "Removed " + temp.caption);
                                        }
                                        undo = 0;
                                        super.onDismissed(transientBottomBar, event);
                                    }
                                });
                        sbar.show();
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(list_images);

        list_adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(cardItem item) {
                try {
                    Intent cropIntent = new Intent("com.android.camera.action.CROP");

                    Uri picUri = item.image;
                    Log.e("Picture", picUri.toString());
                    index = listTools.allCards.indexOf(item);
                    cropIntent.setDataAndType(picUri, "image/*");
                    cropIntent.putExtra("crop", "true");
                    cropIntent.putExtra("aspectX", 1);
                    cropIntent.putExtra("aspectY", 1);
                    cropIntent.putExtra("outputX", 720);
                    cropIntent.putExtra("outputY", 1280);
                    cropIntent.putExtra("return-data", true);
                    startActivityForResult(cropIntent, 3);
                }
                catch (ActivityNotFoundException anfe) {
                    String errorMessage = "Whoops - your device doesn't support the crop action!";
                    Snackbar error = Snackbar.make(listTools.coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG);
                    error.show();
                }
            }
        });

        addGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] request = new String[]{permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE};
                reqPermission(request, 1);

                if (permCount == 2) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, 1);
                    addMenu.collapse();
                }
            }
        });

         addCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] request = new String[]{permission.CAMERA, permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE};
                reqPermission(request, 1);

                if (permCount == 3) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File imagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeltaTwoImages");
                    if (!imagePath.exists()) {
                        if (!imagePath.mkdir()) {
                            System.out.println("***Problem creating Image folder ");
                        }
                    }
                    File tempImg = new File(imagePath, "temp_image.png");
                    Uri photoUri = getUriForFile(getApplicationContext(), "com.curve.nandhakishore.provider", tempImg);
                    getApplicationContext().grantUriPermission("com.curve.nandhakishore.deltatwo", photoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(cameraIntent, 2);
                    addMenu.collapse();
                }
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(cardItem item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            Log.e("Photos", imageUri.toString());
            listTools.getFromGal(this, imageUri);
            list_adapter.notifyDataSetChanged();
        }
        else if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri imageUri;
            File imagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeltaTwoImages");
            File tempImg = new File(imagePath, "temp_image.png");

            File sample = new File(imagePath, filename);
            fileno++;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(tempImg.getAbsolutePath(), options);
            options.inSampleSize = listTools.calculateInSampleSize(options, 720, 1280);
            options.inJustDecodeBounds = false;
            Bitmap bmp = BitmapFactory.decodeFile(tempImg.getAbsolutePath(), options);

            try {
                OutputStream fOutputStream = new FileOutputStream(sample);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);
                fOutputStream.flush();
                fOutputStream.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
                return;
            }catch (IOException e) {
                e.printStackTrace();
                return;
            }
            imageUri = getImageContentUri(this, sample);
            Log.e("File", Uri.fromFile(sample).toString());
            Log.e("Content", imageUri.toString());
            if (!tempImg.delete()) {
                Log.e("logMarker", "Failed to delete " + tempImg);
            }
            listTools.getFromCam(this, imageUri);
            list_adapter.notifyDataSetChanged();
        }
        else if (requestCode == 3 && resultCode == RESULT_OK) {
            Uri imageUri;
            Bitmap cropped = null;
            if(data != null) {
                cropped = (Bitmap) data.getExtras().get("data");
            }
            File imagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeltaTwoImages");
            File sample = new File(imagePath, filename);
            fileno++;

            try {
                OutputStream fOutputStream = new FileOutputStream(sample);
                cropped.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);
                fOutputStream.flush();
                fOutputStream.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
                return;
            }catch (IOException e) {
                e.printStackTrace();
                return;
            }
            imageUri = getImageContentUri(this, sample);
            listTools.allCards.get(index).image = imageUri;
            list_adapter.notifyDataSetChanged();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (addMenu.isExpanded()) {
            addMenu.collapse();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        SharedPreferences sPrefs = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putInt("count", fileno);
        editor.putString("date", today);
        editor.apply();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        dbData.close();
        super.onDestroy();
    }

    public void reqPermission(String[] permissions, int permCode) {
        ArrayList<String> temp = new ArrayList<>();
        permCount = 0;
        for(int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("Permissions", permissions[i] + " not granted");
                temp.add(permissions[i]);
            }
            else {
                permCount++;
            }
        }
        String[] required = new String[temp.size()];
        required = temp.toArray(required);
        if(required.length != 0) {
            ActivityCompat.requestPermissions(this, required, permCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1 : {
                if (grantResults.length > 0) {
                    for(int i = 0; i < permissions.length; i++) {
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e("Permissions", permissions[i] + " granted");
                            permCount++;
                        }
                    }
                }
                return;
            }

            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private void hideViews() {
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) addMenu.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        addMenu.animate().translationY(addMenu.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    /**
     * Show toolbar and image button
     */
    private void showViews() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        addMenu.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }
}


