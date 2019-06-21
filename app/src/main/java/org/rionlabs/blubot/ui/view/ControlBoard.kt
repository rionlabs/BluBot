package org.rionlabs.blubot.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.rionlabs.blubot.databinding.ControlBoardBinding

class ControlBoard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var onClickListener: OnClickListener? = null

    val binding: ControlBoardBinding =
        ControlBoardBinding.inflate(LayoutInflater.from(context), this, false)

    init {
        binding.root.apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addView(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.apply {
            // Should be exact math with enum Button values
            arrayOf(
                buttonA,
                buttonB,
                buttonC,
                buttonD,
                buttonUp,
                buttonDown,
                buttonLeft,
                buttonRight,
                buttonClose
            ).zip(Button.values()).map { pair ->
                pair.first.setOnClickListener {
                    onClickListener?.onButtonClicked(pair.second)
                }
            }
        }
    }

    fun setOnButtonClickListener(block: ((Button) -> Unit)) {
        onClickListener = object : OnClickListener {
            override fun onButtonClicked(button: Button) {
                block(button)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onClickListener = null
    }

    interface OnClickListener {
        fun onButtonClicked(button: Button)
    }

    enum class Button(val signal: Int) {
        A(0xA),
        B(0xB),
        C(0xC),
        D(0xD),
        UP(0x1),
        DOWN(0x2),
        LEFT(0x3),
        RIGHT(0x4),
        CLOSE(0x0)
    }
}