package bot

import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.QuerySnapshot
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.StorageOptions
import java.util.logging.Logger

const val TELEGRAM_API_TOKEN = "1609320696:AAFrhCYR_LPHk9nh3I6l464Qj5G98IhuWiM"
const val HTTP_ENDPOINT_URL = "https://us-central1-android-broadcast-te-bot.cloudfunctions.net/telegram-bot-endpoint"

// Turn on/off Google Cloud Console Logging

const val JAVA_LOG_ENABLED = true

// Firestore collection - button texts store

const val CONFIG_COLLECTION_NAME = "resources"

// GC Storage bucket name - markdown files with bot message texts

const val CLOUD_STORAGE_BUCKET_NAME = "android-broadcast-te-bot.appspot.com"

// Chat <Bot Administrator> <-> <Bot> ID
// Used to forward messages to administrator

const val ADMIN_CHAT_DB_FIELD = "admin_chat_id"

// Button texts will load from FireStore /resource/ru collection.
// In case of prolem - default values configuration here.

const val TALK2ADMIN_BUTTON_DB_FIELD = "button_talk2admin"
const val TALK2ADMIN_BUTTON_DEFAULT = "Написать сообщение администратору канала"
const val SENDARTICLE_BUTTON_DB_FIELD = "button_send_article"
const val SENDARTICLE_BUTTON_DEFAULT = "Предложить материал для размешения на канале"
const val SENDARTICLE_URL_DB_FIELD = "send_article_form_url"
const val SENDARTICLE_URL_DEFAULT = "https://forms.gle/DQ4CkDah5Yj5ojyg9"
const val AD_BUTTON_DB_FIELD = "button_ad"
const val AD_BUTTON_DEFAULT = "Хочу разместить рекламу на канале"

// Markdown files as message templates stored with Firebase store
// Also in /files dir in project tree.

// Use: gsutil -m cp -r ../files/* gs://abtelebot.appspot.com
// to copy file to GC storage manually
// but you need restart Cloud Function later

const val WELCOME_MESSAGE_FILENAME = "welcome_message.md"
const val WELCOME_MESSAGE_DEFAULT = "Вас привествует Android Broadcast Clerk bot!"

const val CONTINUE_MESSAGE_FILENAME = "continue_message.md"
const val CONTINUE_MESSAGE_DEFAULT = "Android Broadcast Clerk bot. Давайте продолжим."

const val TALK2ADMIN_MESSAGE_FILENAME = "talk2admin_message.md"
const val TALK2ADMIN_MESSAGE_DEFAULT = "Чтобы написать администратору канала начните сообщение со знака решетки #"

const val SUCCESS_MESSAGE_FILENAME = "send_success_message.md"
const val SUCCESS_MESSAGE_DEFAULT = "Ваше сообщение отправлено администратору."

const val CAN_NOT_RECOGNIZE_MESSAGE_FILENAME = "cant_recognize_command_message.md"
const val CAN_NOT_RECOGNIZE_MESSAGE_DEFAULT = "Не могу распознать комманду. Попробуйте еще раз."

// All messages started with - direct messages to admin

const val TALK2ADMIN_PREFIX = "#"

const val AD_MESSAGE_FILENAME = "ad_message.md"
const val AD_MESSAGE_DEFAULT = "Условия размещения рекламы на канале."

object BotConfig {

    private val db = FirestoreOptions.getDefaultInstance().service
    private val storage = StorageOptions.getDefaultInstance().service

    var adminChatId: Long = 0L

    var talk2AdminButtonText: String = TALK2ADMIN_BUTTON_DEFAULT
    var sendArticleButtonText: String = SENDARTICLE_BUTTON_DEFAULT
    var adButtonText: String = AD_BUTTON_DEFAULT

    var sendArticleUrl: String = SENDARTICLE_URL_DEFAULT

    var welcomeMessage: String = WELCOME_MESSAGE_DEFAULT
    var continueMessage: String = CONTINUE_MESSAGE_DEFAULT
    var talk2AdminMessage: String = TALK2ADMIN_MESSAGE_DEFAULT
    var adMessage: String = AD_MESSAGE_DEFAULT
    var errorMessage: String = CAN_NOT_RECOGNIZE_MESSAGE_DEFAULT
    var succesMessage: String = SUCCESS_MESSAGE_DEFAULT

    var log: Logger? = null

    fun setCloudFunctionLogger(log: Logger) {
        this.log = log
    }

    // load all bot info from Firestore and GC Storage

    fun loadConfigFromDbAndStorage() {

        val query = db.collection(CONFIG_COLLECTION_NAME).get()

        adminChatId = query.get().getNumberField(ADMIN_CHAT_DB_FIELD) ?: 0

        talk2AdminButtonText = query.get().getStringField(TALK2ADMIN_BUTTON_DB_FIELD) ?: TALK2ADMIN_BUTTON_DEFAULT
        sendArticleButtonText = query.get().getStringField(SENDARTICLE_BUTTON_DB_FIELD) ?: SENDARTICLE_BUTTON_DEFAULT
        adButtonText = query.get().getStringField(AD_BUTTON_DB_FIELD) ?: AD_BUTTON_DEFAULT

        sendArticleUrl = query.get().getStringField(SENDARTICLE_URL_DB_FIELD) ?: SENDARTICLE_URL_DEFAULT

        welcomeMessage = loadMessageFile(WELCOME_MESSAGE_FILENAME) ?: WELCOME_MESSAGE_DEFAULT
        continueMessage = loadMessageFile(CONTINUE_MESSAGE_FILENAME) ?: CONTINUE_MESSAGE_DEFAULT
        talk2AdminMessage = loadMessageFile(TALK2ADMIN_MESSAGE_FILENAME) ?: TALK2ADMIN_MESSAGE_DEFAULT
        adMessage = loadMessageFile(AD_MESSAGE_FILENAME) ?: AD_MESSAGE_DEFAULT
        errorMessage = loadMessageFile(CAN_NOT_RECOGNIZE_MESSAGE_FILENAME) ?: CAN_NOT_RECOGNIZE_MESSAGE_DEFAULT
        succesMessage = loadMessageFile(SUCCESS_MESSAGE_FILENAME) ?: SUCCESS_MESSAGE_DEFAULT

    }

    // get string field from Firestore snapshot

    private fun QuerySnapshot.getStringField(fieldName: String): String? {
        return if (this.documents.size == 0) {
            logIfEnabled("Firestore config query return no docs")
            null
        } else {
            logIfEnabled("Loading $fieldName")
            this.documents[0].getString(fieldName)
        }
    }

    // get number field from Firestore snapshot

    private fun QuerySnapshot.getNumberField(fieldName: String): Long? {
        return if (this.documents.size == 0) {
            logIfEnabled("Firestore config query return no docs")
            null
        } else {
            logIfEnabled("Loading $fieldName")
            this.documents[0].getLong(fieldName)
        }
    }

    // load file from GC Storage file to memory

    private fun loadMessageFile(fileName: String): String? {
        return try {
            val blob = storage.get(BlobId.of(CLOUD_STORAGE_BUCKET_NAME, fileName))
            val resource = blob.getContent().decodeToString()
            logIfEnabled("$fileName loaded (${resource.length} bytes)")
            resource
        } catch (ex: Exception) {
            logIfEnabled("Read file $fileName exception : $ex")
            null
        }
    }

    fun logIfEnabled(msg: String, forceLogging : Boolean = false) {
        if (JAVA_LOG_ENABLED or forceLogging)
           log?.info("---> $msg")
    }
}

