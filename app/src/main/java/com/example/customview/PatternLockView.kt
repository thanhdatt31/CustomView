package com.example.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import androidx.core.content.ContextCompat

class PatternLockView : GridLayout {
    companion object {
        const val DEFAULT_RADIUS_RATIO = 0.3f
        const val DEFAULT_LINE_WIDTH = 2f // unit: dp
        const val DEFAULT_SPACING = 24f // unit: dp
        const val DEFAULT_ROW_COUNT = 3
        const val DEFAULT_COLUMN_COUNT = 3
        const val DEFAULT_ERROR_DURATION = 400 // unit: ms
        const val DEFAULT_HIT_AREA_PADDING_RATIO = 0.2f
        const val DEFAULT_INDICATOR_SIZE_RATIO = 0.2f
    }

    private var regularCellBackground: Drawable? = null
    private var regularDotColor: Int = 0
    private var regularDotRadiusRatio: Float = 0f

    private var selectedCellBackground: Drawable? = null
    private var selectedDotColor: Int = 0
    private var selectedDotRadiusRatio: Float = 0f

    private var errorCellBackground: Drawable? = null
    private var errorDotColor: Int = 0
    private var errorDotRadiusRatio: Float = 0f

    private var lineStyle: Int = 0

    private var lineWidth: Int = 0
    private var regularLineColor: Int = 0
    private var errorLineColor: Int = 0

    private var spacing: Int = 0

    private var plvRowCount: Int = 0
    private var plvColumnCount: Int = 0

    private var errorDuration: Int = 0
    private var hitAreaPaddingRatio: Float = 0f
    private var indicatorSizeRatio: Float = 0f

    private var cells: ArrayList<Cell> = ArrayList()
    private var selectedCells: ArrayList<Cell> = ArrayList()

    private var linePaint: Paint = Paint()
    private var linePath: Path = Path()

    private var lastX: Float = 0f
    private var lastY: Float = 0f

    private var isSecureMode = false

