package com.github.mech.npm

import android.content.Context
import com.aliucord.Http
import com.aliucord.Http.QueryBuilder
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.discord.api.commands.ApplicationCommandType
import java.io.IOException
import org.json.JSONObject
import java.lang.StringBuilder

private const val baseUrl = "https://api.npms.io/v2"

@AliucordPlugin
class npm : Plugin() {
    override fun start(context: Context) {
        val arguments = listOf(
            Utils.createCommandOption(
                ApplicationCommandType.STRING,
                "package",
                "The package to find",
                required = true,
                default = true
            ),
            Utils.createCommandOption(
                ApplicationCommandType.BOOLEAN,
                "send",
                "Should the message be visible for everyone",
            )
        )

        commands.registerCommand("npm", "Find an npm package", arguments) { ctx ->
            val pkg = ctx.getRequiredString("package")
            var send = ctx.getBoolOrDefault("send", false)
            var msg: String
            try {
                val res = Http.Request(QueryBuilder("${baseUrl}/search").append("size", "1").append("q", pkg).toString(), "GET").execute().text()
                msg = JSONObject(res).toString(2)
            } catch (t: IOException) {
                msg = "Error: " + t.message
                send = false
            }

            CommandResult("```json\n${msg}```", null, send, "NPM", "https://static.npmjs.com/58a19602036db1daee0d7863c94673a4.png")
        }
    }

    override fun stop(context: Context) {
        // Unregister our commands
        commands.unregisterAll()
    }
}