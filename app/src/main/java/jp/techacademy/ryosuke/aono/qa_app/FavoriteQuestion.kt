package jp.techacademy.ryosuke.aono.qa_app

import java.io.Serializable
import java.util.*

class FavoriteQuestion(val title: String, val body: String, val name: String,val uid: String,bytes: ByteArray):
    Serializable {
    val imageBytes: ByteArray
    init {
        imageBytes = bytes.clone()
    }
}