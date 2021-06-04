package net.teamtruta.tiaires.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import net.teamtruta.tiaires.R

class TriStatesCheckBox : AppCompatCheckBox {
    private var state = 0
    private var indeterminateStateDrawableID: Int = R.drawable.half_open_eye
    private var checkedStateDrawableID: Int = R.drawable.open_eye
    private var uncheckedStateDrawableID: Int = R.drawable.closed_eye

    constructor(context: Context)
            : super(context) {
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

    fun setCheckboxDrawables(indeterminateStateDrawableID: Int,
                             checkedStateDrawableID: Int, uncheckedStateDrawableID: Int){
        this.indeterminateStateDrawableID = indeterminateStateDrawableID
        this.checkedStateDrawableID = checkedStateDrawableID
        this.uncheckedStateDrawableID = uncheckedStateDrawableID
        updateBtn()
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
            INDETERMINATE -> indeterminateStateDrawableID
            UNCHECKED -> uncheckedStateDrawableID
            CHECKED -> checkedStateDrawableID
            else -> uncheckedStateDrawableID
        }
        setButtonDrawable(btnDrawable)
    }

    fun getState(): Int {
        return state
    }

    fun setState(state : Int){
        this.state = state
        updateBtn()
    }

    companion object {
        const val UNCHECKED = 0
        const val INDETERMINATE = 1
        const val CHECKED = 2
    }
}