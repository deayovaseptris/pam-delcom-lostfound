package com.ifs21017.lostfound.presentation.lostfound

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ifs21017.lostfound.R
import com.ifs21017.lostfound.data.model.DelcomObject
import com.ifs21017.lostfound.data.remote.MyResult
import com.ifs21017.lostfound.databinding.ActivityObjectManageBinding
import com.ifs21017.lostfound.helper.Utils.Companion.observeOnce
import com.ifs21017.lostfound.helper.getImageUri
import com.ifs21017.lostfound.helper.reduceFileImage
import com.ifs21017.lostfound.helper.uriToFile
import com.ifs21017.lostfound.presentation.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ObjectManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectManageBinding
    private var currentImageUri: Uri? = null
    private val viewModel by viewModels<ObjectViewModel> {
        ViewModelFactory.getInstance(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectManageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }

    private fun setupView() {
        showLoading(false)

        val statusArray = arrayOf("lost", "found")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.etObjectManageStatus.adapter = adapter
    }

    private fun setupAction() {
        val isAddObject = intent.getBooleanExtra(KEY_IS_ADD, true)
        if (isAddObject) {
            manageAddObject()
        } else {
            val delcomObject = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    intent.getParcelableExtra(KEY_OBJECT, DelcomObject::class.java)
                }
                else -> {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<DelcomObject>(KEY_OBJECT)
                }
            }
            if (delcomObject == null) {
                finishAfterTransition()
                return
            }
            manageEditObject(delcomObject)
        }
        binding.appbarObjectManage.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    private fun manageAddObject() {
        binding.apply {
            appbarObjectManage.title = "Tambah Object"
            btnObjectManageSave.setOnClickListener {
                val title = etObjectManageTitle.text.toString()
                val description = etObjectManageDesc.text.toString()
                val status = etObjectManageStatus.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty()) {
                    AlertDialog.Builder(this@ObjectManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }
                observePostObject(title, description, status)
            }
            btnTodoManageCamera.setOnClickListener {
                startCamera()
            }
            btnTodoManageGallery.setOnClickListener {
                startGallery()
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch("image/*")
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri)
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Toast.makeText(
                applicationContext,
                "Tidak ada media yang dipilih!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_image_24)
                .into(binding.ivTodoManageCover)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun observePostObject(title: String, description: String, status: String) {
        viewModel.postObject(title, description, status).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    if (currentImageUri != null) {
                        observeAddCoverObject(result.data.lostfoundId)
                    } else {
                        showLoading(false)
                        val resultIntent = Intent()
                        setResult(RESULT_CODE, resultIntent)
                        finishAfterTransition()
                    }
                }
                is MyResult.Error -> {
                    AlertDialog.Builder(this@ObjectManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun manageEditObject(lostfound: DelcomObject) {
        binding.apply {
            appbarObjectManage.title = "Ubah Object"
            etObjectManageTitle.setText(lostfound.title)
            etObjectManageDesc.setText(lostfound.description)
            cbObjectDetailIsCompleted.isChecked = lostfound.isCompleted

            val statusArray = resources.getStringArray(R.array.status)
            val statusIndex = statusArray.indexOf(lostfound.status)
            etObjectManageStatus.setSelection(statusIndex)

            if (lostfound.cover != null) {
                Glide.with(this@ObjectManageActivity)
                    .load(lostfound.cover)
                    .placeholder(R.drawable.ic_image_24)
                    .into(ivTodoManageCover)
            }

            btnObjectManageSave.setOnClickListener {
                val title = etObjectManageTitle.text.toString()
                val description = etObjectManageDesc.text.toString()
                val status = etObjectManageStatus.selectedItem.toString()
                val isCompleted = cbObjectDetailIsCompleted.isChecked

                if (title.isEmpty() || description.isEmpty()) {
                    AlertDialog.Builder(this@ObjectManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }
                observePutObject(lostfound.id, title, description, status, isCompleted)
            }
            btnTodoManageCamera.setOnClickListener {
                startCamera()
            }
            btnTodoManageGallery.setOnClickListener {
                startGallery()
            }
        }
    }

    private fun observePutObject(
        lostfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ) {
        viewModel.putObject(
            lostfoundId,
            title,
            description,
            status,
            isCompleted
        ).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    if (currentImageUri != null) {
                        observeAddCoverObject(lostfoundId)
                    } else {
                        showLoading(false)
                        val resultIntent = Intent()
                        setResult(RESULT_CODE, resultIntent)
                        finishAfterTransition()
                    }
                }
                is MyResult.Error -> {
                    AlertDialog.Builder(this@ObjectManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun observeAddCoverObject(
        todoId: Int,
    ) {
        val imageFile =
            uriToFile(currentImageUri!!, this).reduceFileImage()
        val requestImageFile =
            imageFile.asRequestBody("image/jpeg".toMediaType())
        val reqPhoto =
            MultipartBody.Part.createFormData(
                "cover",
                imageFile.name,
                requestImageFile
            )
        viewModel.addCoverObject(
            todoId,
            reqPhoto
        ).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    val resultIntent = Intent()
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }
                is MyResult.Error -> {
                    showLoading(false)
                    AlertDialog.Builder(this@ObjectManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ ->
                            val resultIntent = Intent()
                            setResult(RESULT_CODE, resultIntent)
                            finishAfterTransition()
                        }
                        setCancelable(false)
                        create()
                        show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbObjectManage.visibility =
            if (isLoading) View.VISIBLE else View.GONE
        binding.btnObjectManageSave.isActivated = !isLoading
        binding.btnObjectManageSave.text =
            if (isLoading) "" else "Simpan"
    }

    companion object {
        const val KEY_IS_ADD = "is_add"
        const val KEY_OBJECT = "object"
        const val RESULT_CODE = 1002
    }
}
