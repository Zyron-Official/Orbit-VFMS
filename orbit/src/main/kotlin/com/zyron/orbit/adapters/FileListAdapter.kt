package com.zyron.orbit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.zyron.orbit.FileList
import com.zyron.orbit.FileListItem
import com.zyron.orbit.FileListAdapterUpdateListener
import com.zyron.orbit.databinding.FileListViewItemBinding
import com.zyron.orbit.events.FileListEventListener
import com.zyron.orbit.providers.FileIconProvider
import com.zyron.orbit.R

class FileListAdapter(
    private val context: Context,
    private val fileList: FileList,
    private val fileIconProvider: FileIconProvider? = null,
    private val fileListEventListener: FileListEventListener? = null
) : ListAdapter<FileListItem, FileListAdapter.VH>(FileListDiffCallback()), FileListAdapterUpdateListener {

    /**
     * ViewHolder class for holding references to the views in FileListItemView.
     */
    inner class VH(internal val binding: FileListViewItemBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Creates a new ViewHolder instance with FileListItemView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(FileListViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * Binds data to the ViewHolder based on the position in the list.
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val file = getItem(position)
        onBindItemView(holder, file, position)
    }

    /**
     * Configures the ViewHolder for the file tree item.
     */
    private fun onBindItemView(holder: VH, file: FileListItem, position: Int) {
        holder.binding.apply {
            name.text = file.name
            info.setImageDrawable(ContextCompat.getDrawable(context, fileIconProvider!!.getOptionIcon()))
            root.setBackgroundResource(R.drawable.item_view_background)
        }
        when {
            file.isDirectory -> onBindDirectory(holder, file)
            file.isFile -> onBindFile(holder, file)
        }
    }

    /**
     * Binds a directory item to the ViewHolder.
     */
    private fun onBindDirectory(holder: VH, file: FileListItem) {
        holder.binding.apply {
            icon.setImageDrawable(ContextCompat.getDrawable(context, fileIconProvider!!.getIconForFolder(file.item)))

            root.setOnClickListener {
                fileListEventListener?.onFolderClick(file.item)
            }

            root.setOnLongClickListener {
                fileListEventListener?.onFolderLongClick(file.item) ?: false
            }
        }
    }

    /**
     * Binds a file item to the ViewHolder.
     */
    private fun onBindFile(holder: VH, file: FileListItem) {
        holder.binding.apply {
            icon.setImageDrawable(ContextCompat.getDrawable(context, fileIconProvider!!.getIconForFile(file.item)))

            root.setOnClickListener {
                fileListEventListener?.onFileClick(file.item)
            }

            root.setOnLongClickListener {
                fileListEventListener?.onFileLongClick(file.item) ?: false
            }
        }
    }

    /**
     * Updates the RecyclerView when the file tree changes.
     */
    override fun onFileListUpdated(startPosition: Int, itemCount: Int) {
        notifyItemRangeChanged(startPosition, itemCount)
    }

    /**
     * Submits a new list to be displayed.
     */
    override fun submitList(list: MutableList<FileListItem>?) {
        super.submitList(list?.toList())
    }

    /**
     * DiffUtil callback to optimize RecyclerView updates.
     */
    class FileListDiffCallback : DiffUtil.ItemCallback<FileListItem>() {
        override fun areItemsTheSame(oldItem: FileListItem, newItem: FileListItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: FileListItem, newItem: FileListItem): Boolean {
            return oldItem.path == newItem.path
        }
    }
}