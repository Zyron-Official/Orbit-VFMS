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
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.EdgeEffect
import android.widget.OverScroller
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import androidx.recyclerview.widget.SimpleItemAnimator
import com.zyron.orbit.R
import com.zyron.orbit.FileTree
import com.zyron.orbit.FileTreeAdapterUpdateListener
import com.zyron.orbit.adapters.FileTreeAdapter
import com.zyron.orbit.events.FileTreeEventListener
import com.zyron.orbit.map.ConcurrentFileMap
import com.zyron.orbit.providers.DefaultFileIconProvider
import com.zyron.orbit.providers.FileIconProvider
import com.zyron.orbit.scrollview.BipolarScrollView
import com.zyron.orbit.scrollview.DiagonalScrollView
import com.zyron.orbit.utils.ThreadUtils

enum class Animations {
    DEFAULT, FALLDOWN, ROTATE_IN, SCALE_IN, SCALE_UP
}

/**
 * FileTreeView extends RecyclerView to represent a file tree structure.
 * Handles initialization, layout, and interactions specific to a file tree.
 */
class FileTreeView : RecyclerView {

    private var filePath: String? = null
    private var  fileTree: FileTree? = null
    private var fileTreeAdapter: FileTreeAdapter? = null
    
    private var startBranchX: Float = 0f
    private var startBranchY: Float = 0f
    private var endBranchX: Float = 0f
    private var endBranchY: Float = 0f 
       
    public val FALLDOWN_ANIM: Int = 0
    public val ROTATE_IN_ANIM: Int = 1
    public val SCALE_UP_ANIM: Int = 2
    public val SCALE_IN_ANIM: Int = 3
    public val DEFAULT_ANIM: Int = 4    
    
    private var recyclerItemViewCount: Int = 200
    private var itemViewCacheSize: Int = 100
    private var fileMapMaxSize: Int = 150
    private var fileTreeAnimation: Int = 4
    private var animationResId: Int = 4
    private var animation: Int = 0
    
    public var isRecyclerItemViewEnabled: Boolean = true
    public var isItemViewCachingEnabled: Boolean = true
    public var isFileMapEnabled: Boolean = true
    public var isLayoutAnimationEnabled: Boolean = true
    public var isItemAnimatorEnabled: Boolean = true

    private val recycledViewPool: RecycledViewPool = RecycledViewPool()
    private val defaultFileIconProvider: DefaultFileIconProvider = DefaultFileIconProvider()

