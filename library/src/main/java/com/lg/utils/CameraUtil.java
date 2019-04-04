package com.lg.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.lg.utils.constants.Constant;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;

public class CameraUtil {
    private static final int REQUEST_CODE_TAKE_PHOTO = 0;
    private static final int REQUEST_CODE_LOCAL_ALBUM = 1;
    private static final int REQUEST_CODE_CROP_PHOTO = 2;

    private Activity activity;
    private static File photoDir;//图片存储目录

    private int compressLimit = 3;//图片需要压缩的阈值
    private int aspectX = -1;//裁剪图片X方向上的比例
    private int aspectY = -1;//裁剪图片Y方向上的比例
    private int outputX = -1;//裁剪图片的宽
    private int outputY = -1;//裁剪图片的高

    private boolean needCrop;//图片是否需要裁剪
    private boolean isLoadPhoto = false;//图片是否已经加载
    private ImageView imageView;
    private File saveFile;//保存图片的文件
    private Uri photoUri;//最终图片的Uri
    private Bitmap compressBitmap;


    public CameraUtil(Activity activity) {
        this.activity = activity;
    }

    public static CameraUtil getInstance(@NonNull Activity activity){
        photoDir = new File(Constant.SDCARD_BASE_PATH,"CameraUtil");
        photoDir.mkdirs();
        return new CameraUtil(activity);
    }

    /**
     * 拍照
     * @param photoName 设置拍照后的图片名称，可根据名称找到相应图片文件，处理文件上传等
     * @param needCrop 是否需要裁剪
     * @param imageView 加载图片的ImageView
     */
    public void takePhoto(@NonNull String photoName, boolean needCrop, @NonNull ImageView imageView){
        this.needCrop = needCrop;
        this.imageView = imageView;
        if(TextUtils.isEmpty(photoName)){
            throw new IllegalArgumentException("图片名称不能为空！");
        }else{
            saveFile = new File(photoDir,photoName + ".jpg");
        }
        photoUri = file2Uri(saveFile);
        openCamera(photoUri);
    }

    /**
     * 从相册中选择图片
     * @param photoName 设置选中图片的名称，可根据名称找到相应图片文件，处理文件上传等
     * @param needCrop 是否需要裁剪
     * @param imageView 显示图片的控件
     */
    public void pickFromAlbum(@NonNull String photoName, boolean needCrop, @NonNull ImageView imageView) {
        this.needCrop = needCrop;
        this.imageView = imageView;
        if(TextUtils.isEmpty(photoName)){
            throw new IllegalArgumentException("图片名称不能为空！");
        }else{
            saveFile = new File(photoDir,photoName + ".jpg");
        }
        photoUri = file2Uri(saveFile);
        openAlbum();
    }

