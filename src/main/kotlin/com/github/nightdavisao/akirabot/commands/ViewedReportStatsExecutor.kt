package com.github.nightdavisao.akirabot.commands

import com.github.nightdavisao.akirabot.AkiraMiscConfig
import com.github.nightdavisao.akirabot.commands.ViewedReportStatsExecutor.Companion.Options.filter
import com.github.nightdavisao.akirabot.dao.schemas.UserReport
import com.github.nightdavisao.akirabot.tasks.UserReportCatcherTask
import com.github.nightdavisao.akirabot.utils.emote.Emotes
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.concurrent.*

class ViewedReportStatsExecutor(private val client: Kord,
                                private val database: Database,
                                emotes: Emotes, miscConfig: AkiraMiscConfig) :
    AkiraCommand(onlyStaff = true, config = miscConfig) {
    private val trophyEmoji = emotes.trophy
    private val approvedEmoji = emotes.approved
    private val deniedEmoji = emotes.denied

    private val logger = KotlinLogging.logger { }
    private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale("pt", "BR"))
        .withZone(ZoneId.of("America/Sao_Paulo"))

    companion object : SlashCommandExecutorDeclaration(ViewedReportStatsExecutor::class) {
        object Options : CommandOptions() {
            val filter = optionalString("filter", "Filtrar por datas")
                .choice("week", "Nesta semana")
                .choice("month", "Neste mês")
                .choice("sevenDays", "Últimos 7 dias")
                .choice("thirtyDays", "Últimos 30 dias")
                .choice("year", "Neste ano")
                .register()
        }

        override val options: CommandOptions = Options
    }

    override suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments) {
        context.defer()
        val dateFilter = args[filter]
        val localDateTime = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))

        val firstDay = localDateTime.with(LocalTime.MIN).let {
            when (dateFilter) {
                "week" -> it.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                "month" -> it.with(TemporalAdjusters.firstDayOfMonth())
                "sevenDays" -> it.minusDays(7)
                "thirtyDays" -> it.minusDays(30)
                "year" -> it.with(TemporalAdjusters.firstDayOfYear())
                else -> null
            }
        }

        val userReports = transaction(database) {
            if (firstDay == null) {
                UserReport.selectAll()
            } else {
                UserReport.select {
                    UserReport.reportedAt greater firstDay.toInstant(ZoneOffset.MIN).toEpochMilli()
                }
            }
        }


        val mutableMap = mutableMapOf<Long, Pair<Int, Int>>()

        val message = buildString {
            if (firstDay != null)
                append("Começo: ${formatter.format(firstDay.toInstant(ZoneOffset.MIN))}\n\n")

            transaction(database) {
                userReports.filterNotNull().forEach {
                    val viewedBy = it[UserReport.viewedBy]
                    if (viewedBy != null) {
                        val first = mutableMap[viewedBy]?.first ?: 0
                        val second = mutableMap[viewedBy]?.second ?: 0

                        val approved = it[UserReport.approved]
                        val denied = it[UserReport.denied]

                        mutableMap[viewedBy] = Pair(
                            first + if (approved == true) 1 else 0,
                            second + if (denied == true) 1 else 0
                        )
                    }
                }
            }

            var position = 1
            mutableMap.toList()
                .sortedByDescending { (_, value) -> value.first + value.second }
                .toMap()
                .forEach { (key, value) ->
                    val user = client.getUser(Snowflake(key))
                    this.append("${position}. `${user?.tag ?: key}` - ${value.first + value.second} (${approvedEmoji} ${value.first}/${deniedEmoji} ${value.second})\n")
                    position += 1
                }
        }

        context.sendMessage {
            content = " "
            embed {
                body {
                    title = "$trophyEmoji Ranking de \"quem viu as denúncias\""
                    description = message
                    color = Color(255, 255, 0)
                }
                footer("Total de denúncias: ${mutableMap.values.sumBy { it.first + it.second }}") {}
            }
        }
    }
}

class UpdateReportStatsExecutor(
    private val executors: ExecutorService,
    private val userReportCatcherTask: UserReportCatcherTask,
    config: AkiraMiscConfig
) :
    AkiraCommand(onlyOwner = true, config = config) {
    companion object : SlashCommandExecutorDeclaration(UpdateReportStatsExecutor::class)

    override suspend fun executesAkira(context: SlashCommandContext, args: SlashCommandArguments) {
        context.sendMessage {
            content = "Atualizando a database..."
        }

        executors.submit(userReportCatcherTask)
    }
}