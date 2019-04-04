package com.lg.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {

    public static final int REQUEST_CODE_DEFAULT = 0x100;
    public static final int REQUEST_CODE_CAMERA = 0x101;
    public static final int REQUEST_CODE_RECORD_AUDIO = 0x102;
    public static final int REQUEST_CODE_READ_PHONE_STATE = 0x103;
    public static final int REQUEST_CODE_CALL_PHONE = 0x104;
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 0x105;


    /**
     * Activity中调用
     * 检查和申请多个权限
     * @param activity
     * @param permissions
     * @param requestCode
     * @return
     */
    public static boolean checkPermissions(@NonNull Activity activity, @NonNull String[] permissions, int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> denyPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(activity, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    denyPermissionList.add(permissions[i]);
                }
            }

            if (denyPermissionList.size() > 0) {
                //存在未允许的权限
                String[] permissionsArr = denyPermissionList.toArray(new String[denyPermissionList.size()]);
                ActivityCompat.requestPermissions(activity, permissionsArr, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * Fragment中调用
     * 检查和申请多个权限
     * @param fragment
     * @param permissions
     * @param requestCode
     * @return
     */
    public static boolean checkPermissions(@NonNull Fragment fragment, @NonNull String[] permissions, int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> denyPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(fragment.getActivity(), permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    denyPermissionList.add(permissions[i]);
                }
            }

            if (denyPermissionList.size() > 0) {
                //存在未允许的权限
                String[] permissionsArr = denyPermissionList.toArray(new String[denyPermissionList.size()]);
                fragment.requestPermissions(permissionsArr, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * Activity中调用
     * 检查和申请单个权限
     * @param activity
     * @param permission
     * @param requestCode
     * @return
     */
    public static boolean checkPermission(@NonNull Activity activity, @NonNull String permission, int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * Fragment中调用
     * 检查和申请单个权限
     * @param fragment
     * @param permission
     * @param requestCode
     * @return
     */
    public static boolean checkPermission(@NonNull Fragment fragment, @NonNull String permission, int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(fragment.getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                fragment.requestPermissions(new String[]{permission}, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * 检查修改系统设置权限，如果未打开则跳转设置页面
     * @param context
     * @return
     */
    public static boolean checkWriteSettings(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canWrite =Settings.System.canWrite(context);
            if (!canWrite)
                showMissingPermissionDialog(context, Manifest.permission.WRITE_SETTINGS);
            return canWrite;
        } else {
            return true;
        }
    }

    /**
     * Activity中调用
     * 用户拒绝相关权限，弹框提示去设置
     * @param activity
     * @param permission
     */
    public static void deniedPermission(Activity activity, String permission){
        //判断是否勾选禁止后不再询问
        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        if (showRequestPermission) {
            Toast.makeText(activity, "您拒绝了权限", Toast.LENGTH_SHORT).show();
        } else {
            showMissingPermissionDialog(activity, permission);
        }
    }

    /**
     * Fragment中调用
     * 用户拒绝相关权限，弹框提示去设置
     * @param fragment
     * @param permission
     */
    public static void deniedPermission(Fragment fragment, String permission){
        //判断是否勾选禁止后不再询问
        boolean showRequestPermission = fragment.shouldShowRequestPermissionRationale(permission);
        if (showRequestPermission) {
            Toast.makeText(fragment.getActivity(), "您拒绝了权限", Toast.LENGTH_SHORT).show();
        } else {
            showMissingPermissionDialog(fragment.getActivity(), permission);
        }
    }

    /**
     * 缺少权限弹框
     * @param context
     */
    public static void showMissingPermissionDialog(final Context context, final String permission) {
        String permissionName;
        switch (permission){
            case Manifest.permission.READ_PHONE_STATE:
                permissionName = "【设备信息】";
                break;
            case Manifest.permission.CALL_PHONE:
                permissionName = "【电话】";
                break;
            case Manifest.permission.CAMERA:
                permissionName = "【相机】";
                break;
            case Manifest.permission.RECORD_AUDIO:
                permissionName = "【麦克风】";
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                permissionName = "【存储】";
                break;
            case Manifest.permission.WRITE_SETTINGS:
                permissionName = "【修改系统设置】";
                break;
            default:
                permissionName = "相应";
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage("当前应用缺少" + permissionName + "权限。\n\n请点击\"确定\"，在\"权限\"中打开所需权限");

        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.equals(permission, Manifest.permission.WRITE_SETTINGS)) {
                            startWriteSettings(context);
                        } else {
                            startDefaultSettings(context);
                        }
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        builder.show();
    }

    /**
     * 跳转默认权限页面
     * @param context
     */
    private static void startDefaultSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    /**
     * 跳转修改系统权限页面
     * @param context
     */
    private static void startWriteSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
