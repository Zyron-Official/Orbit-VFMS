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

package com.zyron.orbit

import android.content.Context
import android.util.Log
import com.zyron.orbit.map.FileMapManager
import com.zyron.orbit.map.ConcurrentFileMap
import com.zyron.orbit.utils.FileTreeUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Listener interface to notify when the file tree is updated.
 */
interface FileTreeAdapterUpdateListener {
    fun onFileTreeUpdated(startPosition: Int, itemCount: Int)
}

/**
 * Represents a item in a file tree, corresponding to a file or directory.
 *
 * @property children The file or directory represented by this item.
 * @property parent The parent item of this item in the file tree.
 * @property level The depth level of this item in the file tree.
 */
data class FileTreeItem(var children: File, var isExpanded: Boolean = false, var parent: FileTreeItem? = null, var level: Int = 0) {
    var childrenStartIndex: Int = 0
    var childrenEndIndex: Int = 0    
    val name: String = children.name
    val path: String = children.path
    val extension: String = children.extension
    val size: Long = children.length() 
    val isEmpty: Boolean = children.length() == 0L
    val isDirectory: Boolean = children.isDirectory
    val isFile: Boolean = children.isFile

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as FileTreeItem
        return this.children.absolutePath == other.children.absolutePath
    }

    override fun hashCode(): Int {
        return children.absolutePath.hashCode()
    }
}

/**
 * FileTree represents a file tree structure with functionality to expand and collapse items.
 *
 * @property context The application context.
 * @property rootDirectory The root directory path of the file tree.
 */
class FileTree(private val context: Context, private val rootDirectory: String) {

    private val items: MutableList<FileTreeItem> = mutableListOf()
    private val expandedItems: MutableSet<FileTreeItem> = mutableSetOf()
    private val collapsedItems: MutableSet<FileTreeItem> = mutableSetOf()
    private val itemsMutex: Mutex = Mutex() 
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var adapterUpdateListener: FileTreeAdapterUpdateListener? = null
    private var isSingleExpansionEnabled: Boolean = false  
    private var isSingleContractionEnabled: Boolean = false  
    private var isMainRecursiveExpansionEnabled: Boolean = false  
    private var isMainRecursiveContractionEnabled: Boolean = false  
    private var isRecursiveExpansionEnabled: Boolean = false  
    private var isRecursiveContractionEnabled: Boolean = false  
        
    /**
     * Sets the listener for adapter updates.
     *
     * @param listener The listener to be notified of updates.
     */
    fun setAdapterUpdateListener(listener: FileTreeAdapterUpdateListener) {
        this.adapterUpdateListener = listener
    }
    
    fun setSingleExpansionEnabled(state: Boolean) {
        this.isSingleExpansionEnabled = state
    }
    
    fun setSingleContractionEnabled(state: Boolean) {
        this.isSingleContractionEnabled = state
    }   
    
    fun setMainRecursiveExpansionEnabled(state: Boolean) {
        this.isMainRecursiveExpansionEnabled = state
    }
    
    fun setMainRecursiveContractionEnabled(state: Boolean) {
        this.isMainRecursiveContractionEnabled = state
    }             
    
    fun setRecursiveExpansionEnabled(state: Boolean) {
        this.isRecursiveExpansionEnabled = state
    }
    
    fun setRecursiveContractionEnabled(state: Boolean) {
        this.isRecursiveContractionEnabled = state
    }    

