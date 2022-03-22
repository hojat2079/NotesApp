package com.application.notes.callback

import com.application.notes.data.db.Note

interface NoteCallback {
    fun onClick(note: Note, position: Int)
    fun onLongClick(isSelected: Boolean)
}