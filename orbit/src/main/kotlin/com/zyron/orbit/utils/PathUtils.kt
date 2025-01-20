/**
 * Copyright 2024-2025 Zyron, Official.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zyron.orbit.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

object PathUtils {

    /**
     * Return the path of /system.
     *
     * @return the path of /system
     */
    fun getRootPath(): String {
        return getAbsolutePath(Environment.getRootDirectory())
    }

    /**
     * Return the path of /data.
     *
     * @return the path of /data
     */
    fun getDataPath(): String {
        return getAbsolutePath(Environment.getDataDirectory())
    }

    /**
     * Return the path of /cache.
     *
     * @return the path of /cache
     */
    fun getDownloadCachePath(): String {
        return getAbsolutePath(Environment.getDownloadCacheDirectory())
    }

    private fun getAbsolutePath(file: File?): String {
        return file?.absolutePath ?: ""
    }

    /**
     * Get the external storage path (e.g., `/storage/emulated/0`).
     */
    fun getExternalStoragePath(): String {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            getAbsolutePath(Environment.getExternalStorageDirectory())
        } else {
            ""
        }
    }

    /**
     * Get the app's external data path (e.g., `/storage/emulated/0/Android/data/<package_name>`).
     */
    fun getExternalAppDataPath(context: Context): String {
        val externalFilesDir = context.getExternalFilesDir(null)
        return getAbsolutePath(externalFilesDir)
    }

    /**
     * Get the app's internal data path (e.g., `/data/data/<package_name>`).
     */
    fun getInternalAppDataPath(context: Context): String {
        return getAbsolutePath(context.filesDir)
    }

    /**
     * Get the app's external files path (e.g., `/storage/emulated/0/Android/data/<package_name>/files`).
     */
    fun getExternalAppFilesPath(context: Context): String {
        val externalFilesDir = context.getExternalFilesDir(null)
        return getAbsolutePath(externalFilesDir)
    }

    /**
     * Get the app's internal files path (e.g., `/data/data/<package_name>/files`).
     */
    fun getInternalAppFilesPath(context: Context): String {
        return getAbsolutePath(context.filesDir)
    }

    /**
     * Get the app's external cache path (e.g., `/storage/emulated/0/Android/data/<package_name>/cache`).
     */
    fun getExternalAppCachePath(context: Context): String {
        val externalCacheDir = context.externalCacheDir
        return getAbsolutePath(externalCacheDir)
    }

    /**
     * Get the app's internal cache path (e.g., `/data/data/<package_name>/cache`).
     */
    fun getInternalAppCachePath(context: Context): String {
        return getAbsolutePath(context.cacheDir)
    }

    fun getRootPathExternalFirst(): String {
        var rootPath = getExternalStoragePath()
        if (rootPath.isEmpty()) {
            rootPath = getRootPath()
        }
        return rootPath
    }

    fun getAppDataPathExternalFirst(context: Context): String {
        var appDataPath = getExternalAppDataPath(context)
        if (appDataPath.isEmpty()) {
            appDataPath = getInternalAppDataPath(context)
        }
        return appDataPath
    }

    fun getFilesPathExternalFirst(context: Context): String {
        var filePath = getExternalAppFilesPath(context)
        if (filePath.isEmpty()) {
            filePath = getInternalAppFilesPath(context)
        }
        return filePath
    }

    fun getCachePathExternalFirst(context: Context): String {
        var appPath = getExternalAppCachePath(context)
        if (appPath.isEmpty()) {
            appPath = getInternalAppCachePath(context)
        }
        return appPath
    }
}