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
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class InterceptableDrawerLayout : DrawerLayout {

    private var rect = Rect()
    private var childId: Int = -1
    private var translationBehaviorStart: TranslationBehavior = TranslationBehavior.DEFAULT
    private var translationBehaviorEnd: TranslationBehavior = TranslationBehavior.DEFAULT

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    private fun initialize() {
        addDrawerListener(drawerListener)
    }

    private val drawerListener = object : SimpleDrawerListener() {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            if (childId == -1) return

            val gravity = (drawerView.layoutParams as LayoutParams).gravity
            val view = findViewById<View>(childId) ?: return
            val (direction, maxOffset) = if (gravity == GravityCompat.START) {
                1 to translationBehaviorStart.maxOffset
            } else {
                -1 to translationBehaviorEnd.maxOffset
            }

            val offset = (drawerView.width * slideOffset) * maxOffset
            view.translationX = direction * offset
        }
    }

    enum class TranslationBehavior(val maxOffset: Float) {
        DEFAULT(0.25f),
        FULL(0.95f)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val child = findScrollingChild(this, event.x, event.y)
        return child == null && super.onInterceptTouchEvent(event)
    }

    private fun findScrollingChild(parent: ViewGroup, x: Float, y: Float): View? {
        val childCount = parent.childCount
        if (parent === this && childCount <= 1) return null

        var startIndex = 0
        if (parent === this) startIndex = 1

        for (i in startIndex until childCount) {
            val child = parent.getChildAt(i)
            if (child.visibility != View.VISIBLE) continue

            child.getHitRect(rect)
            if (!rect.contains(x.toInt(), y.toInt())) continue

            if (child.canScrollHorizontally(-1) || child.canScrollHorizontally(1)) return child

            if (child is ViewGroup) {
                val scrollingChild = findScrollingChild(child, x - rect.left, y - rect.top)
                if (scrollingChild != null) return scrollingChild
            }
        }
        return null
    }
}