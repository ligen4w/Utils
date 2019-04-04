package com.lg.utils.constants;

import android.os.Environment;

import java.io.File;

public class Constant {
    public static final String SDCARD_BASE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator
            + "Utils";

}
