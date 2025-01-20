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

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.zyron.orbit.R
import com.zyron.orbit.FileTree
import com.zyron.orbit.FileTreeItem
import com.zyron.orbit.FileTreeAdapterUpdateListener
import com.zyron.orbit.databinding.FileTreeViewItemBinding
import com.zyron.orbit.events.FileTreeEventListener
import com.zyron.orbit.providers.DefaultFileIconProvider
import com.zyron.orbit.providers.FileIconProvider
import com.zyron.orbit.widget.FileTreeView
import com.zyron.orbit.utils.FileTreeUtils

/**
 * Adapter for displaying a hierarchical list of files and directories in a RecyclerView.
 *
 * @param context The context for accessing resources and inflating views.
 * @param fileTree The FileTree instance used for managing the file tree data.
 * @param fileTreeIconProvider Provider for file and folder icons.
 * @param fileTreeEventListener Listener for file and folder events (optional).
 */
class FileTreeAdapter(
    private val context: Context,
    private val fileTree: FileTree,
    private val fileIconProvider: FileIconProvider? = null,
    private val fileTreeEventListener: FileTreeEventListener? = null
) : ListAdapter<FileTreeItem, FileTreeAdapter.FileTreeViewHolder>(FileTreeDiffCallback()), FileTreeAdapterUpdateListener {

    private var selectedItemPosition: Int = RecyclerView.NO_POSITION
    private val collapsedItems = mutableSetOf<FileTreeItem>()

    inner class FileTreeViewHolder(internal val binding: FileTreeViewItemBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Creates a new ViewHolder for a file tree item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileTreeViewHolder {
        return FileTreeViewHolder(FileTreeViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * Binds data to the ViewHolder for the given position.
     */
    override fun onBindViewHolder(holder: FileTreeViewHolder, position: Int) {
        val item = getItem(position)
        onBindItemView(holder, item, position)
    }
    
    /**
     * Configures the ViewHolder for the file tree item.
     */
    private fun onBindItemView(holder: FileTreeViewHolder, item: FileTreeItem, position: Int) {
        setItemViewLayout(holder, item)
        updateItemViewState(holder, item, position)

        when {
            item.isDirectory -> onBindDirectory(holder, item)
            item.isFile -> onBindFile(holder, item)
        }
    }

    /**
     * Sets the layout parameters for the item view based on the item's level.
     */
    private fun setItemViewLayout(holder: FileTreeViewHolder, item: FileTreeItem) {
        holder.binding.apply {
        
            val indentationMultiplier = when {
                item.isDirectory -> 14 * item.level
                item.isFile -> (14 * item.level) + 28.82f
                else -> 0
            }
            
            val indentationPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indentationMultiplier.toFloat(), context.resources.displayMetrics).toInt()
            val isLtr = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR
            val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
            val layoutParams = root.layoutParams as ViewGroup.MarginLayoutParams

            if (isLtr) {
                layoutParams.leftMargin = indentationPx
                layoutParams.rightMargin = 0
            } else if (isRtl) {
                layoutParams.rightMargin = indentationPx
                layoutParams.leftMargin = 0
            }

            root.layoutParams = layoutParams
        }
    }

    /**
     * Updates the state of the item view based on whether it is selected and expanded.
     */
    private fun updateItemViewState(holder: FileTreeViewHolder, item: FileTreeItem, position: Int) {
        holder.binding.apply {
            root.setBackgroundResource(R.drawable.item_view_background)
            root.isSelected = position == selectedItemPosition && item.isExpanded && item.isDirectory
        }
    }

    /**
     * Binds a directory item to the ViewHolder.
     */
    private fun onBindDirectory(holder: FileTreeViewHolder, item: FileTreeItem) {
        holder.binding.apply {
            val isLtr = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR
            val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

            val iconForChevron = when {
                isLtr -> fileIconProvider?.getChevronLTRIcon()
                isRtl -> fileIconProvider?.getChevronRTLIcon()
                else -> null
            }
            
            val targetRotation = when {
                item.isExpanded -> if (isLtr) 90f else -90f
                else -> 0f
            }
            
            chevronIcon.setImageDrawable(iconForChevron?.let { ContextCompat.getDrawable(context, it) })
            chevronIcon.rotation = targetRotation
            chevronIcon.visibility = View.VISIBLE
            fileIcon.setImageDrawable(ContextCompat.getDrawable(context, fileIconProvider!!.getIconForFolder(item.children)))
            fileName.text = item.name

            val currentRotation = chevronIcon.rotation
            if (currentRotation != targetRotation) {
                val rotationAnimator = ObjectAnimator.ofFloat(chevronIcon, "rotation", currentRotation, targetRotation) as ObjectAnimator
                    rotationAnimator.duration = 300
                    rotationAnimator.interpolator = AccelerateDecelerateInterpolator()
                    rotationAnimator.start()
            }
            
            chevronIcon.setOnClickListener {
                val previousPosition = selectedItemPosition
                selectedItemPosition = holder.bindingAdapterPosition
                when {
                    !item.isExpanded -> fileTree.setExpandFileTree(item)
                    else -> fileTree.setCollapseFileTree(item) 
                }
                notifyItemChanged(selectedItemPosition)
                notifyItemChanged(previousPosition)
            }
            
            root.setOnClickListener {
                fileTreeEventListener?.onFolderClick(item.children)
            }

            root.setOnLongClickListener {
                fileTreeEventListener?.onFolderLongClick(item.children) ?: false
            }
        }
    }

    /**
     * Binds a file item to the ViewHolder.
     */
    private fun onBindFile(holder: FileTreeViewHolder, item: FileTreeItem) {
        holder.binding.apply {
            chevronIcon.visibility = View.GONE
            fileIcon.setImageDrawable(ContextCompat.getDrawable(context, fileIconProvider!!.getIconForFile(item.children)))
            fileName.text = item.name

            root.setOnClickListener {
                fileTreeEventListener?.onFileClick(item.children)
            }

            root.setOnLongClickListener {
                fileTreeEventListener?.onFileLongClick(item.children) ?: false
            }
        }
    }

    /**
     * Updates the RecyclerView when the file tree changes.
     */
    override fun onFileTreeUpdated(startPosition: Int, itemCount: Int) {
        notifyItemRangeChanged(startPosition, itemCount)
    }

    /**
     * Submits a new list to be displayed.
     */
    override fun submitList(list: MutableList<FileTreeItem>?) {
        super.submitList(list?.toList())
    }
    
    /**
     * A DiffUtil.ItemCallback implementation for comparing FileTreeItem objects.
     */    
    class FileTreeDiffCallback : DiffUtil.ItemCallback<FileTreeItem>() {
    
       /**
        * Determines if the contents of two FileTreeItem items are the same.
        *
        * @param oldItem The previous FileTreeItem item.
        * @param newItem The new FileTreeItem item.
        * @return True if the contents of the two items are the same, false otherwise.
        */    
        override fun areItemsTheSame(oldItem: FileTreeItem, newItem: FileTreeItem): Boolean {
            return oldItem.name == newItem.name
        }

       /**
        * Determines if two FileTreeItem items represent the same file by comparing their absolute paths.
        *
        * @param oldItem The previous FileTreeItem item.
        * @param newItem The new FileTreeItem item.
        * @return True if the two items represent the same file, false otherwise.
        */
        override fun areContentsTheSame(oldItem: FileTreeItem, newItem: FileTreeItem): Boolean {
            return oldItem.path == newItem.path
        }
    }    
}