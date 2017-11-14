package com.lewis.lpermission.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: lewis
 * Date: 2017/11/7.
 * Description: a util for permission request
 */

public class PermissionUtil {

    /**
     * determine whether the API < 23
     *
     * @return lower return true, else return false
     */
    public static boolean isLowerMarshmallow() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    /**
     * getContext by object
     *
     * @param object object that apply permission
     * @return context
     */
    public static Context getContext(Object object) {
        if (object instanceof Activity) {
            return ((Activity) object).getBaseContext();
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity().getBaseContext();
        } else if (object instanceof android.support.v4.app.Fragment) {
            return ((android.support.v4.app.Fragment) object).getContext();
        }
        return (Context) object;
    }

    /**
     * getActivity by object
     *
     * @param object object that apply permission
     * @return context
     */
    public static Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof android.support.v4.app.Fragment) {
            return ((android.support.v4.app.Fragment) object).getActivity();
        }
        return null;
    }

    /**
     * get permissions you should request
     *
     * @param context     calling context
     * @param permissions request permissions
     * @return {@link List}
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public static String[] getDeniedPermissions(Context context, String... permissions) {
        List<String> denyPermissions = new ArrayList<>();
        for (String perm : permissions) {
            if (ActivityCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(perm);
            }
        }
        return denyPermissions.toArray(new String[denyPermissions.size()]);
    }

    /**
     * get permissions you should request
     *
     * @param activity     calling activity
     * @param permissions request permissions
     * @return {@link List}
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public static String[] getDeniedPermissions(Activity activity, String... permissions) {
        List<String> denyPermissions = new ArrayList<>();
        for (String perm : permissions) {
            if (activity.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(perm);
            }
        }
        return denyPermissions.toArray(new String[denyPermissions.size()]);
    }

    public static Method[] getMethodByRequestCode(@NonNull Class source, @NonNull Class<? extends Annotation> annotation, int requestCode) {
        List<Method> methods = new ArrayList<>();
        for (Method method : source.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                if (isSameRequestCode(method, annotation, requestCode)) {
                    methods.add(method);
                }
            }
        }
        return methods.toArray(new Method[methods.size()]);
    }

    public static Method getMethodWithRequestCode(@NonNull Class source, @NonNull Class<? extends Annotation> annotation, int requestCode) {
        for (Method method : source.getDeclaredMethods()) {
            if (method.getAnnotation(annotation) != null) {
                if (isSameRequestCode(method, annotation, requestCode)) {
                    return method;
                }
            }
        }
        return null;
    }

    private static boolean isSameRequestCode(@NonNull Method method, @NonNull Class<? extends Annotation> annotation, int requestCode) {
        if (PermissionGranted.class.equals(annotation)) {
            return requestCode == method.getAnnotation(PermissionGranted.class).value();
        } else if (PermissionDenied.class.equals(annotation)) {
            return requestCode == method.getAnnotation(PermissionDenied.class).value();
        } else if (PermissionRationale.class.equals(annotation)) {
            return requestCode == method.getAnnotation(PermissionRationale.class).value();
        } else if (PermissionNeverAskAgain.class.equals(annotation)) {
            return requestCode == method.getAnnotation(PermissionNeverAskAgain.class).value();
        }
        return false;
    }

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermission(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * check if the calling context has a set of permission.
     *
     * @param context the calling context
     * @param perms   one or more permissions, such as {@link Manifest.permission#CAMERA}.
     * @return true if all the permissions are already granted, false if at least one permission is not yet granted.
     * @see Manifest.permission
     */
    public static boolean checkPermission(Context context, @NonNull String... perms) {
        //always return true for SDK < M, let the system deal with the permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w("==", "hasPermissions: API version < M, returning true by default");
            //DANGER ZONE!!! Changing this will break the library.
            return true;
        }
        //Null context may be passed if we have detected Low API (less than M) so getting
        //to this point with a null context should not be possible
        if (context == null) {
            throw new IllegalArgumentException("Can't check permission for null context");
        }
        for (String permission : perms) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
