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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Utility object providing file-related helper methods.
 */
object FileUtils {

    /** Regex used to check if a file name extension is not of a text file. */
    private val INVALID_TEXT_FILES_REGEX = Regex(".*\\.(bin|ttf|png|jpe?g|bmp|mp4|mp3|m4a|iso|so|zip|rar|jar|dex|odex|vdex|7z|apk|apks|xapk)$")

    /**
     * Checks if the file with the given name is a valid text file by ensuring that the name extension
     * is not in the [INVALID_TEXT_FILES_REGEX].
     *
     * @param filename The file name to check the extension.
     * @return True if it is a valid text file, false otherwise.
     */
    fun isValidTextFile(filename: String): Boolean {
        return !filename.matches(INVALID_TEXT_FILES_REGEX)
    }

    /**
     * Checks if storage permission has been granted.
     *
     * @return True if storage permission has been granted, false otherwise.
     */
    fun Context.isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Gets the parent directory path from the given path. For example, from "/path1/path2", it will
     * return "/path1".
     *
     * @param path The path from which to extract the parent directory.
     * @return The parent directory path.
     */
    fun getParentDirPath(path: String): String {
        val index = path.lastIndexOf("/")
        return if (index >= 0) {
            path.substring(0, index)
        } else path
    }
}