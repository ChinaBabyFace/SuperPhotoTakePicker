package com.shark.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.shark.demo.databinding.ActivityMainBinding;
import com.shark.superphototakepicker.OnPhotoChangedListener;
import com.shark.superphototakepicker.SuperPhotoTakePicker;
import com.shark.utils.library.core.SLog;

import java.io.File;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements OnPhotoChangedListener {
    private ActivityMainBinding binding;
    private SuperPhotoTakePicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        picker = new SuperPhotoTakePicker.Builder(this)
                .setAspectX(1)//设置裁剪后图片的横纵比例
                .setAspectY(1)//设置裁剪后图片的横纵比例
                .setCropPhoto(false)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)//裁剪后图片的存储格式
                .setCropWidth(200)//裁剪后图片的大小
                .setCropHeight(200)//裁剪后图片的大小
                .setAuthority(getPackageName())//本APP的FileProvider的Authority
                .setOnPhotoChangedListener(this)//
                .create();
        binding.cmeraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.rawPathTextView.setText("");
                binding.cropPathTextView.setText("");
                binding.error.setText("");
                MainActivityPermissionsDispatcher.startCameraWithPermissionCheck(MainActivity.this);
            }
        });
        binding.albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.rawPathTextView.setText("");
                binding.cropPathTextView.setText("");
                binding.error.setText("");
                MainActivityPermissionsDispatcher.startAlbumWithPermissionCheck(MainActivity.this);
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void startAlbum() {
        picker.startAlbum();
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    public void startCamera() {
        picker.startCamera();
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        picker.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onRawPhotoReceived(File file) {
        //裁剪前的原始图片文件
        SLog.e(this, "onRawPhotoReceived:" + file.getAbsolutePath());
        binding.rawPathTextView.setText(file.getAbsolutePath());
    }

    @Override
    public void onCropPhotoReceived(File file) {
        //裁剪后的图片文件
        //如果在OnResume中调用 picker.checkCropPhotoState()，那么请在这里上传图片后删除，否则不需要删除
        SLog.e(this, "onCropPhotoReceived:" + file.getAbsolutePath());
        binding.cropPathTextView.setText(file.getAbsolutePath());
        Glide.with(this).load(file).into(binding.imageView);
    }

    @Override
    public void onError(Exception e) {
        //错误e.getMessage()
        SLog.e(this, "onError:" + e.getMessage());
        binding.rawPathTextView.setText("");
        binding.cropPathTextView.setText("");
        binding.error.setText("Error:"+e.getMessage());

    }

    @Override
    protected void onResume() {
        super.onResume();
        picker.checkCropPhotoState();
    }
}
