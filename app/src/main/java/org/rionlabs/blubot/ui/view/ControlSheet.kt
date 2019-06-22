package org.rionlabs.blubot.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.rionlabs.blubot.databinding.ViewControlSheetBinding

class ControlSheet @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding = ViewControlSheetBinding.inflate(LayoutInflater.from(context)).apply {
        root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(root)
    }
}