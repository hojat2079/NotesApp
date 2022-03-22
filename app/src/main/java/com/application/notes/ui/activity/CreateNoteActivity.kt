package com.application.notes.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.application.notes.R
import com.application.notes.data.db.Note
import com.application.notes.databinding.ActivityCreateNoteBinding
import com.application.notes.databinding.LayoutAddUrlBinding
import com.application.notes.databinding.LayoutDeleteNoteBinding
import com.application.notes.util.*
import com.application.notes.viewmodel.NoteViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateNoteActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityCreateNoteBinding
    private lateinit var noteTitle: String
    private lateinit var noteSubTitle: String
    private lateinit var noteBodyTitle: String
    private lateinit var bottomSheet: BottomSheetBehavior<View>
    private lateinit var noteTimeTitle: String
    private val viewModel: NoteViewModel by viewModels()


    private var selectedColorNote = color2

    //image path
    private var selectedPathImage = ""

    //available note
    private var availableNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkIsUpdate()
        initView()
        viewModel.successFullInsertNoteLiveData.observe(this)
        { if (it == true) noteIsChanged() }
        viewModel.successFullDeleteNoteLiveData.observe(this)
        { if (it == true) noteIsChanged() }
    }

    private fun noteIsChanged() {
        setResult(Activity.RESULT_OK, Intent().also { intent ->
            intent.putExtra(IS_NOTE_CHANGED, true)
        })
        finish()
    }

    private fun checkIsUpdate() {
        if (intent.getBooleanExtra(IS_VIEW_UPDATED, false)) {
            availableNote = intent.getParcelableExtra(NOTE_EXTRA)
            setDataInViewInUpdateLayout()
        } else if (intent.getBooleanExtra(SELECT_QUICK_ACTION_WEB_URL, false)) {
            updateWebLinkWithClickQuickAction()
        } else if (intent.getBooleanExtra(SELECT_QUICK_ACTION_IMAGE, false)) {
            updateImageWithClickQuickAction()
        }
    }

    private fun updateImageWithClickQuickAction() {
        binding.bottomSheetLayout.titleAddImage.setText(R.string.edit_image)
        binding.imageNoteIv.visibility = View.VISIBLE
        binding.deleteImageIV.visibility = View.VISIBLE
        binding.imageNoteIv.setImageBitmap(
            BitmapFactory.decodeFile(
                intent.getStringExtra(
                    PATH_IMAGE_EXTRA
                )!!
            )
        )
        selectedPathImage = intent.getStringExtra(PATH_IMAGE_EXTRA)!!
    }

    private fun updateWebLinkWithClickQuickAction() {
        binding.webUrlTv.text = intent.getStringExtra(URL_EXTRA)
        binding.bottomSheetLayout.titleAddUrl.setText(R.string.edit_url)
        binding.layoutWebURL.visibility = View.VISIBLE
    }

    private fun setDataInViewInUpdateLayout() {
        binding.noteTitleEt.setText(availableNote!!.title)
        binding.noteTitleEt.setSelection(binding.noteTitleEt.text.toString().length)
        binding.dateTimeTv.text = availableNote!!.dateTime
        binding.noteBodyEt.setText(availableNote!!.bodyText)
        binding.noteBodyEt.setSelection(binding.noteBodyEt.text.toString().length)
        binding.noteSubTitleEt.setText(availableNote!!.subtitle)
        binding.noteSubTitleEt.setSelection(binding.noteSubTitleEt.text.toString().length)
        if (availableNote!!.webLink != null) {
            if (availableNote!!.webLink!!.trim().isNotEmpty()) {
                binding.layoutWebURL.visibility = View.VISIBLE
                binding.webUrlTv.text = availableNote!!.webLink
            } else binding.layoutWebURL.visibility = View.GONE
        } else binding.layoutWebURL.visibility = View.GONE
        if (availableNote!!.imagePath != null) {
            if (availableNote!!.imagePath!!.trim().isNotEmpty()) {
                binding.imageNoteIv.visibility = View.VISIBLE
                binding.deleteImageIV.visibility = View.VISIBLE
                binding.imageNoteIv.setImageBitmap(BitmapFactory.decodeFile(availableNote!!.imagePath))
                selectedPathImage = availableNote!!.imagePath!!
            } else {
                binding.imageNoteIv.visibility = View.GONE
                binding.deleteImageIV.visibility = View.GONE
            }
        } else {
            binding.imageNoteIv.visibility = View.GONE
            binding.deleteImageIV.visibility = View.GONE
        }
    }

    private fun initView() {
        if (!intent.getBooleanExtra(IS_VIEW_UPDATED, false))
            setDateFormat()
        initMiscellaneousBottomSheet()
        binding.backIconIv.setOnClickListener { onBackPressed() }
        binding.doneIconIv.setOnClickListener { saveNote() }
        binding.deleteImageIV.setOnClickListener { deleteImageBitmap() }
        binding.deleteWebLinkIv.setOnClickListener { deleteWebLink() }
    }

    private fun deleteWebLink() {
        binding.layoutWebURL.visibility = View.GONE
        binding.webUrlTv.text = null
        binding.bottomSheetLayout.titleAddUrl.setText(R.string.add_url)
    }

    private fun deleteImageBitmap() {
        binding.imageNoteIv.visibility = View.GONE
        binding.deleteImageIV.visibility = View.GONE
        binding.imageNoteIv.setImageBitmap(null)
        binding.bottomSheetLayout.titleAddImage.setText(R.string.add_url)
        selectedPathImage = ""
    }


    private fun setDateFormat() {
        binding.dateTimeTv.text =
            SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(Date())
    }

    private fun validationNoteText(): Boolean {
        getTextView()
        if (noteTitle.isEmpty()) {
            Toast.makeText(this, "Note Title can't be empty", Toast.LENGTH_SHORT).show()
            return false
        } else if (noteBodyTitle.isEmpty() && noteSubTitle.isEmpty()
        ) {
            Toast.makeText(this, "Note can't be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveNote() {
        hideKeyboard()
        if (validationNoteText()) {
            viewModel.insertNote(
                Note(
                    title = noteTitle,
                    bodyText = noteBodyTitle,
                    dateTime = noteTimeTitle,
                    subtitle = noteSubTitle,
                    color = selectedColorNote,
                    imagePath = selectedPathImage,
                    selected = false
                ).also {
                    if (binding.layoutWebURL.visibility == View.VISIBLE) {
                        it.webLink = binding.webUrlTv.text.toString()
                    }
                    if (availableNote != null) {
                        it.id = availableNote!!.id
                    }
                }
            )

        }
    }

    private fun getTextView() {
        noteTitle = binding.noteTitleEt.text.toString().trim()
        noteBodyTitle = binding.noteBodyEt.text.toString().trim()
        noteTimeTitle = binding.dateTimeTv.text.toString().trim()
        noteSubTitle = binding.noteSubTitleEt.text.toString().trim()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.noteBodyEt.windowToken, 0)
    }

    private fun initMiscellaneousBottomSheet() {
        val linearLayout = binding.bottomSheetLayout.layoutMiscellaneous
        setSubTitleIndicatorColor()
        bottomSheet = BottomSheetBehavior.from(linearLayout)
        val image1 = binding.bottomSheetLayout.color1Iv
        val image2 = binding.bottomSheetLayout.color2Iv
        val image3 = binding.bottomSheetLayout.color3Iv
        val image4 = binding.bottomSheetLayout.color4Iv
        val image5 = binding.bottomSheetLayout.color5Iv

        //check state bottomSheet
        binding.bottomSheetLayout.titleMiscellaneousTv.setOnClickListener {
            if (bottomSheet.state != BottomSheetBehavior.STATE_EXPANDED)
                bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            else bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        //set new color
        image1.setOnClickListener {
            selectedColorNote = color1
            image1.setImageResource(R.drawable.ic_done)
            image2.setImageResource(0)
            image3.setImageResource(0)
            image4.setImageResource(0)
            image5.setImageResource(0)
            setSubTitleIndicatorColor()
        }
        image2.setOnClickListener {
            selectedColorNote = color2
            image1.setImageResource(0)
            image2.setImageResource(R.drawable.ic_done)
            image3.setImageResource(0)
            image4.setImageResource(0)
            image5.setImageResource(0)
            setSubTitleIndicatorColor()
        }
        image3.setOnClickListener {
            selectedColorNote = color3
            image1.setImageResource(0)
            image2.setImageResource(0)
            image3.setImageResource(R.drawable.ic_done)
            image4.setImageResource(0)
            image5.setImageResource(0)
            setSubTitleIndicatorColor()
        }
        image4.setOnClickListener {
            selectedColorNote = color4
            image1.setImageResource(0)
            image2.setImageResource(0)
            image3.setImageResource(0)
            image4.setImageResource(R.drawable.ic_done)
            image5.setImageResource(0)
            setSubTitleIndicatorColor()
        }
        image5.setOnClickListener {
            selectedColorNote = color5
            image1.setImageResource(0)
            image2.setImageResource(0)
            image3.setImageResource(0)
            image4.setImageResource(0)
            image5.setImageResource(R.drawable.ic_done)
            setSubTitleIndicatorColor()
        }
        if (availableNote != null) {
            if (availableNote!!.color != null) {
                if (availableNote!!.color!!.trim().isNotEmpty()) {
                    when (availableNote!!.color) {
                        color2 -> image2.performClick()
                        color3 -> image3.performClick()
                        color4 -> image4.performClick()
                        color5 -> image5.performClick()
                        else -> image1.performClick()
                    }
                }
            }
        }

        binding.bottomSheetLayout.layoutAddImage.setOnClickListener {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            selectImage()
        }
        binding.bottomSheetLayout.layoutAddUrl.setOnClickListener {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            showDialogAddUrl()
        }
        if (binding.imageNoteIv.visibility == View.VISIBLE) {
            binding.bottomSheetLayout.titleAddImage.setText(R.string.edit_image)
        } else binding.bottomSheetLayout.titleAddImage.setText(R.string.add_image)
        if (binding.layoutWebURL.visibility == View.VISIBLE) {
            binding.bottomSheetLayout.titleAddUrl.setText(R.string.edit_url)
        } else binding.bottomSheetLayout.titleAddUrl.setText(R.string.add_url)

        if (availableNote != null) {
            binding.bottomSheetLayout.deleteNoteLayout.visibility = View.VISIBLE
            binding.bottomSheetLayout.deleteNoteLayout.setOnClickListener {
                showDialogDeleteNote()
                bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

    }


    private fun setSubTitleIndicatorColor() {
        val indicatorLayout = binding.indicatorSubTitleView.background as GradientDrawable
        indicatorLayout.setColor(Color.parseColor(selectedColorNote))
    }

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
                resultLauncher.launch(it)
            }
        } else {
            Timber.i("ELSE select image")
            requestExternalStoragePermission()
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedImageUri = data.data
                    if (selectedImageUri != null) {
                        try {
                            val inputStream = contentResolver.openInputStream(selectedImageUri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            selectedPathImage = getPathWithUri(selectedImageUri)
                            binding.imageNoteIv.setImageBitmap(bitmap)
                            binding.imageNoteIv.visibility = View.VISIBLE
                            binding.bottomSheetLayout.titleAddImage.setText(R.string.edit_image)
                            binding.deleteImageIV.visibility = View.VISIBLE
                        } catch (ex: Exception) {
                            Timber.e("$ex")
                        }
                    }
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
                binding.layoutWebURL.visibility = View.VISIBLE
                binding.webUrlTv.text = bind.webUrlEt.text.toString().trim()
                binding.bottomSheetLayout.titleAddUrl.setText(R.string.edit_url)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showDialogDeleteNote() {
        val bind = LayoutDeleteNoteBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this).also {
            it.setView(bind.root)
            it.setCancelable(false)
        }.create()
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }

        bind.cancelDeleteNoteBtn.setOnClickListener {
            dialog.dismiss()
        }
        bind.deleteNoteBtn.setOnClickListener {
            viewModel.deleteNote(availableNote!!)
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if (bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        } else finish()

    }
}