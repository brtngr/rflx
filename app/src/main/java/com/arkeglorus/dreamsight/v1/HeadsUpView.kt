package com.arkeglorus.dreamsight.v1

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.arkeglorus.dreamsight.R


/**
 * TODO: document your custom view class.
 */
class HeadsUpView : View {

    private var _guideRadius: Float = 0f
    private var _guideCenterX: Float = 0f
    private var _guideCenterY: Float = 0f

    private var guideMaskPaint: Paint? = null
    private val xfermode: PorterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)


    /**
     * Radius of the guide
     * */
    var guideRadius: Float
        get() = _guideRadius
        set(value) {
            _guideRadius = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * Vertical Center of the guide
     * */
    var guideCenterX: Float
        get() = _guideCenterX
        set(value) {
            _guideCenterX = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * Horizontal Center of the guide
     * */
    var guideCenterY: Float
        get() = _guideCenterY
        set(value) {
            _guideCenterY = value
            invalidateTextPaintAndMeasurements()
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.HeadsUpView, defStyle, 0
        )

        a.recycle()

        // Set up a default Paint object
        guideMaskPaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
        }

        setLayerType(LAYER_TYPE_SOFTWARE, null)

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        postInvalidate()
    }

    fun convertDpToPixel(dp: Float): Float {
        val metrics = Resources.getSystem().displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return Math.round(px).toFloat()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        guideMaskPaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
        }

        // Draw background
        guideMaskPaint?.let {
            it.color = Color.BLACK
            it.style = Paint.Style.FILL
            canvas.drawPaint(it)
        }


        // Draw outline
        guideMaskPaint?.let {
            it.color = Color.GREEN
            canvas.drawCircle(_guideCenterX, _guideCenterY, _guideRadius + convertDpToPixel(4f), it)
        }

        guideMaskPaint?.let {
            it.xfermode = xfermode
            canvas.drawCircle(_guideCenterX, _guideCenterY, _guideRadius, it)
        }


    }
}
