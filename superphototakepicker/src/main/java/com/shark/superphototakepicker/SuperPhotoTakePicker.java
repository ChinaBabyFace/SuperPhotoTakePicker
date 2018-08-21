package com.shark.superphototakepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Author:renyuxiang on 2018/8/21 11:25
 */
public class SuperPhotoTakePicker {
    public static final String CAMERA_PHOTO_FILE_NAME = "camera_photo.jpg";
    public static final String CROP_PHOTO_FILE_NAME = "crop_photo.jpg";

    public static final int PICK_PHOTO_FROM_CAMERA = 100;
    public static final int PICK_PHOTO_FROM_ALBUM = 200;
    public static final int CROP_PHOTO = 300;

    private int cropWidth;
    private int cropHeight;
    private int aspectX;
    private int aspectY;
    private boolean isCropPhoto;
    private String authority;
    private String cropTimestamp;
    private Activity activity;
    private Bitmap.CompressFormat compressFormat;
    private OnPhotoChangedListener onPhotoChangedListener;

    public SuperPhotoTakePicker(Activity activity) {
        this.activity = activity;
    }

    public void startAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, PICK_PHOTO_FROM_ALBUM);
    }

    public void startCamera() {
        File outputFile = new File(activity.getExternalCacheDir(), CAMERA_PHOTO_FILE_NAME);

        if (outputFile.exists()) outputFile.delete();
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getLocalFileUri(outputFile));
        activity.startActivityForResult(intent, PICK_PHOTO_FROM_CAMERA);
    }

    public Uri getLocalFileUri(File targetFile) {
        return Build.VERSION.SDK_INT >= 24
                ?
                FileProvider.getUriForFile(activity, authority, targetFile)
                :
                Uri.fromFile(targetFile);
    }

    public File getCropFile(String timestamp) {
        File cropFile = new File(Environment.getExternalStorageDirectory().getPath(), timestamp + CROP_PHOTO_FILE_NAME);
        return cropFile;
    }

    public void cropPhoto(File rawPhoto, String timestamp) {
        if (rawPhoto == null) return;
        File cropFile = getCropFile(timestamp);
        Uri rawUri = getLocalFileUri(rawPhoto);
        Uri cropUri = Uri.fromFile(cropFile);
        if (rawUri == null) {
            if (onPhotoChangedListener != null)
                onPhotoChangedListener.onError(new FileNotFoundException("Raw photo not found!"));
            return;
        }
        if (cropUri == null) {
            if (onPhotoChangedListener != null)
                onPhotoChangedListener.onError(new FileNotFoundException("Crop photo target file not found!"));
            return;
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", cropWidth);
        intent.putExtra("outputY", cropHeight);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("outputFormat", compressFormat.toString());
        intent.setDataAndType(rawUri, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivityForResult(intent, CROP_PHOTO);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case PICK_PHOTO_FROM_CAMERA:
                toCrop(new File(activity.getExternalCacheDir().getPath(), CAMERA_PHOTO_FILE_NAME));
                break;
            case PICK_PHOTO_FROM_ALBUM:
                if (data == null || data.getData() == null) return;
                toCrop(new File(XFileUtils.getPath(activity, data.getData(), activity.getPackageName())));
                break;
            case CROP_PHOTO:
                if (onPhotoChangedListener == null) return;
                File cropFile = getCropFile(cropTimestamp);
                if (!cropFile.exists() || cropFile.length() <= 0)
                    onPhotoChangedListener.onError(new FileNotFoundException("No photo found to crop!"));
                else
                    onPhotoChangedListener.onCropPhotoReceived(cropFile);
                break;

        }
    }

    private void toCrop(File rawPhoto) {
        cropTimestamp = "" + System.currentTimeMillis();
        if (rawPhoto == null || rawPhoto.length() <= 0) {
            if (onPhotoChangedListener == null) return;
            onPhotoChangedListener.onError(new FileNotFoundException("No photo to crop!"));
        } else {
            if (onPhotoChangedListener != null)
                onPhotoChangedListener.onRawPhotoReceived(rawPhoto);
            if (isCropPhoto)cropPhoto(rawPhoto, cropTimestamp);
        }
    }

    public void checkCropPhotoState() {
        File cropFile = getCropFile(cropTimestamp);
        if (!cropFile.exists() || cropFile.length() <= 0) return;
        onPhotoChangedListener.onCropPhotoReceived(cropFile);
    }

    public OnPhotoChangedListener getOnPhotoChangedListener() {
        return onPhotoChangedListener;
    }

    public void setOnPhotoChangedListener(OnPhotoChangedListener onPhotoChangedListener) {
        this.onPhotoChangedListener = onPhotoChangedListener;
    }

    public int getCropWidth() {
        return cropWidth;
    }

    public void setCropWidth(int cropWidth) {
        this.cropWidth = cropWidth;
    }

    public int getCropHeight() {
        return cropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.cropHeight = cropHeight;
    }

    public int getAspectX() {
        return aspectX;
    }

    public void setAspectX(int aspectX) {
        this.aspectX = aspectX;
    }

    public int getAspectY() {
        return aspectY;
    }

    public void setAspectY(int aspectY) {
        this.aspectY = aspectY;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Bitmap.CompressFormat getCompressFormat() {
        return compressFormat;
    }

    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public void setCropPhoto(boolean cropPhoto) {
        isCropPhoto = cropPhoto;
    }

    public static class Builder {
        private int cropWidth = 100;
        private int cropHeight = 100;
        private int aspectX = 1;
        private int aspectY = 1;
        private String authority = "";
        private Activity activity;
        private boolean isCropPhoto=true;
        private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
        private OnPhotoChangedListener onPhotoChangedListener;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setCropWidth(int cropWidth) {
            this.cropWidth = cropWidth;
            return this;
        }

        public Builder setCropHeight(int cropHeight) {
            this.cropHeight = cropHeight;
            return this;
        }

        public Builder setAspectX(int aspectX) {
            this.aspectX = aspectX;
            return this;
        }

        public Builder setAspectY(int aspectY) {
            this.aspectY = aspectY;
            return this;
        }

        public Builder setAuthority(String authority) {
            this.authority = authority;
            return this;
        }

        public Builder setCompressFormat(Bitmap.CompressFormat compressFormat) {
            this.compressFormat = compressFormat;
            return this;
        }

        public Builder setOnPhotoChangedListener(OnPhotoChangedListener onPhotoChangedListener) {
            this.onPhotoChangedListener = onPhotoChangedListener;
            return this;
        }

        public Builder setCropPhoto(boolean cropPhoto) {
            isCropPhoto = cropPhoto;
            return this;
        }

        public SuperPhotoTakePicker create() {
            if (!(activity instanceof Activity))
                throw new IllegalArgumentException("SuperPhotoTakePicker need Activity!");
            if (Build.VERSION.SDK_INT >= 24 && TextUtils.isEmpty(authority))
                throw new IllegalArgumentException("When android sdk>=24,need file provider and authority!");
            SuperPhotoTakePicker picker = new SuperPhotoTakePicker(activity);
            picker.setCropPhoto(isCropPhoto);
            picker.setAspectX(aspectX);
            picker.setAspectY(aspectY);
            picker.setCropWidth(cropWidth);
            picker.setCropHeight(cropHeight);
            picker.setAuthority(authority);
            picker.setCompressFormat(compressFormat);
            picker.setOnPhotoChangedListener(onPhotoChangedListener);
            return picker;
        }
    }
}
