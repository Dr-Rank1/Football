package com.rank.football.data.firebase

import android.content.Context
import com.rank.football.data.local.AppPreferences
import com.rank.football.data.firebase.FirebaseAuthHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val reaction: String = ""
)

/** Manages live match chat via Firebase Realtime Database with anonymous auth. */
class ChatRepository(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private var lastSentAt = 0L

    private val blockedWords = listOf("damn", "hell")

    /** Signs in anonymously and ensures a display name exists. */
    suspend fun ensureAuth() {
        FirebaseAuthHelper.ensureAnonymousUser()
    }

    /** Sends a chat message after profanity and rate-limit checks. */
    suspend fun sendMessage(fixtureId: Int, text: String): Result<Unit> {
        val now = System.currentTimeMillis()
        if (now - lastSentAt < 3000) return Result.failure(IllegalStateException("Rate limited"))
        if (blockedWords.any { text.contains(it, ignoreCase = true) }) {
            return Result.failure(IllegalStateException("Message not allowed"))
        }
        ensureAuth()
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Not authenticated"))
        val name = AppPreferences.displayName(context).first()
        val key = database.child("chats").child(fixtureId.toString()).child("messages").push().key
            ?: return Result.failure(IllegalStateException("Push failed"))
        val msg = mapOf(
            "id" to key,
            "userId" to user.uid,
            "displayName" to name,
            "text" to text.trim(),
            "timestamp" to now,
            "reaction" to ""
        )
        database.child("chats").child(fixtureId.toString()).child("messages").child(key).setValue(msg)
        lastSentAt = now
        return Result.success(Unit)
    }

    /** Observes the last 100 chat messages for a fixture. */
    fun observeMessages(fixtureId: Int): Flow<List<ChatMessage>> = callbackFlow {
        val ref = database.child("chats").child(fixtureId.toString()).child("messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { child ->
                    child.getValue(ChatMessage::class.java)
                }.sortedBy { it.timestamp }.takeLast(100)
                trySend(messages)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Returns count of active watchers for the chat room. */
    fun observeWatcherCount(fixtureId: Int): Flow<Int> = callbackFlow {
        val ref = database.child("chats").child(fixtureId.toString()).child("watchers")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.childrenCount.toInt().coerceAtLeast(1))
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Registers this device as an active watcher (presence + onDisconnect cleanup). */
    suspend fun joinWatchers(fixtureId: Int) {
        ensureAuth()
        val uid = auth.currentUser?.uid ?: return
        val ref = database.child("chats").child(fixtureId.toString()).child("watchers").child(uid)
        ref.setValue(true).await()
        ref.onDisconnect().removeValue()
    }

    /** Removes this device from the active watchers list. */
    suspend fun leaveWatchers(fixtureId: Int) {
        val uid = auth.currentUser?.uid ?: return
        database.child("chats").child(fixtureId.toString()).child("watchers").child(uid)
            .removeValue()
            .await()
    }
}
