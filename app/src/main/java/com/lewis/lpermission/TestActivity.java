package com.lewis.lpermission;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lewis.lpermission.permission.LPermission;
import com.lewis.lpermission.permission.PermissionDenied;
import com.lewis.lpermission.permission.PermissionGranted;
import com.lewis.lpermission.permission.PermissionNeverAskAgain;
import com.lewis.lpermission.permission.PermissionRationale;

/**
 * Author: lewis
 * Date: 2017/11/7.
 * Description:
 */

public class TestActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void test(View view) {
        //直接请求就行，区别就是如果想显示rationale showRationale里参数为true默认false
        requestCamera(true);
    }

    private void requestCamera(boolean flag) {
        LPermission.with(this)
                .requestCode(REQUEST_CAMERA)
                .permission(Manifest.permission.CAMERA)
                .showRationale(flag)
                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LPermission.onPermissionResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 成功、已经有权限的操作都在这儿
     */
    @PermissionGranted(REQUEST_CAMERA)
    public void yes() {
        Log.e("====", "成功");
        Toast.makeText(this, "成功操作", Toast.LENGTH_LONG).show();
    }

    /**
     * 失败的操作
     */
    @PermissionDenied(REQUEST_CAMERA)
    public void no() {
        Log.e("====", "失败");
        Toast.makeText(this, "失败操作", Toast.LENGTH_LONG).show();
    }

    /**
     * 注意：如果要想在这儿再次请求，showRationale必须为false
     * 如果不需要这个，showRationale直接给false或者用默认的就可
     */
    @PermissionRationale(REQUEST_CAMERA)
    void rationale() {
        Log.e("====", "dialog");
        Toast.makeText(this, "提示操作", Toast.LENGTH_LONG).show();
        //请求操作可能在弹窗里
        requestCamera(false);
    }

    /**
     * 不在询问的操作，如果是必须的权限，可以调到设置
     */
    @PermissionNeverAskAgain(REQUEST_CAMERA)
    void neverAskAgain() {
        Log.e("====", "never");
        Toast.makeText(this, "不再请求操作", Toast.LENGTH_LONG).show();
    }
}
