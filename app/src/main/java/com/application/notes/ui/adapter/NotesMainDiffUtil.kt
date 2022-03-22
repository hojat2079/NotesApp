package com.application.notes.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.application.notes.data.db.Note

class NotesMainDiffUtil(
    private val oldList: List<Note>,
    private val newList: List<Note>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] === newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]

    }
}