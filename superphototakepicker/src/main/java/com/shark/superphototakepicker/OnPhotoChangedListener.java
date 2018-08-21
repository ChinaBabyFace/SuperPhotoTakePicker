package com.shark.superphototakepicker;

import java.io.File;

/**
 * Author:renyuxiang on 2018/8/21 11:28
 */
public interface OnPhotoChangedListener {

    void onRawPhotoReceived(File file);

    void onCropPhotoReceived(File file);

    void onError(Exception e);
}