    init {
        val file = File(rootDirectory)
        val rw = file.canRead() && file.canWrite()
        if (!file.exists() || !rw) {
            Log.e(this::class.java.simpleName, "The provided path: $rootDirectory is invalid or does not exist.")
            Log.d(this::class.java.simpleName, "Continuing anyway...")
        }

        coroutineScope.coroutineContext[Job]?.invokeOnCompletion { exception ->
            if (exception is CancellationException) {
                Log.d(this::class.java.simpleName, "FileTree operation was cancelled.")
            }
        }

        coroutineScope.launch {
            try {
                val rootPath = Paths.get(rootDirectory)
                val rootItem = FileTreeItem(rootPath.toFile())
                addItem(rootItem)
                FileMapManager.startFileMapping(items)
                onSingleExpansion(rootItem)
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "Error initializing FileTree: ${e.localizedMessage}", e)
            }
        }
    }   
    
    fun setExpandFileTree(file: FileTreeItem) {
        when {
            isSingleExpansionEnabled -> onSingleExpansion(file)
            isRecursiveExpansionEnabled -> onRecursiveExpansion(file)
            isMainRecursiveExpansionEnabled -> onMainRecursiveExpansion(file)
            else -> onDefaultExpansion(file)
        }
    }
    
    fun setCollapseFileTree(file: FileTreeItem) {
        when {
            isSingleExpansionEnabled -> onSingleContraction(file)
            isRecursiveExpansionEnabled -> onRecursiveContraction(file)
            isMainRecursiveExpansionEnabled -> onMainRecursiveContraction(file)
            else -> onDefaultContraction(file)
        }    
    }

    /**
     * Returns the list of all items in the file tree.
     *
     * @return List of FileTreeItem objects.
     */
    fun getItems(): List<FileTreeItem> = items

    /**
     * Returns the set of expanded items in the file tree.
     *
     * @return Set of expanded FileTreeItem objects.
     */
    fun getExpandedItems(): Set<FileTreeItem> = expandedItems
    
    /**
     * Returns the set of collapsed items in the file tree.
     *
     * @return Set of collapsed FileTreeItem objects.
     */
    fun getCollapsedItems(): Set<FileTreeItem> = collapsedItems    

    /**
     * Adds a item to the file tree and notifies the adapter.
     *
     * @param item The item to add.
     * @param parent The parent item, if any.
     */
    private suspend fun addItem(item: FileTreeItem, parent: FileTreeItem? = null) {
        item.parent = parent
        items.add(item)
        withContext(Dispatchers.Main) {
            adapterUpdateListener?.onFileTreeUpdated(items.indexOf(item), 1)
        }
    }
    
    private suspend fun collectSingleChildren(item: FileTreeItem, itemsToRemove: MutableList<FileTreeItem>) {
        val children = items.filter { it.parent == item }
        itemsToRemove.addAll(children)
        children.forEach { childItem ->
            collectSingleChildren(childItem, itemsToRemove)
        }
    }
        
    private suspend fun collectAllDirectChildren(item: FileTreeItem, itemsToRemove: MutableList<FileTreeItem>) {
        val queue = ArrayDeque<FileTreeItem>()
        val parentMap = items.groupBy { it.parent }
        queue.add(item)
        while (queue.isNotEmpty()) {
            val currentItem = queue.removeFirst()
            val children = parentMap[currentItem] ?: emptyList()
            itemsToRemove.addAll(children)
            queue.addAll(children)
        }
    }
        
    private fun onMainRecursiveExpansion(item: FileTreeItem) {
        if (!item.isExpanded && item.isDirectory) {
            item.isExpanded = true
            coroutineScope.launch {
                try {
    
                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "Error expanding item: ${e.localizedMessage}", e)
                }
            }
        }
    }
    
    private fun onMainRecursiveContraction(item: FileTreeItem) {
        if (item.isExpanded && item.isDirectory) {
            item.isExpanded = false
            coroutineScope.launch {
                try {
    
                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "Error expanding item: ${e.localizedMessage}", e)
                }
            }
        }
    }    
    
    private fun onRecursiveExpansion(item: FileTreeItem) {
        if (!item.isExpanded && item.isDirectory) {
            item.isExpanded = true
            coroutineScope.launch {
                try {
    
                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "Error expanding item: ${e.localizedMessage}", e)
                }
            }
        }
    }
    
    private fun onRecursiveContraction(item: FileTreeItem) {
        if (item.isExpanded && item.isDirectory) {
            item.isExpanded = false
            coroutineScope.launch {
                try {
    
                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "Error expanding item: ${e.localizedMessage}", e)
                }
            }
        }
    }    

    private fun onSingleExpansion(item: FileTreeItem) {
        if (!item.isExpanded && item.isDirectory) {
            item.isExpanded = true
            coroutineScope.launch {
                try {
                    expandedItems.add(item)
                    val newItems: List<FileTreeItem>? = ConcurrentFileMap.concurrentFileMap[item] ?: FileTreeUtils.letSortItems(item)
                    newItems?.takeIf { it.isNotEmpty() }?.let { itemsToInsert ->
                        val insertIndex = items.indexOf(item) + 1
                        item.childrenStartIndex = insertIndex
                        item.childrenEndIndex = insertIndex + itemsToInsert.size
                        items.addAll(insertIndex, itemsToInsert)
                        withContext(Dispatchers.Main) {
                            adapterUpdateListener?.onFileTreeUpdated(item.childrenStartIndex, item.childrenEndIndex)
                        }
                    } ?: Log.w(this::class.java.simpleName, "No items to expand for $item")
    
                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "Error expanding item: ${e.localizedMessage}, item: $item", e)
                }
            }
        }
    }
    
    private fun onSingleContraction(item: FileTreeItem) {
        if (item.isExpanded && item.isDirectory) {
            item.isExpanded = false
            coroutineScope.launch {
                try {

                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "Error collapsing item: ${e.localizedMessage}", e)
                }
            }
        }
    }    
    
    private fun onDefaultExpansion(item: FileTreeItem) {
        if (!item.isExpanded && item.isDirectory) {
            item.isExpanded = true
            coroutineScope.launch {
                try {
                    expandedItems.add(item)
                    val newItems: List<FileTreeItem>? = ConcurrentFileMap.concurrentFileMap[item] ?: FileTreeUtils.letSortItems(item) 
                    newItems?.let { itemsToInsert ->
                        if (itemsToInsert.isNotEmpty()) {
                            val insertIndex = items.indexOf(item) + 1
                            item.childrenStartIndex = insertIndex
                            item.childrenEndIndex = insertIndex + itemsToInsert.size
                            items.addAll(insertIndex, itemsToInsert)
                            if (itemsToInsert.size == 1) {
                                onDefaultExpansion(itemsToInsert[0])  
                            }
                            withContext(Dispatchers.Main) {
                                adapterUpdateListener?.onFileTreeUpdated(item.childrenStartIndex, item.childrenEndIndex)
                            }
                        }
                    } ?: Log.w(this::class.java.simpleName, "No items to expand for $item")
                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "Error expanding item: ${e.localizedMessage}, item: $item", e)
                }
            }
        }
    }    

    private fun onDefaultContraction(item: FileTreeItem) {
        if (item.isExpanded && item.isDirectory) {
            item.isExpanded = false
            coroutineScope.launch {
                try {
                    expandedItems.remove(item)
                    val itemsToRemove = mutableListOf<FileTreeItem>() 
                    collectAllDirectChildren(item, itemsToRemove)  
                    val itemIndex = items.indexOf(item)
                    itemsMutex.withLock {
                        items.removeAll(itemsToRemove)  
                    }
                    withContext(Dispatchers.Main) {
                        adapterUpdateListener?.onFileTreeUpdated(itemIndex, itemsToRemove.size)
                    }
                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "Error collapsing item: ${e.localizedMessage}", e)
                }
            }
        }
    }
    
    /**
     * Cancels all coroutines related to this FileTree.
     */
    fun cancelAllCoroutines() {
        coroutineScope.cancel()
    }

    /**
     * Cleans up resources when the FileTree is destroyed.
     */
    fun onDestroy() {
        coroutineScope.coroutineContext[Job]?.cancelChildren()
    }
}