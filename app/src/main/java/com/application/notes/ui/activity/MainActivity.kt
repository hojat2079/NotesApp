package com.application.notes.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.application.notes.R
import com.application.notes.callback.NoteCallback
import com.application.notes.data.db.Note
import com.application.notes.databinding.ActivityMainBinding
import com.application.notes.databinding.LayoutAddUrlBinding
import com.application.notes.ui.adapter.NotesMainAdapter
import com.application.notes.util.*
import com.application.notes.viewmodel.NoteViewModel
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NoteCallback, EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: NoteViewModel by viewModels()
    private val adapter: NotesMainAdapter by lazy { NotesMainAdapter(notes, this) }
    private val notes = ArrayList<Note>()
    private var pathImageQuickAction = ""
    private var urlQuickAction = ""

    //update note
    private var noteClickedPosition = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.notesLiveData.observe(this) { getAllNote(it) }
        viewModel.successFullDeleteNotesLiveData.observe(this)
        { deleteNotesSuccessFull() }
        viewModel.getAllNote()
        initView()
    }


    private fun initView() {
        binding.addNoteMainIv.setOnClickListener { addNewNote() }
        binding.addNoteIv.setOnClickListener { addNewNote() }
        binding.addImageIv.setOnClickListener { selectImage() }
        binding.addLinkIv.setOnClickListener { showDialogAddUrl() }
        binding.closeDeleteNotesIv.setOnClickListener { cancelDeleteNotes() }
        binding.DeleteNotesIv.setOnClickListener { deleteNotes() }
        initRecyclerView()
        addTextWatcherToSearchEditText()
        binding.closeIv.setOnClickListener {
            binding.inputTextEt.setText("")
            binding.closeIv.visibility = View.GONE
        }
    }

    private fun initRecyclerView() {
        binding.notesRv.adapter = adapter
        binding.notesRv.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
    }

    private fun addTextWatcherToSearchEditText() {
        binding.inputTextEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.cancelTimer()
                if (p0 != null && p0.isNotEmpty()) {
                    binding.closeIv.visibility = View.VISIBLE
                } else binding.closeIv.visibility = View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null && notes.isNotEmpty()) {
                    adapter.searchNotes(p0.toString())
                }
            }
        })
    }

    private fun getAllNote(list: List<Note>) {
        Timber.i("insert note call -> list %s", list)
        if (notes.isEmpty()) {
            notes.addAll(list)
            adapter.notifyDataSetChanged()
        } else if (notes.isNotEmpty() && notes.size < list.size) {
            notes.add(0, list[0])
            adapter.notifyItemInserted(0)
            binding.notesRv.smoothScrollToPosition(0)
        } else if (notes.isNotEmpty() && notes.size == list.size && notes[noteClickedPosition] != list[noteClickedPosition] && noteClickedPosition >= 0) {
            notes.removeAt(noteClickedPosition)
            notes.add(noteClickedPosition, list[noteClickedPosition])
            adapter.notifyItemChanged(noteClickedPosition)
        } else if (notes.isNotEmpty() && notes.size > list.size) {
            notes.removeAt(noteClickedPosition)
            adapter.notifyItemRemoved(noteClickedPosition)
        }
    }

    override fun onClick(note: Note, position: Int) {
        noteClickedPosition = position
        startActivity(
            Intent(this, CreateNoteActivity::class.java)
                .also {
                    it.putExtra(IS_VIEW_UPDATED, true)
                    it.putExtra(NOTE_EXTRA, note)
                }
        )
    }

    override fun onLongClick(isSelected: Boolean) {
        if (isSelected) {
            binding.searchIv.isEnabled = false
            binding.inputTextEt.isEnabled = false
            binding.titleMyTextTv.visibility = View.GONE
            binding.layoutMultipleDeleteNote.visibility = View.VISIBLE
        } else {
            binding.searchIv.isEnabled = true
            binding.inputTextEt.isEnabled = true
            binding.titleMyTextTv.visibility = View.VISIBLE
            binding.layoutMultipleDeleteNote.visibility = View.GONE
        }
    }

    private fun cancelDeleteNotes() {
        binding.searchIv.isEnabled = true
        binding.inputTextEt.isEnabled = true
        binding.titleMyTextTv.visibility = View.VISIBLE
        binding.layoutMultipleDeleteNote.visibility = View.GONE
        adapter.cancelDelete()
    }

    private fun deleteNotesSuccessFull() {
        adapter.deleteNotes()
        binding.searchIv.isEnabled = true
        binding.inputTextEt.isEnabled = true
        binding.titleMyTextTv.visibility = View.VISIBLE
        binding.layoutMultipleDeleteNote.visibility = View.GONE
    }

    private fun deleteNotes() {
        viewModel.deleteNotes(adapter.getSelectedNote().map { it.id }.toTypedArray())
    }

    private fun addNewNote() {
        val createNoteIntent = Intent(this, CreateNoteActivity::class.java)
        resultLauncherCreateNoteActivity.launch(createNoteIntent)
    }

    private fun addNewNoteWithImage() {
        val createNoteIntent = Intent(this, CreateNoteActivity::class.java)
            .also {
                it.putExtra(SELECT_QUICK_ACTION_IMAGE, true)
                it.putExtra(PATH_IMAGE_EXTRA, pathImageQuickAction)
            }
        resultLauncherCreateNoteActivity.launch(createNoteIntent)

    }

    private fun addNewNoteWithWebUrl() {
        val createNoteIntent = Intent(this, CreateNoteActivity::class.java)
            .also {
                it.putExtra(SELECT_QUICK_ACTION_WEB_URL, true)
                it.putExtra(URL_EXTRA, urlQuickAction)
            }
        resultLauncherCreateNoteActivity.launch(createNoteIntent)
    }

    private fun showDialogAddUrl() {
        val bind = LayoutAddUrlBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).also {
            it.setView(bind.root)
            it.setCancelable(false)
        }.create()
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }

        bind.webUrlEt.requestFocus()
        bind.cancelUrlBtn.setOnClickListener {
            dialog.dismiss()
        }
        bind.AddUrlBtn.setOnClickListener {
            if (bind.webUrlEt.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Empty URL!", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.WEB_URL.matcher(bind.webUrlEt.text.toString().trim()).matches()) {
                Toast.makeText(this, "Invalid URL!", Toast.LENGTH_SHORT).show()
            } else {
                urlQuickAction = bind.webUrlEt.text.toString().trim()
                dialog.dismiss()
                addNewNoteWithWebUrl()
            }
        }
        dialog.show()
    }

    //select image in external storage
    private fun hasExternalStoragePermission(): Boolean {
        Timber.i("hasExternalStoragePermission")
        return EasyPermissions.hasPermissions(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun requestExternalStoragePermission() {
        Timber.i("requestExternalStoragePermission")
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without External Storage Permission",
            REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
            Timber.i("onPermissionsDenied")
        } else {
            Timber.i("ELSEonPermissionsDenied")
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Timber.i("onPermissionsGranted")
        selectImage()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun selectImage() {
        if (hasExternalStoragePermission()) {
            Timber.i("IF select image")
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
                it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                resultLauncherGetPic.launch(it)
            }
        } else {
            Timber.i("ELSE select image")
            requestExternalStoragePermission()
        }
    }

    private val resultLauncherGetPic =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Timber.e("result Ok")
                val data: Intent? = result.data
                if (data != null) {
                    val selectedImageUri = data.data
                    if (selectedImageUri != null) {
                        Timber.e("result Ok , data ->%s", selectedImageUri)
                        try {
                            pathImageQuickAction = getPathWithUri(selectedImageUri)
                            Timber.e("result Ok , path ->%s", pathImageQuickAction)
                            addNewNoteWithImage()
                        } catch (ex: Exception) {
                            Timber.e("Error in $ex")
                        }
                    }
                }
            }
        }

    private val resultLauncherCreateNoteActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                if (result.data!!.getBooleanExtra(
                        IS_NOTE_CHANGED,
                        false
                    )
                ) {
                    viewModel.getAllNote()
                }
            }
        }

    private fun getPathWithUri(uri: Uri): String {
        var filePath = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor == null) {
            filePath = uri.path!!
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }


}