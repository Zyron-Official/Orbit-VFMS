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
 
package com.zyron.orbit.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.zyron.orbit.FileList
import com.zyron.orbit.FileListAdapterUpdateListener
import com.zyron.orbit.adapters.FileListAdapter  
import com.zyron.orbit.events.FileListEventListener
import com.zyron.orbit.providers.FileIconProvider
import com.zyron.orbit.providers.DefaultFileIconProvider
import com.zyron.orbit.utils.ThreadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class FileListView : RecyclerView {

    private var filePath: String? = null
    private var fileList: FileList? = null
    private var fileListAdapter: FileListAdapter? = null
    private var defaultFileIconProvider: DefaultFileIconProvider? = null
    private val recycledViewPool: RecycledViewPool = RecycledViewPool()

    constructor(context: Context) : super(context) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        // Any initialization related to attributes can be done here if needed
    }
  
    /**
     * Initializes the FileList with a given path and optional listeners.
     *
     * @param path The root path for the file list.
     * @param fileListEventListener Optional listener for file list events.
     * @param fileIconProvider Optional provider for custom file list icons.
     */
    fun initializeFileList(path: String, fileIconProvider: FileIconProvider? = null, fileListEventListener: FileListEventListener? = null) {
        fileList = FileList(context, path)
        defaultFileIconProvider = DefaultFileIconProvider()
        fileListAdapter = when {
            fileIconProvider == null && fileListEventListener == null -> FileListAdapter(context, fileList!!)
            fileIconProvider != null && fileListEventListener != null -> FileListAdapter(context, fileList!!, fileIconProvider, fileListEventListener)
            fileIconProvider != null && fileListEventListener == null -> FileListAdapter(context, fileList!!, fileIconProvider, null)
            fileIconProvider == null && fileListEventListener != null -> FileListAdapter(context, fileList!!, defaultFileIconProvider, fileListEventListener)
            else -> FileListAdapter(context, fileList!!, defaultFileIconProvider, null)
        }

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = fileListAdapter
        
        recycledViewPool.setMaxRecycledViews(0, 100)
        this.setRecycledViewPool(recycledViewPool)
          
        fileList?.setAdapterUpdateListener(object : FileListAdapterUpdateListener {
            override fun onFileListUpdated(startPosition: Int, itemCount: Int) {
                fileListEventListener?.onFileListViewUpdated(startPosition, itemCount)
                ThreadUtils.runOnUiThread {
                    fileListAdapter?.submitList(fileList!!.getItems().toMutableList())
                }
            }
        })        
    }  

    fun onDestroyView() {
        fileList?.onDestroy()
    }    
}