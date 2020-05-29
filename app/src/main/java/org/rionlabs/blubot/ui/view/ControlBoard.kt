package org.rionlabs.blubot.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import org.rionlabs.blubot.databinding.ControlBoardBinding

/**
 * View representing control board.
 * Just a view controlled by [ControlSheet].
 */
class ControlBoard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var onClickListener: OnClickListener? = null

    private val binding: ControlBoardBinding =
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
            // Should be exact match with enum Button values
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

    @Suppress("unused")
    enum class Button(val signal: String) {
        A("0x0A"),
        B("0x0B"),
        C("0x0C"),
        D("0x0D"),
        UP("0x01"),
        DOWN("0x02"),
        LEFT("0x03"),
        RIGHT("0x04"),
        CLOSE("0x00")
    }
}