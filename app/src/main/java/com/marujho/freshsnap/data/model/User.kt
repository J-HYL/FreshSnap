package com.marujho.freshsnap.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_user")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_email")
    val userEmail: String,

    @ColumnInfo(name = "user_name")
    val userName: String,

    @ColumnInfo(name = "user_salt")
    val userSalt: String, // guardar en base64

    // se usa para saber si la contraseña es correcta sin tener que guardarla
    @ColumnInfo(name = "encrypted_verification_value")
    val encryptedVerificationValue: String,

    @ColumnInfo(name = "iv_verification_value")
    val ivVerificationValue: String
)
