package com.github.nightdavisao.akirabot.utils.emote

interface EmoteManager {
    companion object {
        const val FALLBACK_EMOJI = "\uD83D\uDC1B"
    }

    fun getEmoteByName(name: String): String = FALLBACK_EMOJI
}