    private var onPatternListener: OnPatternListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.PatternLockView)
        regularCellBackground =
            ta.getDrawable(R.styleable.PatternLockView_plv_regularCellBackground)
        regularDotColor = ta.getColor(
            R.styleable.PatternLockView_plv_regularDotColor,
            ContextCompat.getColor(context, R.color.regularColor)
        )
        regularDotRadiusRatio =
            ta.getFloat(R.styleable.PatternLockView_plv_regularDotRadiusRatio, DEFAULT_RADIUS_RATIO)

        selectedCellBackground =
            ta.getDrawable(R.styleable.PatternLockView_plv_selectedCellBackground)
        selectedDotColor = ta.getColor(
            R.styleable.PatternLockView_plv_selectedDotColor,
            ContextCompat.getColor(context, R.color.selectedColor)
        )
        selectedDotRadiusRatio = ta.getFloat(
            R.styleable.PatternLockView_plv_selectedDotRadiusRatio,
            DEFAULT_RADIUS_RATIO
        )

        errorCellBackground = ta.getDrawable(R.styleable.PatternLockView_plv_errorCellBackground)
        errorDotColor = ta.getColor(
            R.styleable.PatternLockView_plv_errorDotColor,
            ContextCompat.getColor(context, R.color.errorColor)
        )
        errorDotRadiusRatio =
            ta.getFloat(R.styleable.PatternLockView_plv_errorDotRadiusRatio, DEFAULT_RADIUS_RATIO)

        lineStyle = ta.getInt(R.styleable.PatternLockView_plv_lineStyle, 1)
        lineWidth = ta.getDimensionPixelSize(
            R.styleable.PatternLockView_plv_lineWidth,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_LINE_WIDTH,
                context.resources.displayMetrics
            ).toInt()
        )
        regularLineColor = ta.getColor(
            R.styleable.PatternLockView_plv_regularLineColor,
            ContextCompat.getColor(context, R.color.selectedColor)
        )
        errorLineColor = ta.getColor(
            R.styleable.PatternLockView_plv_errorLineColor,
            ContextCompat.getColor(context, R.color.errorColor)
        )

        spacing = ta.getDimensionPixelSize(
            R.styleable.PatternLockView_plv_spacing,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                DEFAULT_SPACING,
                context.resources.displayMetrics
            ).toInt()
        )

        plvRowCount = ta.getInteger(R.styleable.PatternLockView_plv_rowCount, DEFAULT_ROW_COUNT)
        plvColumnCount =
            ta.getInteger(R.styleable.PatternLockView_plv_columnCount, DEFAULT_COLUMN_COUNT)

        errorDuration =
            ta.getInteger(R.styleable.PatternLockView_plv_errorDuration, DEFAULT_ERROR_DURATION)
        hitAreaPaddingRatio = ta.getFloat(
            R.styleable.PatternLockView_plv_hitAreaPaddingRatio,
            DEFAULT_HIT_AREA_PADDING_RATIO
        )
        indicatorSizeRatio = ta.getFloat(
            R.styleable.PatternLockView_plv_indicatorSizeRatio,
            DEFAULT_INDICATOR_SIZE_RATIO
        )
        ta.recycle()

        rowCount = plvRowCount
        columnCount = plvColumnCount

        setupCells()
        initPathPaint()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val hitCell = getHitCell(event.x.toInt(), event.y.toInt())
                if (hitCell == null) {
                    return false
                } else {
                    onPatternListener?.onStarted()
                    notifyCellSelected(hitCell)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                handleActionMove(event)
            }

            MotionEvent.ACTION_UP -> onFinish()

            MotionEvent.ACTION_CANCEL -> reset()

            else -> return false
        }
        return true
    }

    private fun onFinish() {
        lastX = 0f
        lastY = 0f

        val isCorrect = onPatternListener?.onComplete(generateSelectedIds())
        if (isCorrect != null && isCorrect) {
            reset()
        } else {
            onError()
        }
    }


    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (isSecureMode) return
        canvas?.drawPath(linePath, linePaint)
        if (selectedCells.size > 0 && lastX > 0 && lastY > 0) {
            val center = selectedCells[selectedCells.size - 1].getCenter()
            canvas?.drawLine(center.x.toFloat(), center.y.toFloat(), lastX, lastY, linePaint)
        }
    }

    private fun onError() {
        if (isSecureMode) {
            reset()
            return
        }
        for (cell in selectedCells) {
            cell.setState(State.ERROR)
        }
        linePaint.color = errorLineColor
        invalidate()

        postDelayed({
            reset()
        }, errorDuration.toLong())

    }

    private fun reset() {
        for (cell in selectedCells) {
            cell.reset()
        }
        selectedCells.clear()
        linePaint.color = regularLineColor
        linePath.reset()

        lastX = 0f
        lastY = 0f

        invalidate()
    }

    private fun handleActionMove(event: MotionEvent) {
        val hitCell = getHitCell(event.x.toInt(), event.y.toInt())
        if (hitCell != null) {
            if (!selectedCells.contains(hitCell)) {
                notifyCellSelected(hitCell)
            }
        }

        lastX = event.x
        lastY = event.y

        invalidate()
    }


    private fun getHitCell(x: Int, y: Int): Cell? {
        for (cell in cells) {
            if (isSelected(cell, x, y)) {
                return cell
            }
        }
        return null
    }

    private fun notifyCellSelected(cell: Cell) {
        selectedCells.add(cell)
        onPatternListener?.onProgress(generateSelectedIds())
        if (isSecureMode) return
        cell.setState(State.SELECTED)
        val center = cell.getCenter()
        if (selectedCells.size == 1) {
            linePath.moveTo(center.x.toFloat(), center.y.toFloat())
        } else {
            linePath.lineTo(center.x.toFloat(), center.y.toFloat())
        }
    }

    private fun isSelected(view: View, x: Int, y: Int): Boolean {
        val innerPadding = view.width * hitAreaPaddingRatio
        return x >= view.left + innerPadding && x <= view.right - innerPadding &&
                y >= view.top + innerPadding && y <= view.bottom - innerPadding
    }

    private fun generateSelectedIds(): ArrayList<Int> {
        val result = ArrayList<Int>()
        for (cell in selectedCells) {
            result.add(cell.index)
        }
        return result
    }

    private fun initPathPaint() {
        linePaint.isAntiAlias = true
        linePaint.isDither = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeWidth = lineWidth.toFloat()
        linePaint.color = regularLineColor
    }

    private fun setupCells() {
        for (i in 0 until plvRowCount) {
            for (j in 0 until plvColumnCount) {
                val cell = Cell(
                    context, i * plvColumnCount + j,
                    regularCellBackground, regularDotColor, regularDotRadiusRatio,
                    selectedCellBackground, selectedDotColor, selectedDotRadiusRatio,
                    errorCellBackground, errorDotColor, errorDotRadiusRatio,
                    lineStyle, regularLineColor, errorLineColor, plvColumnCount
                )
                val cellPadding = spacing
                cell.setPadding(cellPadding, cellPadding, cellPadding, cellPadding)
                addView(cell)
                cells.add(cell)
            }
        }
    }


    fun setOnPatternListener(listener: OnPatternListener) {
        onPatternListener = listener
    }

    interface OnPatternListener {
        fun onStarted() {}
        fun onProgress(ids: ArrayList<Int>) {}
        fun onComplete(ids: ArrayList<Int>): Boolean
    }
}