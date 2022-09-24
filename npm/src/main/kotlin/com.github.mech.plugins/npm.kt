package com.github.mech.plugins

import android.content.Context
import com.aliucord.Http
import com.aliucord.Http.QueryBuilder
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.MessageEmbedBuilder
import com.aliucord.entities.Plugin
import com.discord.api.commands.ApplicationCommandType
import com.discord.api.message.embed.MessageEmbed
import java.io.IOException
import org.json.*

private const val searchUrl = "https://registry.npmjs.com/-/v1/search"
private const val downloadsUrl = "https://api.npmjs.org/downloads/point/last-week"

@AliucordPlugin
class npm : Plugin() {
    override fun start(context: Context) {
        val arguments =
                listOf(
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
            val query = ctx.getRequiredString("package")
            var send = ctx.getBoolOrDefault("send", false)
            var embed: List<MessageEmbed?>? = null
            var msg: String;
            try {
                val pkg = JSONObject(JSONObject(Http.Request(QueryBuilder(searchUrl).append("size", "1").append("text", query).toString(), "GET").execute().text()).getJSONArray("objects")[0].toString()).getJSONObject("package")
                val downloads = JSONObject(Http.Request(QueryBuilder("${downloadsUrl}/${query}").toString(), "GET").execute().text()).getString("downloads")

                if (send) {
                    msg = 
"""
>>> **${pkg.getString("name")}**
${pkg.getString("description")}

**Weekly Downloads**: `${downloads}`
**Version**: `${pkg.getString("version")}`
**Author**: `${pkg.getJSONObject("author").getString("name")}`

<${pkg.getJSONObject("links").getString("npm")}>
"""
                } else {
                    msg = "I found this package:"
                    embed =
                            listOf(
                                    MessageEmbedBuilder()
                                            .setTitle(pkg.getString("name"))
                                            .setUrl(pkg.getJSONObject("links").getString("npm"))
                                            .setDescription(
                                                    "${pkg.getString("description")}\n\n**Weekly Downloads**: `${downloads}`\n**Version**: `${pkg.getString("version")}`\n**Author**: `${pkg.getJSONObject("author").getString("name")}`"
                                            )
                                            .build()
                            )
                }
            } catch (t: IOException) {
                msg = "```json\n{\n  \"error\": \"package $query not found\"\n}```"
                send = false
            }
            CommandResult(
                    msg,
                    embed,
                    send,
                    "NPM",
                    "https://static.npmjs.com/58a19602036db1daee0d7863c94673a4.png"
            )
        }
    }

    override fun stop(context: Context) {
        // Unregister our commands
        commands.unregisterAll()
    }
}
