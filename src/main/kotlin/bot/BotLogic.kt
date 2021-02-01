package bot

import bot.BotConfig.adMessage
import bot.BotConfig.adminChatId
import bot.BotConfig.continueMessage
import bot.BotConfig.errorMessage
import bot.BotConfig.loadConfigFromDbAndStorage
import bot.BotConfig.logIfEnabled
import bot.BotConfig.succesMessage
import bot.BotConfig.talk2AdminMessage
import bot.BotConfig.welcomeMessage
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.telegramError
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.webhook
import db.FirebaseEngine

class BotLogic {

    fun getBotLogic(): Bot =
        bot {

            token = TELEGRAM_API_TOKEN

            webhook {
                url = "${HTTP_ENDPOINT_URL}/${TELEGRAM_API_TOKEN}"
                maxConnections = 50
                allowedUpdates = listOf("message")
            }

            dispatch {

                message(Filter.Text) {
                    if (message.text != null) {
                        val receivedMessage = message.text!!
                        if (receivedMessage.startsWith(TALK2ADMIN_PREFIX)) {
                            val userName = message.from?.username ?: "<Unknown username>"
                            val firstName = message.from?.firstName ?: "<Unknown firstname>"
                            val lastName = message.from?.lastName ?: "<Unknown lastname>"
                            val messageText = receivedMessage.removePrefix(TALK2ADMIN_PREFIX)
                            FirebaseEngine.saveMessageToAdmin(userName, firstName, lastName, messageText)
                            if (adminChatId != 0L) {
                                var msg = "Message from @$userName [$firstName $lastName]\n\n"
                                msg += receivedMessage.removePrefix(TALK2ADMIN_PREFIX)
                                bot.sendMessage(
                                    chatId = adminChatId,
                                    text = msg
                                )
                            }
                            bot.sendMessage(
                                chatId = message.chat.id,
                                text = succesMessage,
                                replyMarkup = getMainMenuMarkup()
                            )
                        } else {
                            bot.sendMessage(
                                chatId = message.chat.id,
                                text = errorMessage,
                                replyMarkup = getMainMenuMarkup()
                            )
                        }
                    }
                }

                command("start") {
                    bot.sendMessage(
                        chatId = message.chat.id,
                        text = welcomeMessage,
                        replyMarkup = getMainMenuMarkup()
                    )
                }

                command("adminreload") {
                    loadConfigFromDbAndStorage()
                    bot.sendMessage(
                        chatId = message.chat.id,
                        text = "Database / Storage reloaded",
                        replyMarkup = getMainMenuMarkup()
                    )
                }

                callbackQuery(TALK2ADMIN_BUTTON_DB_FIELD) {
                    val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                    if (callbackQuery.message?.messageId != null) {
                        bot.deleteMessage(chatId, callbackQuery.message?.messageId!!)
                    }
                    bot.sendMessage(
                        chatId = chatId,
                        text = talk2AdminMessage,
                        replyMarkup = getMainMenuMarkup()
                    )
                }

                callbackQuery(AD_BUTTON_DB_FIELD) {
                    val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                    if (callbackQuery.message?.messageId != null) {
                        bot.deleteMessage(chatId, callbackQuery.message?.messageId!!)
                    }
                    bot.sendMessage(
                        chatId = chatId,
                        text = adMessage
                        ,
                        parseMode = ParseMode.MARKDOWN_V2
                    )
                    val userName = callbackQuery.message?.from?.username ?: "<Unknown username>"
                    val firstName = callbackQuery.message?.from?.firstName ?: "<Unknown firstname>"
                    val lastName = callbackQuery.message?.from?.lastName ?: "<Unknown lastname>"
                    FirebaseEngine.logAdConditionsRequest(userName, firstName, lastName)
                    bot.sendMessage(
                        chatId = chatId,
                        text = continueMessage,
                        replyMarkup = getMainMenuMarkup()
                    )
                }

                telegramError {
                    logIfEnabled("Telegram error -> ${error.getErrorMessage()}", forceLogging = true)
                }
            }
        }
}

