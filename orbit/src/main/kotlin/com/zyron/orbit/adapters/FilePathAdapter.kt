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
 
package com.zyron.orbit.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R
import com.zyron.orbit.databinding.FilePathViewItemBinding
import com.zyron.orbit.utils.Utils.getColorAttr
import java.io.File

class FilePathAdapter : ListAdapter<File, FilePathAdapter.ViewHolder>(FileDiffCallback()) {

    inner class ViewHolder(val binding: FilePathViewItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FilePathViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val file = getItem(position)
            val colorPrimary = getColorAttr(root.context, R.attr.colorPrimary)
            val colorSurfaceInverse = getColorAttr(root.context, R.attr.colorSurfaceInverse)

            name.text = when (file.name.trim()) {
                "" -> "<No Name>"
                "0" -> "Storage"
                "sdcard" -> "SD Card"
                "." -> "<Current Directory>"
                ".." -> "<Parent Directory>"
                "root" -> "<Root Directory>"
                else -> {
                    if (file.name.contains(" ", true)) {
                        "<File/Directory with spaces>"
                    } else if (file.name.contains(".", true)) {
                        "<File with extension>"
                    } else if (file.name.length > 20) {
                        "<Long file/directory name>"
                    } else {
                        file.name
                    }
                }
            }

            if (position == itemCount - 1) {
                name.setTextColor(colorPrimary)
                separator.visibility = View.GONE
            } else {
                name.setTextColor(colorSurfaceInverse)
                separator.visibility = View.VISIBLE
            }
        }
    }
}

class FileDiffCallback : DiffUtil.ItemCallback<File>() {
    override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
        // Compare items based on their paths
        return oldItem.path == newItem.path
    }

    override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
        // Compare the full content of the items
        return oldItem == newItem
    }
}