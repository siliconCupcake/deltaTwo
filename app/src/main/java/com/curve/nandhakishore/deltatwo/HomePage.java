package com.curve.nandhakishore.deltatwo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.jar.Manifest;

import android.Manifest.permission;

import static android.support.v4.content.FileProvider.getUriForFile;


public class HomePage extends AppCompatActivity {

    RecyclerView list_images;
    RecyclerView.LayoutManager list_manager;
    imageAdapter list_adapter;
    databaseManage dbData = new databaseManage(this);
    int undo = 0, permCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        dbData.open();
        listTools.init_cards();
        listTools.allCards = dbData.getData();
        listTools.coordinatorLayout = (CoordinatorLayout) findViewById(R.id.cLayout);

        list_images = (RecyclerView) findViewById(R.id.list);
        list_manager = new LinearLayoutManager(this);
        list_images.setLayoutManager(list_manager);

        list_adapter = new imageAdapter(this);
        list_images.setAdapter(list_adapter);
        list_adapter.notifyDataSetChanged();

        FloatingActionButton addGallery = (FloatingActionButton) findViewById(R.id.add_gallery);
        FloatingActionButton addCamera = (FloatingActionButton) findViewById(R.id.add_camera);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

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

        addGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] request = new String[]{permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE};
                reqPermission(request, 1);

                if (permCount == 2) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, 1);
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
                    File imagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeltaTwoCamera");
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
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            listTools.getFromGal(this, imageUri);
            list_adapter.notifyDataSetChanged();
        }
        else if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri imageUri;
            File imagePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeltaTwoCamera");
            File tempImg = new File(imagePath, "temp_image.png");
            File sample = new File(imagePath, "temp_sample.jpg");

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
            try {
                imageUri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(), sample.getAbsolutePath(), null, null));
                if (!tempImg.delete()) {
                    Log.e("logMarker", "Failed to delete " + tempImg);
                }
                listTools.getFromCam(this, imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("Error", "No file");
            }
            list_adapter.notifyDataSetChanged();
        }

        super.onActivityResult(requestCode, resultCode, data);
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
}


