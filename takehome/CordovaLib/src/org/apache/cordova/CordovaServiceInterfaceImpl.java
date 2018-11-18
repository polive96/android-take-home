/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package org.apache.cordova;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Pair;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.json.JSONException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default implementation of CordovaServiceInterface.
 */
public class CordovaServiceInterfaceImpl implements CordovaServiceInterface {
    private static final String TAG = "CordovaServiceInterfaceImpl";
    protected Service service;
    protected ExecutorService threadPool;

    protected CallbackMap permissionResultCallbacks;

    public CordovaServiceInterfaceImpl(Service service) {
        this(service, Executors.newCachedThreadPool());
    }

    private CordovaServiceInterfaceImpl(Service service, ExecutorService threadPool) {
        this.service = service;
        this.threadPool = threadPool;
        this.permissionResultCallbacks = new CallbackMap();
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        //stub method
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        //stub method
    }

    @Override
    public Activity getActivity() {
        //stub method
        return null;
    }

    @Override
    public Context getContext() {
        return service.getBaseContext();
    }

    @Override
    public Object onMessage(String id, Object data) {
        if ("exit".equals(id)) {
            service.stopSelf();
        }
        return null;
    }

    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    @Override
    public Service getService() {
        return service;
    }

    public void requestPermission(CordovaPlugin plugin, int requestCode, String permission) {
        String[] permissions = new String [1];
        permissions[0] = permission;
        requestPermissions(plugin, requestCode, permissions);
    }

        @SuppressLint("NewApi")
    public void requestPermissions(CordovaPlugin plugin, int requestCode, final String [] permissions) {
        final int mappedRequestCode = permissionResultCallbacks.registerCallback(plugin, requestCode);
        Permissions.check(service/*context*/, permissions, null/*rationale*/, null/*options*/, new PermissionHandler() {
            @Override
            public void onGranted() {
                Pair<CordovaPlugin, Integer> callback = permissionResultCallbacks.getAndRemoveCallback(mappedRequestCode);
                if(callback != null) {
                    try {
                        callback.first.onRequestPermissionResult(callback.second, permissions, new int[]{1});
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean hasPermission(String permission)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int result = service.checkSelfPermission(permission);
            return PackageManager.PERMISSION_GRANTED == result;
        }
        else
        {
            return true;
        }
    }


}
