package com.moehoemar.storyapp.views.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.moehoemar.storyapp.R

@SuppressLint("ClickableViewAccessibility")
class MyButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : AppCompatButton(context, attrs) {
    private var enabledBackground: Drawable =
        ContextCompat.getDrawable(context, R.drawable.bg_button) as Drawable
    private var disabledBackground: Drawable =
        ContextCompat.getDrawable(context, R.drawable.bg_button_disabled) as Drawable
    private val set = AnimatorSet()

    init {
        isClickable = true
        isFocusable = true
        isSoundEffectsEnabled = true

        setOnTouchListener { v, event ->
            when (event.action) {
                ACTION_DOWN -> {
                    animatePressed()
                }

                ACTION_UP -> {
                    animateReleased()
                    playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    v.performClick()
                }
            }
            false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        background = if (isEnabled) enabledBackground else disabledBackground
        textSize = 12f
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
    }

    private fun animatePressed() {
        val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, 0.9f).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, 0.9f).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
        }
        set.playTogether(scaleX, scaleY)
        set.start()
    }

    private fun animateReleased() {
        val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 0.9f, 1f).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0.9f, 1f).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
        }
        set.playTogether(scaleX, scaleY)
        set.start()
    }


}