    /**
     * 打开系统相机
     * 注：拍照的图片一般很大，使用Bitmap容易OOM，
     * 所以使用Uri，将return-data设为false
     * @param photoUri 拍照后保存的图片文件对应Uri
     */
    private void openCamera(Uri photoUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        activity.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, REQUEST_CODE_LOCAL_ALBUM);
    }

    /**
     * 剪裁图片,裁剪后的图片将替换原图片
     * @param srcImgUri 原图片文件Uri
     * @param cropImgUri 裁剪后的图片文件Uri
     */
    public void cropPhoto(Uri srcImgUri, Uri cropImgUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(srcImgUri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection",true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                activity.grantUriPermission(packageName, cropImgUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImgUri);
        activity.startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
    }

    /**
     * 剪裁图片,裁剪后的图片将替换原图片，设置裁剪后的图片宽高比例和大小
     * 注：aspectX、aspectY控制裁剪宽高比例，outputX、outputY控制图片输出大小
     * @param srcImgUri 原图片文件Uri
     * @param cropImgUri 裁剪后的图片文件Uri
     * @param aspectX X方向的比例
     * @param aspectY Y方向的比例
     * @param outputX   剪裁图片的宽度
     * @param outputY  剪裁图片的高度
     */
    public void cropPhoto(Uri srcImgUri, Uri cropImgUri, int aspectX, int aspectY, int outputX, int outputY) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(srcImgUri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection",true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                activity.grantUriPermission(packageName, cropImgUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImgUri);
        activity.startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
    }

    /**
     * 需要在Activity的onActivityResult回调里面调用此方法，用于显示最终得到的图片
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                /** 相册 */
                case REQUEST_CODE_LOCAL_ALBUM:
                    try {
                        if(needCrop){
                            if (aspectX != -1 && aspectY != -1 && outputX != -1 && outputY != -1) {
                                cropPhoto(data.getData(), photoUri, aspectX, aspectY, outputX, outputY);
                            } else {
                                cropPhoto(data.getData(), photoUri);
                            }
                        }else{
                            try{
                                Bitmap originalBitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(data.getData()));
                                if(originalBitmap != null){
                                    compressBitmap = compressBitmap(originalBitmap,compressLimit, imageView);//压缩原图片
                                    originalBitmap.recycle();
                                    saveBitmapFile(compressBitmap, saveFile);//将压缩后的图片替换原图片文件
                                    imageView.setImageBitmap(compressBitmap);
                                    isLoadPhoto = true;
                                    if(successListener != null){
                                        successListener.onSuccess();
                                    }
                                }
                            }catch (OutOfMemoryError e){

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                /** 拍照 */
                case REQUEST_CODE_TAKE_PHOTO:
                    try {
                        if (needCrop) {
                            if (aspectX != -1 && aspectY != -1 && outputX != -1 && outputY != -1) {
                                cropPhoto(photoUri, photoUri, aspectX, aspectY, outputX, outputY);
                            } else {
                                cropPhoto(photoUri, photoUri);
                            }
                        } else {
                            try {
                                Bitmap originalBitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(photoUri));
                                if (originalBitmap != null) {
                                    int origByte = originalBitmap.getByteCount();
                                    compressBitmap = compressBitmap(originalBitmap, compressLimit, imageView);
                                    int compressByte = compressBitmap.getByteCount();
                                    originalBitmap.recycle();
                                    saveBitmapFile(compressBitmap, saveFile);
                                    imageView.setImageBitmap(compressBitmap);
                                    isLoadPhoto = true;
                                    if (successListener != null) {
                                        successListener.onSuccess();
                                    }
                                }
                            } catch (OutOfMemoryError e) {

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                /** 裁剪图片 */
                case REQUEST_CODE_CROP_PHOTO:
                    try{
                        Bitmap originalBitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(photoUri));
                        if(originalBitmap != null){
                            compressBitmap = compressBitmap(originalBitmap, compressLimit, imageView);
                            originalBitmap.recycle();
                            saveBitmapFile(compressBitmap,saveFile);
                            imageView.setImageBitmap(compressBitmap);
                            isLoadPhoto = true;
                            if(successListener != null){
                                successListener.onSuccess();
                            }
                        }
                    }catch (OutOfMemoryError e){

                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    /**
     * File转化成Uri
     * @param file
     * @return
     */
    public Uri file2Uri(File file){
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    /**
     * 将bitmap转换成file
     * @param bitmap
     * @param file
     * @return
     */
    public static File saveBitmapFile(Bitmap bitmap, File file) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 图片按比例大小压缩方法
     * 先采样率压缩，如果压缩后图片还大于limitSize，再使用质量压缩
     *
     * @param originalBitmap （根据Bitmap图片压缩）
     * @param limitSize Bitmap需要压缩的最大值，单位M
     * @return
     */
    public Bitmap compressBitmap(Bitmap originalBitmap, int limitSize ,ImageView imageView){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        int options = 80;
//        // 判断如果图片大于limitSize,进行压缩避免在生成图片（BitmapFactory.decodeStream）时内存溢出
//        while (baos.toByteArray().length / 1024 > limitSize * 1024) {
//            Log.i("CameraUtil","while = " + options);
//            baos.reset();// 重置baos即清空baos
//            originalBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩50%，把压缩后的数据存放到baos中
//            options -= 10;
//        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        int bitmapWidth = newOpts.outWidth;
        int bitmapHeight = newOpts.outHeight;
        //获取ImageView的宽高
        float imageViewWidth = imageView.getWidth();
        float imageViewHeight = imageView.getHeight();
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int scale = 1;// scale=1表示不缩放
        if (bitmapWidth > bitmapHeight && bitmapWidth > imageViewWidth) {// 如果宽度大的话根据宽度固定大小缩放
            scale = (int) (newOpts.outWidth / imageViewWidth);
        } else if (bitmapWidth < bitmapHeight && bitmapHeight > imageViewHeight) { // 如果高度高的话根据高度固定大小缩放
            scale = (int) (newOpts.outHeight / imageViewHeight);
        }
        if (scale <= 0)
            scale = 1;
        newOpts.inJustDecodeBounds = false;
        newOpts.inSampleSize = scale; // 设置缩放比例
        newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;//降低图片从ARGB888到RGB565
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 80;
        // 判断如果图片大于limitSize,进行压缩避免在生成图片（BitmapFactory.decodeStream）时内存溢出
        while (baos.toByteArray().length / 1024 > limitSize * 1024) {
            baos.reset();// 重置baos即清空baos
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩50%，把压缩后的数据存放到baos中
            options -= 10;
        }
        return bitmap;
    }

    /**
     * 获取图片保存父目录
     * @return
     */
    public static File getPhotoDir() {
        return photoDir;
    }

    /**
     * 获取拍照或从相册选择的图片Uri
     * @return
     */
    public Uri getPhotoUri(){
        return photoUri;
    }

    /**
     * 设置压缩阈值,需要在打开相机或相册之前调用
     * @param limitSize
     */
    public void setCompressLimit(int limitSize){
        compressLimit = limitSize;
    }

    /**
     * 设置裁剪图片宽高比例和大小,需要在打开相机或相册之前调用
     * 注：aspectX、aspectY控制裁剪宽高比例，outputX、outputY控制图片输出大小
     * @param aspectX
     * @param aspectY
     * @param outputX
     * @param outputY
     */
    public void setCropData(int aspectX, int aspectY, int outputX, int outputY){
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        this.outputX = outputX;
        this.outputY = outputY;
    }

    /**
     * 图片是否加载成功
     * @return
     */
    public boolean isLoadPhoto(){
        return isLoadPhoto;
    }

    /**
     * 重置图片加载状态
     */
    public void resetIsLoadPhoto(){
        isLoadPhoto = false;
    }

    private OnImageLoadSuccessListener successListener;
    public void setOnImageLoadSuccessListener(OnImageLoadSuccessListener listener){
        successListener = listener;
    }

    /**
     * 图片加载成功监听
     */
    public interface OnImageLoadSuccessListener{
        void onSuccess();
    }

    // 解析获取图片库图片Uri物理路径
    @SuppressLint("NewApi")
    public static String parsePicturePath(Context context, Uri uri) {

        if (null == context || uri == null)
            return null;

        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentUri
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageDocumentsUri
            if (isExternalStorageDocumentsUri(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] splits = docId.split(":");
                String type = splits[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + File.separator + splits[1];
                }
            }
            // DownloadsDocumentsUri
            else if (isDownloadsDocumentsUri(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaDocumentsUri
            else if (isMediaDocumentsUri(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosContentUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;

    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        String column = "_data";
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            try {
                if (cursor != null)
                    cursor.close();
            } catch (Exception e) {
                Log.e("harvic",e.getMessage());
            }
        }
        return null;

    }

    private static boolean isExternalStorageDocumentsUri(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocumentsUri(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocumentsUri(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosContentUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 判断相机是否可用
     * @return true 可以使用 ; false 不可以使用
     */
    public boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); //针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        } if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }

    /**
     * 释放资源
     */
    public void onDestroy(){
        if(compressBitmap !=null && !compressBitmap.isRecycled()){
            compressBitmap.recycle();
            compressBitmap = null;
        }
        System.gc();
    }
}
