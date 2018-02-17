package com.touhidroid.spycammetrics;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

/**
 * Created by touhid on 5/8/16.
 *
 * @author touhid
 */
public class PermissionManager {


    private static String curMessage = null;

    /**
     * @param activity        requesting & receiving activity (single activity).
     * @param permissionsList List of pairs as main permission string keys following their names.
     * @return true, if all asked permissions are already granted <br/>
     * false, otherwise.
     */
    public static boolean requestMultiPermission(final Activity activity,
                                                 final ArrayList<Pair<String, String>> permissionsList,
                                                 final int reqCode) {

        final ArrayList<String> permissions = new ArrayList<>();
        ArrayList<Pair<String, String>> permissionList = new ArrayList<>();
        int sz = permissionsList.size();
        //for(Pair<String, String> p : permissionsList) {
        for (int i = 0; i < sz; i++) {
            Pair<String, String> p = permissionsList.get(i);
            if (ContextCompat.checkSelfPermission(activity, p.first)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(p);
                permissions.add(p.first);
            }
        }

        if (permissions.size() > 0) {
            // Need Rationale
            String message = "You need to grant access to " + permissionList.get(0).second;
            for (int i = 1; i < permissionList.size(); i++)
                message = message + ", " + permissionList.get(i).second;
            showMessageOKCancel(activity, message,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            curMessage = null;
                            ActivityCompat.requestPermissions(activity,
                                    permissions.toArray(new String[permissions.size()]),
                                    reqCode);
                        }
                    });
            return false;
        }
        return true; // All provided permissions were previously accepted.
    }

    private static void showMessageOKCancel(Activity activity, String message,
                                            DialogInterface.OnClickListener okListener) {
        if (curMessage != null && curMessage.equals(message))
            return;
        curMessage = message;

        new AlertDialog.Builder(activity)
                .setMessage(curMessage)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public static boolean addPermission(Activity activity,
                                        ArrayList<String> permissionsList,
                                        String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                return false;
        }
        return true;
    }

    /**
     * If the permission as in @permissionName is already granted,
     * then true is returned. <br/>
     * Otherwise, the permission is requested from the user and false is returned.
     * In this case, the activity's onRequestPermissionsResult() method is called with the mentioned @reqCode.
     */
    public static boolean requestSinglePermission(final Activity activity,
                                                  final String permissionName,
                                                  final int reqCode, String reqMsg) {
        int permissionStatus = ContextCompat.checkSelfPermission(activity, permissionName);
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, permissionName)) {
                showMessageOKCancel(activity, reqMsg,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                curMessage = null;
                                ActivityCompat.requestPermissions(
                                        activity, new String[]{permissionName}, reqCode);
                            }
                        });
                return false;
            }
            ActivityCompat.requestPermissions(
                    activity, new String[]{permissionName},
                    reqCode);
            return false;
        }
        return true;
    }
}

/*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted - do whatever exhaustion you like now 3:D
                }
                break;

            default:
                break;
        }
    }*/