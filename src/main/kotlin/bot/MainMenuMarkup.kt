package bot

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

fun getMainMenuMarkup() : InlineKeyboardMarkup {
    return InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(
                text = BotConfig.talk2AdminButtonText,
                callbackData = TALK2ADMIN_BUTTON_DB_FIELD
            )
        ),
        listOf(
            InlineKeyboardButton.Url(text = BotConfig.sendArticleButtonText, url = BotConfig.sendArticleUrl)
        ),
        listOf(
            InlineKeyboardButton.CallbackData(
                text = BotConfig.adButtonText,
                callbackData = AD_BUTTON_DB_FIELD
            )
        )
    )
}