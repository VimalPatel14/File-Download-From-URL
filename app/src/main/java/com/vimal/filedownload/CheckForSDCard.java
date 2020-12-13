package com.vimal.filedownload;

import android.os.Environment;

public class CheckForSDCard {

    public boolean isSDCardPresent() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}