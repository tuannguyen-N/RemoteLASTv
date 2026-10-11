package com.tuannguyen.remotelastv.ui.common

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape

fun wedgeShape(centerAngle: Float, sweep: Float = 90f): Shape =
    GenericShape { size, _ ->
        moveTo(size.width / 2f, size.height / 2f)
        arcTo(
            rect = Rect(0f, 0f, size.width, size.height),
            startAngleDegrees = centerAngle - sweep / 2f,
            sweepAngleDegrees = sweep,
            forceMoveTo = false
        )
        close()
    }