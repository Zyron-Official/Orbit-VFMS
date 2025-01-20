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

package com.zyron.orbit.map

import com.zyron.orbit.FileTreeItem
import com.zyron.orbit.utils.FileTreeUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Manages a cache of file items for efficient access and manipulation using concurrency.
 *
 * @param items A list of root items to be processed and cached.
 * @param maxSize The maximum size of the cache before trimming.
 */
class ConcurrentFileMap(private val items: List<FileTreeItem>, private val maxSize: Int = 100) : Runnable {

companion object {
    val concurrentFileMap: MutableMap<FileTreeItem, List<FileTreeItem>> = ConcurrentHashMap()
}

    private val cache = ConcurrentHashMap<FileTreeItem, List<FileTreeItem>>(maxSize)
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())

    init {
        // Schedule cache trimming at fixed intervals
        executor.scheduleAtFixedRate(::trimCache, 10, 10, TimeUnit.SECONDS)
    }

    /**
     * Retrieves a list of child items for a given file item from the cache.
     *
     * @param item The parent file item.
     * @return A list of child file items, or null if the item is not in the cache.
     */
    fun get(item: FileTreeItem): List<FileTreeItem>? {
        return cache[item]
    }

    /**
     * Adds a file item and its children to the cache.
     *
     * @param item The parent file item.
     * @param result The list of child file items.
     */
    fun put(item: FileTreeItem, result: List<FileTreeItem>) {
        cache[item] = result
        if (cache.size > maxSize) {
            trimCache()
        }
    }

    /**
     * Clears the entire cache.
     */
    fun clear() {
        cache.clear()
    }

    /**
     * Processes a list of file items asynchronously to populate the cache.
     *
     * @param items The list of file items to process.
     */
    private fun processNodes(items: List<FileTreeItem>) {
        val futures = mutableListOf<Future<*>>()
        for (item in items) {
            val future = executor.submit {
                val itemList = FileTreeUtils.letSortItems(item)
                put(item, itemList)
                processNodes(itemList)
            }
            futures.add(future)
        }
        futures.forEach { it.get() }
    }

    /**
     * Trims the cache to the specified maximum size by removing excess entries.
     */
    private fun trimCache() {
        if (cache.size > maxSize) {
            val keysToRemove = cache.keys.take(cache.size - maxSize)
            for (key in keysToRemove) {
                cache.remove(key)
            }
        }
    }

    /**
     * Entry point for processing items when this Runnable is executed.
     */
    override fun run() {
        processNodes(items)
    }

    /**
     * Shuts down the executor service and cancels all running tasks.
     */
    fun shutdown() {
        executor.shutdown()
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (ex: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}