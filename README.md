## 工具类合集Utils
### 使用
在项目根目录的build.gradle中添加：

    allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" } 
        }
    }
在module目录的build.gradle中添加：

    dependencies {
        implementation 'com.github.ligen4w:Utils:v1.0.9' 
    } 
#### SensorHelper—重力感应帮助类

    public class SensorActivity extends AppCompatActivity {
        private SensorHelper sensorHelper;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sensor);
            icIcon = (ImageView) findViewById(R.id.iv_icon);
            sensorHelper = SensorHelper.newInstance(this);
            //设置支持旋转的方向，分别对应正向竖屏、反向竖屏、正向横屏和反向横屏
            sensorHelper.setSupportOrientation(false,false,false,false);
            sensorHelper.setOnDegreeChangedListener(new SensorHelper.OnDegreeChangedListener() {
                @Override
                public void onDegreeChanged(int exactDegree) {
                    Log.i("lg","exactDegree:" + exactDegree);
                    int difDegree = exactDegree - lastDegree;
                    if(difDegree >= -180 && difDegree <= 180){
                        ObjectAnimator.ofFloat(icIcon, "rotation", exactDegree).start();
                    }else{
                        ObjectAnimator.ofFloat(icIcon, "rotation", difDegree - 360).start();
                    }
                    lastDegree = exactDegree;
                }
            });
            //打开监听
    //        sensorHelper.startObserver();
        }

        @Override
        protected void onResume() {
            super.onResume();
            //使重力感应可用
            sensorHelper.enable();
        }

        @Override
        protected void onPause() {
            super.onPause();
            //使重力感应不可用
            sensorHelper.disable();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            //关闭监听
    //        sensorHelper.stopObserver();
        }
    }

#### CameraUtil—拍照工具类

    /**
     * 拍照
     * @param photoName 设置拍照后的图片名称，可根据名称找到相应图片文件，处理文件上传等
     * @param needCrop 是否需要裁剪
     * @param imageView 加载图片的ImageView
     */
    public void takePhoto(@NonNull String photoName, boolean needCrop, @NonNull ImageView imageView)

    /**
     * 从相册中选择图片
     * @param photoName 设置选中图片的名称，可根据名称找到相应图片文件，处理文件上传等
     * @param needCrop 是否需要裁剪
     * @param imageView 显示图片的控件
     */
    public void pickFromAlbum(@NonNull String photoName, boolean needCrop, @NonNull ImageView imageView)
    
    /**
     * 设置压缩阈值,需要在打开相机或相册之前调用
     * @param limitSize
     */
    public void setCompressLimit(int limitSize)
    
    /**
     * 设置裁剪图片宽高比例和大小,需要在打开相机或相册之前调用
     * 注：aspectX、aspectY控制裁剪宽高比例，outputX、outputY控制图片输出大小
     * @param aspectX
     * @param aspectY
     * @param outputX
     * @param outputY
     */
    public void setCropData(int aspectX, int aspectY, int outputX, int outputY)
    
    /**
     * 图片是否加载成功
     * @return
     */
    public boolean isLoadPhoto()

    /**
     * 重置图片加载状态
     */
    public void resetIsLoadPhoto()

    /**
     * 图片加载成功监听
     */
    public interface OnImageLoadSuccessListener{
        void onSuccess();
    }
    
    /**
     * 在Activity或Fragment销毁的时候调用
     * 释放Bitmap资源,调用gc方法
     */
    public void onDestroy()
    
 #### PermissionUtil—运行时权限工具类
 
    * Activity中使用
 
    /**
     * Activity中调用
     * 检查和申请多个权限
     * @param activity
     * @param permissions
     * @param requestCode
     * @return
     */
    public static boolean checkPermissions(@NonNull Activity activity, @NonNull String[] permissions, int requestCode)
    
    /**
     * Activity中调用
     * 检查和申请单个权限
     * @param activity
     * @param permission
     * @param requestCode
     * @return
     */
    public static boolean checkPermission(@NonNull Activity activity, @NonNull String permission, int requestCode)
    
    /**
     * Activity中调用
     * 用户拒绝相关权限，弹框提示去设置
     * @param activity
     * @param permission
     */
    public static void deniedPermission(Activity activity, String permission)
    
 * Fragment中使用
 
    /**
     * Fragment中调用
     * 检查和申请多个权限
     * @param fragment
     * @param permissions
     * @param requestCode
     * @return
     */
    public static boolean checkPermissions(@NonNull Fragment fragment, @NonNull String[] permissions, int requestCode)
    
    /**
     * Fragment中调用
     * 检查和申请单个权限
     * @param fragment
     * @param permission
     * @param requestCode
     * @return
     */
    public static boolean checkPermission(@NonNull Fragment fragment, @NonNull String permission, int requestCode)
    
    /**
     * Fragment中调用
     * 用户拒绝相关权限，弹框提示去设置
     * @param fragment
     * @param permission
     */
    public static void deniedPermission(Fragment fragment, String permission)
    
 * 检查高级权限
 
    1.修改系统设置
    
    /**
     * 检查修改系统设置权限，如果未打开则跳转设置页面
     * @param context
     * @return
     */
    public static boolean checkWriteSettings(Context context)
