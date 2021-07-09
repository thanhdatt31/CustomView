package com.example.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View

internal class Cell(
    context: Context,
    var index: Int,
    private var regularCellBackground: Drawable?,
    private var regularDotColor: Int,
    private var regularDotRadiusRatio: Float,
    private var selectedCellBackground: Drawable?,
    private var selectedDotColor: Int,
    private var selectedDotRadiusRatio: Float,
    private var errorCellBackground: Drawable?,
    private var errorDotColor: Int,
    private var errorDotRadiusRatio: Float,
    private var lineStyle: Int,
    private var regularLineColor: Int,
    private var errorLineColor: Int,
    private var columnCount: Int,
) : View(context) {
    private var currentState: State = State.REGULAR
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentDegree: Float = -1f
    private var indicatorPath: Path = Path()
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cellWidth = MeasureSpec.getSize(widthMeasureSpec) / columnCount
        setMeasuredDimension(cellWidth, cellWidth)
    }

    override fun onDraw(canvas: Canvas?) {
        when(currentState) {
            State.REGULAR -> drawDot(canvas, regularCellBackground, regularDotColor, regularDotRadiusRatio)
            State.SELECTED -> drawDot(canvas, selectedCellBackground, selectedDotColor, selectedDotRadiusRatio)
            State.ERROR -> drawDot(canvas, errorCellBackground, errorDotColor, errorDotRadiusRatio)
        }
    }

    private fun drawDot(
        canvas: Canvas?,
        background: Drawable?,
        dotColor: Int,
        radiusRation: Float
    ) {
        val radius = getRadius()
        val centerX = width / 2
        val centerY = height / 2
        if (background is ColorDrawable) {
            paint.color = background.color
            paint.style = Paint.Style.STROKE
            canvas?.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), paint)
        } else {
            background?.setBounds(
                paddingLeft,
                paddingTop,
                width - paddingRight,
                height - paddingBottom
            )
            background?.draw(canvas!!)
        }

        paint.color = dotColor
        paint.style = Paint.Style.STROKE
        canvas?.drawCircle(centerX.toFloat(), centerY.toFloat(), radius * radiusRation, paint)


    }

    fun getRadius(): Int {
        return (width.coerceAtMost(height) - (paddingLeft + paddingRight)) / 2
    }

    fun getCenter(): Point {
        var point = Point()
        point.x = left + (right - left) / 2
        point.y = top + (bottom - top) / 2
        return point
    }

    fun setState(state: State) {
        currentState = state
        invalidate()
    }

    fun setDegree(degree: Float) {
        currentDegree = degree
    }

    fun reset() {
        setState(State.REGULAR)
        currentDegree = -1f
    }
}