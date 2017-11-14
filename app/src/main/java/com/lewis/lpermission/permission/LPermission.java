package com.lewis.lpermission.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: lewis
 * Date: 2017/11/7.
 * Description:
 */

public class LPermission {

    /**
     * may be an Activity, a Fragment or a Context
     */
    private Object mObject;
    /**
     * request code
     */
    private int mRequestCode;
    /**
     * permissions
     */
    private String[] mPermissions;

    /**
     * check if need show rationale
     * true-check false-not check
     */
    private boolean mIsCheck;

    /*********init***************/
    private LPermission(Object object) {
        this.mObject = object;
    }

    /**
     * In the Activity
     *
     * @param activity {@link Activity}
     * @return {@link LPermission}
     */
    public static @NonNull
    LPermission with(@NonNull Activity activity) {
        return new LPermission(activity);
    }

    /**
     * In the Activity
     *
     * @param fragment {@link android.app.Fragment}
     * @return {@link LPermission}
     */
    public static @NonNull
    LPermission with(@NonNull android.app.Fragment fragment) {
        return new LPermission(fragment);
    }

    /**
     * In the Activity
     *
     * @param fragment {@link android.support.v4.app.Fragment}
     * @return {@link LPermission}
     */
    public static @NonNull
    LPermission with(@NonNull android.support.v4.app.Fragment fragment) {
        return new LPermission(fragment);
    }

    /**
     * Context
     *
     * @param context {@link Context}
     * @return {@link LPermission}
     */
    public static @NonNull
    LPermission with(@NonNull Context context) {
        return new LPermission(context);
    }

    /**
     * permission request code
     *
     * @param requestCode 请求码
     * @return {@link LPermission}
     */
    public @NonNull
    LPermission requestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    /**
     * permissions you want to apply
     *
     * @param permissions permissions
     * @return {@link LPermission}
     */
    public @NonNull
    LPermission permission(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    /**
     * true if you want to show rationale, otherwise false
     *
     * @param flag a flag to show or not show rationale
     * @return {@link LPermission}
     */
    public @NonNull
    LPermission showRationale(boolean flag) {
        this.mIsCheck = flag;
        return this;
    }

    /**
     * request for permission
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public void request() {
        if (mIsCheck) {
            applyPermissions(mObject, mRequestCode, mPermissions);
        } else {
            requestPermissions(mObject, mRequestCode, mPermissions);
        }
    }

    /**
     * get permissions that you will request, don't check if the permission has been rejected
     *
     * @param object      object who request permissions
     * @param requestCode request code
     * @param permissions request permissions
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    private void requestPermissions(Object object, int requestCode, String[] permissions) {
        if (PermissionUtil.isLowerMarshmallow()) {
            doExecuteSuccess(object, requestCode);
            return;
        }
        String[] denyPermissions = PermissionUtil.getDeniedPermissions(PermissionUtil.getActivity(object), permissions);
        if (denyPermissions.length > 0) {
            if (object instanceof Activity) {
                ((Activity) object).requestPermissions(permissions, requestCode);
            } else if (object instanceof Fragment) {
                ((Fragment) object).requestPermissions(permissions, requestCode);
            } else {
                throw new IllegalArgumentException(object.getClass().getName() + " is not supported!");
            }
        } else {
            doExecuteSuccess(object, requestCode);
        }
    }

    /**
     * get permissions that you will request, check if the permission has been rejected
     *
     * @param object      object who request permissions
     * @param requestCode request code
     * @param permissions request permissions
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    private static void applyPermissions(Object object, int requestCode, String... permissions) {
        if (PermissionUtil.isLowerMarshmallow()) {
            doExecuteSuccess(object, requestCode);
            return;
        }
        String[] denyPermissions = PermissionUtil.getDeniedPermissions(PermissionUtil.getActivity(object), permissions);
        if (denyPermissions.length > 0) {
            if (object instanceof Activity) {
                if (!shouldShowRationale((Activity) object, requestCode, permissions)) {
                    ((Activity) object).requestPermissions(permissions, requestCode);
                }
            } else if (object instanceof Fragment) {
                if (!shouldShowRationale((Fragment) object, requestCode, permissions)) {
                    ((Fragment) object).requestPermissions(permissions, requestCode);
                }
            } else {
                throw new IllegalArgumentException(object.getClass().getName() + " is not supported!");
            }
        } else {
            doExecuteSuccess(object, requestCode);
        }
    }

    /**
     * request success
     *
     * @param object      object request permission
     * @param requestCode permission request code
     */
    private static void doExecuteSuccess(Object object, int requestCode) {
        callAnnotation(object, requestCode, PermissionGranted.class);
    }

    /**
     * request failure
     *
     * @param object      permission request object
     * @param requestCode permission request code
     */
    private static void doExecuteFail(Object object, int requestCode) {
        callAnnotation(object, requestCode, PermissionDenied.class);
    }

    private static void doExecuteRationale(Object object, int requestCode) {
        callAnnotation(object, requestCode, PermissionRationale.class);
    }

    private static void callAnnotation(Object object, int requestCode, @NonNull Class<? extends Annotation> annotation, Object... args) {
        Method[] executeMethod = PermissionUtil.getMethodByRequestCode(object.getClass(), annotation, requestCode);
        if (executeMethod.length > 0) {
            try {
                for (Method method : executeMethod) {
                    if (!method.isAccessible()) method.setAccessible(true);
                    method.invoke(object, args);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static void onPermissionResult(Object object, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        boolean allGranted = PermissionUtil.verifyPermission(grantResult);
        if (allGranted) {
            doExecuteSuccess(object, requestCode);
        } else {
            if (object instanceof Activity) {
                if (checkNeverAskAgain((Activity) object, requestCode, permissions)) {
                    doExecuteFail(object, requestCode);
                }
            } else if (object instanceof Fragment) {
                if (checkNeverAskAgain((Fragment) object, requestCode, permissions)) {
                    doExecuteFail(object, requestCode);
                }
            } else {
                throw new IllegalArgumentException(object.getClass().getName() + " is not supported!");
            }
        }
    }

    private static boolean shouldShowRationale(Activity activity, int requestCode, String... perms) {
        return shouldShowRequestPermissionRationale(activity, requestCode, perms);
    }

    private static boolean shouldShowRationale(Fragment fragment, int requestCode, String... perms) {
        return shouldShowRequestPermissionRationale(fragment.getActivity(), requestCode, perms);
    }

    private static boolean shouldShowRequestPermissionRationale(Activity activity, int requestCode, String... perms) {
        for (String permission : perms) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                callAnnotation(activity, requestCode, PermissionRationale.class);
                return true;
            }
        }
        return false;
    }

    private static boolean checkNeverAskAgain(Activity activity, int requestCode, String[] perms) {
        return shouldShowNeverAskAgainTip(activity, requestCode, perms);
    }

    private static boolean checkNeverAskAgain(Fragment fragment, int requestCode, String[] perms) {
        return shouldShowNeverAskAgainTip(fragment.getActivity(), requestCode, perms);
    }

    private static boolean shouldShowNeverAskAgainTip(Activity activity, int requestCode, String[] perms) {
        for (String permission : perms) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        callAnnotation(activity, requestCode, PermissionNeverAskAgain.class);
        return false;
    }
}
