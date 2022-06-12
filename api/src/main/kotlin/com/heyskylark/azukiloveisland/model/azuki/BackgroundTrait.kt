package com.heyskylark.azukiloveisland.model.azuki

enum class BackgroundTrait(val text: String) {
    OFF_WHITE_D("Off White D"),
    OFF_WHITE_C("Off White C"),
    OFF_WHITE_B("Off White B"),
    OFF_WHITE_A("Off White A"),
    RED("Red"),
    DARK_BLUE("Dark Blue"),
    COOL_GRAY("Cool Gray"),
    DARK_PURPLE("Dark Purple")
    ;

    companion object {
        private val TEXT_LOOKUP = values().associateBy(BackgroundTrait::text)

        fun fromText(text: String): BackgroundTrait {
            return TEXT_LOOKUP[text]
                ?: throw IllegalArgumentException("Unsupported background string for background trait: $text")
        }

        fun fromTextOrNull(text: String): BackgroundTrait? {
            return TEXT_LOOKUP[text]
        }
    }
}