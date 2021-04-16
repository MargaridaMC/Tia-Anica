package net.teamtruta.tiaires.extensions

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import net.teamtruta.tiaires.R

class TriStatesCheckBox : AppCompatCheckBox {
    private var state = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
            context,
            attrs
    ) {
        init()
    }

    constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        state = INDETERMINATE
        updateBtn()
        setOnCheckedChangeListener { _, _ ->
            // checkbox status is changed from uncheck to checked.
            state = when (state) {
                INDETERMINATE -> CHECKED
                UNCHECKED -> INDETERMINATE
                CHECKED -> UNCHECKED
                else -> CHECKED
            }
            updateBtn()
        }
    }

    private fun updateBtn() {
        val btnDrawable: Int = when (state) {
            INDETERMINATE -> R.drawable.half_open_eye
            UNCHECKED -> R.drawable.closed_eye
            CHECKED -> R.drawable.open_eye
            else -> R.drawable.closed_eye
        }
        setButtonDrawable(btnDrawable)
    }

    fun getState(): Int {
        return state
    }

    companion object {
        const val UNCHECKED = 0
        const val INDETERMINATE = 1
        const val CHECKED = 2
    }
}