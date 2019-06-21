package org.rionlabs.blubot.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rionlabs.blubot.R
import kotlin.math.roundToInt

class DeviceItemDecoration(context: Context) :
    RecyclerView.ItemDecoration() {

    private val drawable = ContextCompat.getDrawable(context, R.drawable.device_item_decoration)!!

    private val startPadding =
        context.resources.getDimension(R.dimen.device_item_divider_start_padding)

    private val endPadding = context.resources.getDimension(R.dimen.device_item_divider_end_padding)

    private val bounds = Rect()

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        canvas.save()

        var left: Int
        var right: Int

        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(
                left, parent.paddingTop, right,
                parent.height - parent.paddingBottom
            )
        } else {
            left = 0
            right = parent.width
        }

        left += startPadding.roundToInt()
        right -= endPadding.roundToInt()

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + Math.round(child.translationY)
            val top = bottom - drawable.intrinsicHeight

            drawable.setBounds(left, top, right, bottom)
            drawable.draw(canvas)
        }
        canvas.restore()
    }
}