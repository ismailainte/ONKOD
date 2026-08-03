package com.onkod.keyboard.ime

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable

class KeyDrawable(normalColor: Int, pressedColor: Int, radius: Float) : StateListDrawable() {
    init {
        addState(intArrayOf(android.R.attr.state_pressed), shape(pressedColor, radius))
        addState(intArrayOf(), shape(normalColor, radius))
    }

    private fun shape(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
}
