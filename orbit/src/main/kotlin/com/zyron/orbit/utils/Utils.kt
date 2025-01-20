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

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

object Utils {

    /**
     * Retrieves the color value associated with a given attribute resource from the current theme.
     *
     * @param context The context used to access the current theme.
     * @param attr The attribute resource ID for which to retrieve the color.
     * @return The color value associated with the attribute, as defined in the current theme.
     */
    @ColorInt
    fun getColorAttr(context: Context, @AttrRes attr: Int): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attr))
        val colorStateList = typedArray.getColorStateList(0)
        val color = colorStateList?.defaultColor ?: 0
        typedArray.recycle()
        return color
    }
}