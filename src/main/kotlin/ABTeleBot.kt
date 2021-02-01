import bot.BotConfig
import bot.BotConfig.logIfEnabled
import bot.BotLogic
import com.github.kotlintelegrambot.Bot
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import java.util.logging.Logger

class ABTeleBot : HttpFunction {
    companion object {
        var log: Logger = Logger.getLogger(ABTeleBot::class.java.name)

        val bot : Bot

        init {
            BotConfig.setCloudFunctionLogger(log)
            BotConfig.loadConfigFromDbAndStorage()
            bot = BotLogic().getBotLogic()
            logIfEnabled("Bot loaded -- set webhook ....")
            bot.startWebhook()
        }
    }

    // Any http/https request to endpoint comes here

    override fun service(request: HttpRequest?, response: HttpResponse?) {
        logIfEnabled("HTTP endpoint: request received")

        if (request == null) {
            logIfEnabled("Request is empty. Exit.")
            return
        }

        val telegramRequest = request.reader.readText()
        logIfEnabled("Telegram request = $telegramRequest")
        bot.processUpdate(telegramRequest)

        // We don't care about response cause bot send answer itself
    }
}