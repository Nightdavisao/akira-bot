package com.github.nightdavisao.akirabot.dao.schemas

import org.jetbrains.exposed.dao.id.IntIdTable

object ServerJoinedUser: IntIdTable() {
    val userId = long("userId")
    val timestamp = long("timestamp")
}