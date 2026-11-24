package com.pixelisland.ui.content

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.pixelisland.settings.IconContainer

/** Официальная фигура Material 3 Expressive как Compose-Shape (фигуры нормализованы в квадрат 1×1). */
class PolygonShape(private val polygon: RoundedPolygon, private val rotation: Float = 0f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val p = polygon.toPath(android.graphics.Path())
        p.transform(matrix(size, rotation))
        return Outline.Generic(p.asComposePath())
    }
}

/** Перетекание одной фигуры в другую: progress 0 → первая, 1 → вторая. */
class MorphShape(private val morph: Morph, private val progress: Float, private val rotation: Float = 0f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val p = morph.toPath(progress, android.graphics.Path())
        p.transform(matrix(size, rotation))
        return Outline.Generic(p.asComposePath())
    }
}

private fun matrix(size: Size, rotation: Float) = android.graphics.Matrix().apply {
    setScale(size.width, size.height)
    if (rotation != 0f) postRotate(rotation, size.width / 2f, size.height / 2f)
}

/** Многоугольник для подложки, если она — фигура M3 (иначе null). */
fun IconContainer.polygon(): RoundedPolygon? = when (this) {
    IconContainer.COOKIE -> MaterialShapes.Cookie9Sided
    IconContainer.FLOWER -> MaterialShapes.Flower
    IconContainer.CLOVER -> MaterialShapes.Clover4Leaf
    IconContainer.SUNNY -> MaterialShapes.Sunny
    IconContainer.BURST -> MaterialShapes.SoftBurst
    IconContainer.GEM -> MaterialShapes.Gem
    IconContainer.PENTAGON -> MaterialShapes.Pentagon
    IconContainer.HEXAGON -> MaterialShapes.Cookie6Sided
    IconContainer.DIAMOND -> MaterialShapes.PuffyDiamond
    IconContainer.HEART -> MaterialShapes.Heart
    IconContainer.GHOST -> MaterialShapes.Ghostish
    IconContainer.PIXEL -> MaterialShapes.PixelCircle
    IconContainer.ARCH -> MaterialShapes.Arch
    IconContainer.PUFFY -> MaterialShapes.Puffy
    else -> null
}

private val polygonShapes = HashMap<IconContainer, Shape>()

fun IconContainer.shape(): Shape = when (this) {
    IconContainer.SQUIRCLE -> RoundedCornerShape(30)
    IconContainer.DROP -> RoundedCornerShape(50, 50, 50, 12)
    else -> polygon()?.let { pg -> polygonShapes.getOrPut(this) { PolygonShape(pg) } } ?: CircleShape
}

/** Морф «круг → подложка» для анимации «Перетекание». */
fun IconContainer.morphFromCircle(): Morph? =
    (polygon() ?: if (this == IconContainer.SQUIRCLE) MaterialShapes.Square else null)?.let { Morph(MaterialShapes.Circle, it) }
