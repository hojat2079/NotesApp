package com.application.notes.ui.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.application.notes.callback.NoteCallback
import com.application.notes.data.db.Note
import com.application.notes.databinding.ItemContainerNoteBinding
import java.util.*
import kotlin.collections.ArrayList

class NotesMainAdapter(private var notes: ArrayList<Note>, private val callback: NoteCallback) :
    RecyclerView.Adapter<NotesMainAdapter.ViewHolder>() {
    private var timer: Timer? = null
    private val sourceList = notes

    inner class ViewHolder(private val binding: ItemContainerNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val noteLayoutBackground = binding.noteLayout.background as GradientDrawable
        fun onBind(note: Note) {
            binding.titleTv.text = note.title
            binding.subTitleTv.text = note.subtitle
            binding.timeDateTv.text = note.dateTime
            if (note.color != null && note.color.isNotEmpty())
                noteLayoutBackground.setColor(Color.parseColor(note.color))
            else noteLayoutBackground.setColor(Color.parseColor("#333333"))
            if (note.imagePath != null && note.imagePath.isNotEmpty()) {
                binding.imageNoteIv.visibility = View.VISIBLE
                binding.imageNoteIv.setImageBitmap(BitmapFactory.decodeFile(note.imagePath))
            } else binding.imageNoteIv.visibility = View.GONE
            binding.noteLayout.setOnClickListener {
                if (getSelectedNote().isEmpty())
                    callback.onClick(note, notes.indexOf(note))
                else {
                    if (note.selected) {
                        binding.doneIconIv.visibility = View.INVISIBLE
                        note.selected = false
                        if (getSelectedNote().isEmpty()) {
                            callback.onLongClick(false)
                        }
                    } else {
                        note.selected = true
                        binding.doneIconIv.visibility = View.VISIBLE
                        callback.onLongClick(true)
                    }
                }
            }
            if (note.selected) {
                binding.doneIconIv.visibility = View.VISIBLE
            } else binding.doneIconIv.visibility = View.INVISIBLE
            binding.noteLayout.setOnLongClickListener {
                if (note.selected) {
                    binding.doneIconIv.visibility = View.INVISIBLE
                    note.selected = false
                    if (getSelectedNote().isEmpty()) {
                        callback.onLongClick(false)
                    }
                } else {
                    note.selected = true
                    binding.doneIconIv.visibility = View.VISIBLE
                    callback.onLongClick(true)
                }
                true
            }
        }

    }

    fun getSelectedNote(): List<Note> {
        val selectedNotes = ArrayList<Note>()
        notes.forEach { note ->
            if (note.selected)
                selectedNotes.add(note)
        }
        return selectedNotes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContainerNoteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(notes[position])
    }

    override fun getItemCount(): Int = notes.size
    override fun getItemViewType(position: Int): Int = position

    fun searchNotes(searchKeyboard: String) {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                notes = if (searchKeyboard.trim().isEmpty())
                    sourceList
                else {
                    val tempNote = ArrayList<Note>()
                    sourceList.forEach { note ->
                        if (note.title!!.trim().contains(searchKeyboard, true)
                            || note.subtitle!!.trim().contains(searchKeyboard, true)
                            || note.bodyText!!.trim().contains(searchKeyboard, true)
                        ) {
                            tempNote.add(note)
                        }
                    }
                    tempNote
                }
                Handler(Looper.getMainLooper()).post {
                    notifyDataSetChanged()
                }
            }
        }, 500)
    }

    fun cancelTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    fun cancelDelete() {
        notes.forEach {
            it.selected = false
        }
        notifyDataSetChanged()
    }
    fun deleteNotes(){
        getSelectedNote().forEach {
            notes.remove(it)
        }
        notifyDataSetChanged()
    }

    fun setNewData(newNotes: ArrayList<Note>) {
        val notesMainDiffUtil = NotesMainDiffUtil(this.notes, newNotes)
        val diffUtilResult = DiffUtil.calculateDiff(notesMainDiffUtil)
        this.notes = newNotes
        diffUtilResult.dispatchUpdatesTo(this)

    }
}