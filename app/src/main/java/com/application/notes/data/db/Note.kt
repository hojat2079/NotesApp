package com.application.notes.data.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.application.notes.util.DATABASE_NAME
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = DATABASE_NAME)
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "title")
    val title: String? = "",
    @ColumnInfo(name = "date_time")
    val dateTime: String? = "",
    @ColumnInfo(name = "subtitle")
    val subtitle: String? = "",
    @ColumnInfo(name = "body_text")
    val bodyText: String? = "",
    @ColumnInfo(name = "image_path")
    val imagePath: String? = "",
    @ColumnInfo(name = "color")
    val color: String? = "",
    @ColumnInfo(name = "web_link")
    var webLink: String? = "",
    @ColumnInfo(name = "selected")
    var selected: Boolean = false

) : Parcelable {
}