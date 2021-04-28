package com.github.nightdavisao.akirabot.utils.emote

import com.github.nightdavisao.akirabot.utils.emote.EmoteManager
import com.grack.nanojson.JsonParser

class EmoteManagerImpl(private val file: String): EmoteManager {
    override fun getEmoteByName(name: String): String {
        val json = JsonParser.`object`().from(file)

        return json.getString(name)
    }
}