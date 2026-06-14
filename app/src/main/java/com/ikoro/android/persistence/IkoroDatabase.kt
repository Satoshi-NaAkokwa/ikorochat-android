package com.ikoro.android.persistence

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

/**
 * Room entities for Ikoro persistence.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    @ColumnInfo(name = "recipient_id") val recipientId: String,
    @ColumnInfo(name = "channel") val channel: String?,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "is_self") val isSelf: Boolean,
    @ColumnInfo(name = "is_channel") val isChannel: Boolean,
    @ColumnInfo(name = "delivered") val delivered: Boolean = false,
    @ColumnInfo(name = "read") val read: Boolean = false
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val publicKey: String,
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "last_seen") val lastSeen: Long = 0L,
    @ColumnInfo(name = "trusted") val trusted: Boolean = false
)

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAll(): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE channel = :channel ORDER BY timestamp ASC")
    suspend fun getChannelMessages(channel: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE (sender_id = :peer OR recipient_id = :peer) AND channel IS NULL ORDER BY timestamp ASC")
    suspend fun getPeerMessages(peer: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("UPDATE messages SET delivered = 1 WHERE id = :id")
    suspend fun markDelivered(id: String)

    @Query("DELETE FROM messages WHERE channel = :channel")
    suspend fun deleteChannelMessages(channel: String)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY nickname ASC")
    suspend fun getAll(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE public_key = :publicKey")
    suspend fun delete(publicKey: String)
}

@Database(entities = [MessageEntity::class, ContactEntity::class], version = 1)
abstract class IkoroDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
}
