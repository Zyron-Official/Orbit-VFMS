package com.zyron.orbit

import android.content.Context
import android.util.Log
import com.zyron.orbit.adapters.FileListAdapter
import com.zyron.orbit.providers.DefaultFileIconProvider
import kotlinx.coroutines.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Listener interface to notify when the file list is updated.
 */
interface FileListAdapterUpdateListener {
    fun onFileListUpdated(startPosition: Int, itemCount: Int)
}

data class FileListItem(var item: File) {
    val name: String = item.name
    val path: String = item.path
    val extension: String = item.extension
    val size: Long = item.length() 
    val isEmpty: Boolean = item.length() == 0L
    val isDirectory: Boolean = item.isDirectory
    val isFile: Boolean = item.isFile
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as FileListItem
        return this.item.absolutePath == other.item.absolutePath
    }

    override fun hashCode(): Int {
        return item.absolutePath.hashCode()
    }    
}

class FileList(private val context: Context, private val rootDirectory: String) {

    private var items: MutableList<FileListItem> = mutableListOf()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var adapterUpdateListener: FileListAdapterUpdateListener? = null

    init {
        val file = File(rootDirectory)
        if (!file.exists() || !(file.canRead() && file.canWrite())) {
            Log.e(this::class.java.simpleName, "Invalid path: $rootDirectory")
        }
        
        coroutineScope.launch {
            try {
                val fileItems = setLoadItemList()  
                items.addAll(fileItems)  
                withContext(Dispatchers.Main) {
                    adapterUpdateListener?.onFileListUpdated(0, fileItems.size)
                }
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "Error loading files: ${e.message}", e)
            }
        }
    }
    
    /**
     * Sets the listener for adapter updates.
     *
     * @param listener The listener to be notified of updates.
     */
    fun setAdapterUpdateListener(listener: FileListAdapterUpdateListener) {
        this.adapterUpdateListener = listener
    } 
       
    /**
     * Returns the list of all items in the file list.
     *
     * @return List of FileListItem objects.
     */
    fun getItems(): MutableList<FileListItem> = items
    
    /**
     * Loads the file items from the specified root directory.
     * @return List of FileListItem objects.
     */
    private suspend fun setLoadItemList(): MutableList<FileListItem> {
        val fileList = mutableListOf<FileListItem>()
        val directory = File(rootDirectory)  
        val items = directory.listFiles()?.asSequence()?.partition { it.isDirectory }?.let { (directories, files) ->
                (directories.sortedBy { it.name.lowercase() } + files.sortedBy { it.name.lowercase() })
            } ?: emptyList()
            
        items?.forEach { file ->
            val fileItem = FileListItem(file)
                fileList.add(fileItem)  
        }

        return fileList
    }
    
    /**
     * Cancels all coroutines related to this FileList.
     */
    fun cancelAllCoroutines() {
        coroutineScope.cancel()  
    }

    /**
     * Cleans up resources when the FileList is destroyed.
     */
    fun onDestroy() {
        coroutineScope.coroutineContext[Job]?.cancelChildren()
    }
}