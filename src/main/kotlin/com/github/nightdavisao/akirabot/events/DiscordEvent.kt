package com.github.nightdavisao.akirabot.events

import dev.kord.core.event.Event

interface DiscordEvent<in T: Event> {
    suspend fun executes(event: T)
}