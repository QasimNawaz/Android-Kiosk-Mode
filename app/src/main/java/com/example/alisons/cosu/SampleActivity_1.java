package com.example.alisons.cosu;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.afollestad.materialcamera.MaterialCamera;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SampleActivity_1 extends AppCompatActivity implements View.OnClickListener {

    private static final int CAMERA_RQ = 6969;
    private static final int PERMISSION_RQ = 84;

    private static final int REQUEST_CODE_ENABLE_DEVICE_ADMIN = 1;
    int TAKE_PHOTO_CODE = 0;
    public static int count = 0;
    String dir;

    private Button openLib, openDir, activate, deActive;
    private static final int REQUEST_CODE_IMAGES = 732;
    private ArrayList<String> imagesResults;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName mAdminComponentName;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_1);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = com.example.alisons.cosu.DeviceAdminReceiver.getComponentName(this);
        // Here, we are making a folder named picFolder to store
        // pics taken by the camera using this application.
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
        File newdir = new File(dir);
        newdir.mkdirs();


        openLib = findViewById(R.id.openLibraryCamera);
        openDir = findViewById(R.id.openDirectCamera);
        activate = findViewById(R.id.activateAdmin);
        deActive = findViewById(R.id.deActivateAdmin);
        imagesResults = new ArrayList<>();
        openLib.setOnClickListener(this);
        openDir.setOnClickListener(this);
        activate.setOnClickListener(this);
        deActive.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.openLibraryCamera:
                selectImages();
                break;
            case R.id.openDirectCamera:
                File saveDir = null;
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Only use external storage directory if permission is granted, otherwise cache directory is used by default
                    saveDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MCMS_Camera");
                }
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "MCMS");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Site Images");
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(MediaStore.Images.Media.CONTENT_TYPE, "image/jpeg");
                assert saveDir != null;
                values.put(MediaStore.Images.ImageColumns.BUCKET_ID, saveDir.toString().toLowerCase(Locale.US).hashCode());
                values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, saveDir.getName().toLowerCase(Locale.US));
                values.put("_data", saveDir.getAbsolutePath());
                ContentResolver cr = getContentResolver();
                cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                saveDir.mkdirs();
                MaterialCamera materialCamera =
                        new MaterialCamera(this)
                                .saveDir(saveDir)
                                .showPortraitWarning(true)
                                .allowRetry(true)
                                .defaultToFrontFacing(false)
                                .allowRetry(true)
                                .autoSubmit(false)
                                .labelConfirm(R.string.mcam_use_video);
                materialCamera
                        .stillShot() // launches the Camera in stillshot mode
                        .labelConfirm(R.string.mcam_use_stillshot);
                materialCamera.start(CAMERA_RQ);
                break;
            case R.id.activateAdmin:
                if (!devicePolicyManager.isAdminActive(mAdminComponentName)) {
                    Intent activateDeviceAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, DeviceAdminReceiver.getComponentName(this));
                    activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "After activating admin, you will be able to block application uninstallation.");
                    startActivityForResult(activateDeviceAdmin, REQUEST_CODE_ENABLE_DEVICE_ADMIN);
                }
                break;
            case R.id.deActivateAdmin:
                devicePolicyManager.removeActiveAdmin(mAdminComponentName);
                break;
        }
    }

    private void selectImages() {
        // start multiple photos selector
        Intent intent = new Intent(SampleActivity_1.this, ImagesSelectorActivity.class);
        // max number of images to be selected
        intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 5);
        // min size of image which will be shown; to filter tiny images (mainly icons)
        intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000);
        // show camera or not
        intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true);
        // pass current selected images as the initial value
        intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, imagesResults);
        // start the selector
        startActivityForResult(intent, REQUEST_CODE_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ) {
            if (resultCode == RESULT_OK) {
                imagesResults.add(data.getDataString());
                Log.d("Images_Array", "" + imagesResults.size());

                Bundle extras = data.getExtras();
                bitmap = extras.getParcelable("data");
//                String path = saveImage(bitmap);
                File storedImagePath = generateImagePath("mcms", "jpeg");
                if (!compressAndSaveImage(storedImagePath, bitmap)) {
                    Log.d("Save", "true");
                }
                Uri url = addImageToGallery(this.getContentResolver(), "jpeg", storedImagePath);

//                Toast.makeText(
//                        this,
//                        String.format("Saved to: %s, size: %s", file.getAbsolutePath(), fileSize(file)),
//                        Toast.LENGTH_LONG)
//                        .show();
            } else if (data != null) {
                Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                if (e != null) {
                    e.printStackTrace();
                    Log.d("Error", "" + e.getMessage());
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private String saveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Trippr/ProfileImages");

        if (!myDir.exists())
            myDir.mkdirs();

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image_" + n + ".jpg";
        File file = new File(myDir, fname);

        if (file.exists())
            file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root + "/Trippr/ProfileImages/" + fname;
    }

    private static File getImagesDirectory() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "MCMS_Images");//Environment.getExternalStorageDirectory()
        if (!file.mkdirs() && !file.isDirectory()) {
            Log.e("mkdir", "Directory not created");
        }
        return file;
    }

    public static File generateImagePath(String title, String imgType) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        return new File(getImagesDirectory(), title + "_" + sdf.format(new Date()) + "." + imgType);
    }

    public boolean compressAndSaveImage(File file, Bitmap bitmap) {
        boolean result = false;

        try {
            FileOutputStream out = new FileOutputStream(file);
            if (result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)){
                Log.w("image manager", "Compression success");
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

//
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            if (result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {
//                Log.w("image manager", "Compression success");
//            }
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return result;
    }

    public Uri addImageToGallery(ContentResolver cr, String imgType, File filepath) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "mcms");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "mcms");
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + imgType);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, filepath.toString());

        return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