    constructor(context: Context) : super(context) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }

    /**
     * Initializes the FileTreeView with given attributes.
     *
     * @param context The context associated with the view.
     * @param attrs The attributes from the XML layout.
     */
    private fun initialize(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val attribute = context.obtainStyledAttributes(it, R.styleable.FileTreeView)
            recyclerItemViewCount = attribute.getInteger(R.styleable.FileTreeView_recyclerItemViewCount, 200)
            isRecyclerItemViewEnabled = attribute.getBoolean(R.styleable.FileTreeView_recyclerItemViewEnabled, true)
            itemViewCacheSize = attribute.getInteger(R.styleable.FileTreeView_itemViewCacheSize, 100) 
            isItemViewCachingEnabled = attribute.getBoolean(R.styleable.FileTreeView_itemViewCachingEnabled, true)
            fileMapMaxSize = attribute.getInteger(R.styleable.FileTreeView_fileMapMaxSize, 150) 
            isFileMapEnabled = attribute.getBoolean(R.styleable.FileTreeView_fileMapEnabled, true)
            fileTreeAnimation = attribute.getInteger(R.styleable.FileTreeView_fileTreeAnimation, 4) 
            isLayoutAnimationEnabled = attribute.getBoolean(R.styleable.FileTreeView_fileTreeAnimationEnabled, true)
            attribute.recycle()
        }
        this.requestLayout()
        this.setAnimation(fileTreeAnimation)
    }

    /**
     * Sets the animation for the FileTreeView based on the provided animation type.
     *
     * @param fileTreeAnimation The type of animation to use.
     */
    fun setAnimation(fileTreeAnimation: Int) {
        animationResId = when (fileTreeAnimation) {
            0 -> R.anim.fall_down_animation
            1 -> R.anim.rotate_in_animation
            2 -> R.anim.scale_up_animation
            3 -> R.anim.slide_in_animation
            else -> R.anim.default_animation
        }
    }
    
    fun setRecursiveExpansionEnabled(state: Boolean) = fileTree?.setRecursiveExpansionEnabled(state) ?: false

    fun setRecursiveContractionEnabled(state: Boolean) = fileTree?.setRecursiveContractionEnabled(state) ?: false

    fun isRecyclerItemViewEnabled(state: Boolean) {
        this.isRecyclerItemViewEnabled = state
    }
        
    fun isItemViewCachingEnabled(state: Boolean) {
        this.isItemViewCachingEnabled = state
    }
    
    fun isFileMapEnabled(state: Boolean) {
        this.isFileMapEnabled = state
    }
        
    fun isLayoutAnimationEnabled(state: Boolean) {
        this.isLayoutAnimationEnabled = state
    }
        
    fun isItemAnimatorEnabled(state: Boolean) {
        this.isItemAnimatorEnabled = state
    }
    
    /**
     * Initializes the FileTree with a given path and optional listeners.
     *
     * @param path The root path for the file tree.
     * @param fileTreeEventListener Optional listener for file tree events.
     * @param fileTreeIconProvider Optional provider for custom file tree icons.
     */
    fun initializeFileTree(path: String, fileTreeEventListener: FileTreeEventListener? = null, fileIconProvider: FileIconProvider? = null) {
        filePath = path
        fileTree = FileTree(context, filePath!!)
        fileTreeAdapter = when {
            fileIconProvider == null && fileTreeEventListener == null -> FileTreeAdapter(context, fileTree!!)
            fileIconProvider != null && fileTreeEventListener != null -> FileTreeAdapter(context, fileTree!!, fileIconProvider, fileTreeEventListener)
            fileIconProvider != null && fileTreeEventListener == null -> FileTreeAdapter(context, fileTree!!, fileIconProvider, null)
            fileIconProvider == null && fileTreeEventListener != null -> FileTreeAdapter(context, fileTree!!, defaultFileIconProvider, fileTreeEventListener)
            else -> FileTreeAdapter(context, fileTree!!, defaultFileIconProvider, null)
        }
        
        this.adapter = fileTreeAdapter
        this.layoutManager = LinearLayoutManager(context)
        
        if (isRecyclerItemViewEnabled) {
            this.setRecycledViewPool(recycledViewPool)
            recycledViewPool.setMaxRecycledViews(0, recyclerItemViewCount)
        }
        
        if (isItemViewCachingEnabled) {
            this.setItemViewCacheSize(itemViewCacheSize)
        }        

        if (isFileMapEnabled) {
            val concurrentFileMap = ConcurrentFileMap(fileTree!!.getItems(), fileMapMaxSize)
            Thread(concurrentFileMap).start()
        }

        if (isLayoutAnimationEnabled) {
            val animation = AnimationUtils.loadLayoutAnimation(context, animationResId)
            this.layoutAnimation = animation
        }

        if (isItemAnimatorEnabled) (this.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = true
        else this.itemAnimator = null

        fileTree?.setAdapterUpdateListener(object : FileTreeAdapterUpdateListener {
            override fun onFileTreeUpdated(startPosition: Int, itemCount: Int) {
                fileTreeEventListener?.onFileTreeViewUpdated(startPosition, itemCount)
                ThreadUtils.runOnUiThread {
                    fileTreeAdapter?.submitList(fileTree!!.getItems().toMutableList())
                }
            }
        })
    }
}