package com.pixelisland.settings

/** Готовые наборы оформления: меняют вид и стиль всех событий, но не трогают, какие события включены. */
enum class Preset(val title: String, val hint: String) {
    MINIMAL("Минимал", "Узко, без подложек, белые иконки"),
    PIXEL("Pixel", "Цвета Monet, сквирклы, плавно"),
    NEON("Неон", "Свечение, яркие круги, пульс"),
    GLASS("Стекло", "Полупрозрачный фон, размытие"),
    CLASSIC("Классика", "Чёрный, упругий, как привыкли"),
    BLOOM("Цветение", "Формы-цветы, градиент, мягко"),
    ;

    fun apply(s: IslandSettings): IslandSettings {
        val base = when (this) {
            MINIMAL -> s.copy(sideWidthDp = 18f, pillPadDp = 2f, bg = BgChoice.BLACK, bgOpacity = 1f, gradient = GradientStyle.NONE,
                outline = false, shadow = false, glow = false, animStyle = AnimStyle.SNAPPY, appearStyle = AppearStyle.FROM_CAMERA)
            PIXEL -> s.copy(sideWidthDp = 30f, pillPadDp = 4f, bg = BgChoice.MONET_DARK, bgOpacity = 1f, gradient = GradientStyle.NONE,
                outline = false, shadow = true, glow = false, animStyle = AnimStyle.SMOOTH, appearStyle = AppearStyle.POP)
            NEON -> s.copy(sideWidthDp = 32f, pillPadDp = 4f, bg = BgChoice.BLACK, bgOpacity = 1f, gradient = GradientStyle.NONE,
                outline = true, shadow = false, glow = true, animStyle = AnimStyle.SPRING, appearStyle = AppearStyle.FROM_CAMERA)
            GLASS -> s.copy(sideWidthDp = 32f, pillPadDp = 5f, bg = BgChoice.MONET_DARK, bgOpacity = 0.7f, gradient = GradientStyle.NONE,
                outline = true, shadow = true, glow = false, blurBehind = true, animStyle = AnimStyle.SMOOTH, appearStyle = AppearStyle.FADE)
            CLASSIC -> s.copy(sideWidthDp = 30f, pillPadDp = 4f, bg = BgChoice.BLACK, bgOpacity = 1f, gradient = GradientStyle.NONE,
                outline = false, shadow = false, glow = false, animStyle = AnimStyle.JELLY, appearStyle = AppearStyle.FROM_CAMERA)
            BLOOM -> s.copy(sideWidthDp = 34f, pillPadDp = 5f, bg = BgChoice.MONET_DARK, bgOpacity = 1f, gradient = GradientStyle.ACCENT,
                outline = false, shadow = true, glow = false, animStyle = AnimStyle.SPRING, appearStyle = AppearStyle.POP)
        }
        val events = EventType.entries.associateWith { t ->
            val e = s.style(t)
            when (this) {
                MINIMAL -> e.copy(container = IconContainer.NONE, anim = IconAnim.STATIC, color = ColorChoice.MONO,
                    variant = IconVariant.OUTLINED, weight = TextWeight.MEDIUM, widthMode = WidthMode.AUTO, ring = RingStyle.BAR, ringThick = 2f)
                PIXEL -> e.copy(container = IconContainer.SQUIRCLE, anim = IconAnim.POP, color = ColorChoice.MONET,
                    variant = IconVariant.ROUNDED, weight = TextWeight.MEDIUM, ring = RingStyle.RING, ringThick = 3f)
                NEON -> e.copy(container = IconContainer.SOLID, anim = IconAnim.PULSE, color = ColorChoice.AUTO,
                    variant = IconVariant.SHARP, weight = TextWeight.BOLD, ring = RingStyle.RING, ringThick = 4f)
                GLASS -> e.copy(container = IconContainer.RING, anim = IconAnim.BLUR_IN, color = ColorChoice.AUTO,
                    variant = IconVariant.OUTLINED, weight = TextWeight.REGULAR, ring = RingStyle.FILL)
                CLASSIC -> e.copy(container = IconContainer.TONAL, anim = IconAnim.POP, color = ColorChoice.AUTO,
                    variant = IconVariant.ROUNDED, weight = TextWeight.MEDIUM, ring = RingStyle.RING, ringThick = 3f)
                BLOOM -> e.copy(container = IconContainer.FLOWER, anim = IconAnim.SPIN, color = ColorChoice.AUTO,
                    variant = IconVariant.ROUNDED, weight = TextWeight.BOLD, ring = RingStyle.DOTS)
            }
        }
        return base.copy(events = events)
    }
}
