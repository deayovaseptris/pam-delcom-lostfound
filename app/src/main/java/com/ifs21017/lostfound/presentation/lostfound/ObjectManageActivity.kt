package com.ifs21017.lostfound.presentation.lostfound

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ifs21017.lostfound.R
import com.ifs21017.lostfound.data.model.DelcomObject
import com.ifs21017.lostfound.data.remote.MyResult
import com.ifs21017.lostfound.databinding.ActivityObjectManageBinding
import com.ifs21017.lostfound.helper.Utils.Companion.observeOnce
import com.ifs21017.lostfound.presentation.ViewModelFactory
import com.ifs21017.lostfound.presentation.lostfound.ObjectDetailActivity.Companion.KEY_IS_CHANGED

class ObjectManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectManageBinding
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
                observePostObject(title, description, status, isCompleted)
            }
        }
    }

    private fun observePostObject(title: String, description: String, status: String, isCompleted: Boolean) {
        viewModel.postObject(title, description, status, isCompleted).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    val resultIntent = Intent()
                    resultIntent.putExtra(KEY_IS_CHANGED, true)
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
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
                    showLoading(false)
                    val resultIntent = Intent()
                    resultIntent.putExtra(KEY_IS_CHANGED, true)
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
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
