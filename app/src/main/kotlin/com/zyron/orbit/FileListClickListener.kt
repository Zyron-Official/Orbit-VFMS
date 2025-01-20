package com.zyron.orbit

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.zyron.orbit.events.FileListEventListener
import java.io.File

class FileListClickListener(private val context: Context) : FileListEventListener {

    override fun onFileClick(file: File) {
        Toast.makeText(context, "File clicked: ${file.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onFolderClick(folder: File) {
        Toast.makeText(context, "Folder clicked: ${folder.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onFileLongClick(file: File): Boolean {
        Toast.makeText(context, "File long-clicked: ${file.name}", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onFolderLongClick(folder: File): Boolean {
        Toast.makeText(context, "Folder long-clicked: ${folder.name}", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onFileListViewUpdated(startPosition: Int, itemCount: Int) {
        Log.d("FileOperationExecutor", "FileTreeView has been updated.")
    }
}