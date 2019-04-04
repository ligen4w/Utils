package com.lg.utilsdemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.lg.utils.CameraUtil;
import com.lg.utils.PermissionUtil;
import com.lg.utilsdemo.R;
import com.lg.utilsdemo.view.StringListDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class CameraActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.image_view)
    ImageView imageView;
    @BindView(R.id.switch_btn)
    SwitchCompat switchBtn;
    @BindView(R.id.et_compressLimit)
    AppCompatEditText etCompressLimit;
    @BindView(R.id.et_aspectX)
    AppCompatEditText etAspectX;
    @BindView(R.id.et_aspectY)
    AppCompatEditText etAspectY;
    @BindView(R.id.et_outputX)
    AppCompatEditText etOutputX;
    @BindView(R.id.et_outputY)
    AppCompatEditText etOutputY;

    private Unbinder unbinder;
    private CameraUtil cameraUtil;
    private StringListDialog listDialog;
    private String currentPhotoName;
    private boolean needCrop;

    boolean granted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        unbinder = ButterKnife.bind(this);
        switchBtn.setOnCheckedChangeListener(this);
        cameraUtil = CameraUtil.getInstance(this);
        cameraUtil.setCompressLimit(1);
    }

    /**
     * 初始化选择照片弹框
     */
    private void showTakePhotoDialog(final String photoName, final ImageView imageView) {
        currentPhotoName = photoName;
        if (listDialog == null) {
            ArrayList<String> optionsList = new ArrayList<>();
            optionsList.add("拍照");
            optionsList.add("从相册选择");
            listDialog = new StringListDialog(this, optionsList);
        }
        listDialog.setOnItemClickListener(new StringListDialog.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                switch (position) {
                    case 0:
                        setData();
                        granted = PermissionUtil.checkPermissions(CameraActivity.this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PermissionUtil.REQUEST_CODE_CAMERA);
                        if (granted) {
                            takePhoto(photoName, needCrop, imageView);
                        }
                        break;
                    case 1:
                        setData();
                        granted = PermissionUtil.checkPermission(CameraActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                PermissionUtil.REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                        if (granted) {
                            pickFromAlbum(photoName, needCrop, imageView);
                        }
                        break;
                }
            }
        });
        if (!listDialog.isShowing()) {
            listDialog.show();
        }
    }

    private void takePhoto(String photoName, boolean needCrop, ImageView imageView) {
        cameraUtil.takePhoto(photoName, needCrop, imageView);
        listDialog.dismiss();
    }

    private void pickFromAlbum(String photoName, boolean needCrop, ImageView imageView) {
        cameraUtil.pickFromAlbum(photoName, needCrop, imageView);
        listDialog.dismiss();
    }

    private void setData(){
        String compressLimitStr = etCompressLimit.getText().toString();
        String etAspectXStr = etAspectX.getText().toString();
        String etAspectYStr = etAspectY.getText().toString();
        String etOutputXStr = etOutputX.getText().toString();
        String etOutputYStr = etOutputY.getText().toString();
        if (!TextUtils.isEmpty(compressLimitStr)) {
            cameraUtil.setCompressLimit(Integer.parseInt(compressLimitStr));
        }
        if (needCrop) {
            if (!TextUtils.isEmpty(etAspectXStr) && !TextUtils.isEmpty(etAspectYStr) &&
                !TextUtils.isEmpty(etOutputXStr) && !TextUtils.isEmpty(etOutputYStr)) {

                cameraUtil.setCropData(Integer.parseInt(etAspectXStr),
                        Integer.parseInt(etAspectYStr),
                        Integer.parseInt(etOutputXStr),
                        Integer.parseInt(etOutputYStr));
            }
        }
        cameraUtil.setOnImageLoadSuccessListener(new CameraUtil.OnImageLoadSuccessListener() {
            @Override
            public void onSuccess() {

            }
        });
    }

    @OnClick(R.id.image_view)
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.image_view:
                showTakePhotoDialog("test", imageView);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionUtil.REQUEST_CODE_CAMERA:
                boolean granted = true;
                for (int i = 0; i < grantResults.length; i++){
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        granted = false;
                        PermissionUtil.deniedPermission(this, permissions[i]);
                    }
                }
                if (granted) {
                    takePhoto(currentPhotoName, needCrop, imageView);
                }
                break;
            case PermissionUtil.REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromAlbum(currentPhotoName, needCrop, imageView);
                } else {
                    PermissionUtil.deniedPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (cameraUtil != null) {
                cameraUtil.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraUtil.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        needCrop = isChecked;
    }
}
