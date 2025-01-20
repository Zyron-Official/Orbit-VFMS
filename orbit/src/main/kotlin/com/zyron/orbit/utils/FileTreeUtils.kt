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

import com.zyron.orbit.FileTreeItem
import java.io.File

object FileTreeUtils {

    /**
     * Sorts the children of this item, separating directories from files and sorting them alphabetically.
     *
     * @param item The FileTreeItem whose children will be sorted.
     * @return A list of FileTreeItem objects representing the sorted children.
     */
    fun letSortItems(item: FileTreeItem): List<FileTreeItem> {
        val children = item.children.listFiles()?.asSequence()?.partition { it.isDirectory }?.let { (directories, files) ->
            (directories.sortedBy { it.name.lowercase() } + files.sortedBy { it.name.lowercase() })
            } ?: emptyList()

        return children.map { childFile ->
            FileTreeItem(children = childFile, parent = item, level = item.level + 1)
        }
    }
}