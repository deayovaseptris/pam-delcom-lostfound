package com.ifs21017.lostfound.presentation.lostfound

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ifs21017.lostfound.R
import com.ifs21017.lostfound.data.entity.DelcomObjectEntity
import com.ifs21017.lostfound.data.model.DelcomObject
import com.ifs21017.lostfound.data.remote.MyResult
import com.ifs21017.lostfound.data.remote.response.LostFoundObjectResponse
import com.ifs21017.lostfound.databinding.ActivityObjectDetailBinding
import com.ifs21017.lostfound.helper.Utils.Companion.observeOnce
import com.ifs21017.lostfound.presentation.ViewModelFactory

class ObjectDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectDetailBinding
    private val viewModel by viewModels<ObjectViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var isChanged: Boolean = false
    private var isFavorite: Boolean = false
    private var delcomLostFound: DelcomObjectEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }

    private fun setupView() {
        showComponent(false)
        showLoading(false)
    }

    private fun setupAction() {
        val lostfoundId = intent.getIntExtra(KEY_OBJECT_ID, 0)
        if (lostfoundId == 0) {
            finish()
            return
        }
        observeGetObject(lostfoundId)
        binding.appbarObjectDetail.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(KEY_IS_CHANGED, isChanged)
            setResult(RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadObject(lostfound: LostFoundObjectResponse) {
        showComponent(true)
        binding.apply {
            tvObjectDetailTitle.text = lostfound.title
            tvObjectDetailDate.text = "Dibuat pada: ${lostfound.createdAt}"
            tvObjectDetailDesc.text = "Deskripsi: ${lostfound.description}"
            tvObjectDetailStatus.text = "Status: ${lostfound.status}"
            cbObjectDetailIsCompleted.isChecked = lostfound.status == "lost"
            ivObjectDetailCover.visibility = View.VISIBLE
            Glide.with(this@ObjectDetailActivity)
                .load(lostfound.cover)
                .placeholder(R.drawable.ic_image_24)
                .into(ivObjectDetailCover)
            viewModel.getLocalObject(lostfound.id).observeOnce {
                if(it != null){
                    delcomLostFound = it
                    setFavorite(true)
                }else{
                    setFavorite(false)
                }
            }
            cbObjectDetailIsCompleted.setOnCheckedChangeListener { _, isChecked ->
                val status = if (isChecked) "Selesai" else "Belum selesai"
                viewModel.putObject(
                    lostfound.id,
                    lostfound.title,
                    lostfound.description,
                    status,
                    isChecked
                ).observeOnce { result ->
                    when (result) {
                        is MyResult.Error -> {
                            val message = if (isChecked) {
                                "Gagal menyelesaikan object: ${lostfound.title}"
                            } else {
                                "Gagal batal menyelesaikan object: ${lostfound.title}"
                            }
                            showToast(message)
                        }
                        is MyResult.Success -> {
                            val message = if (isChecked) {
                                "Berhasil menyelesaikan object: ${lostfound.title}"
                            } else {
                                "Berhasil batal menyelesaikan object: ${lostfound.title}"
                            }
                            showToast(message)
                            if (lostfound.isCompleted == 1 != isChecked) {
                                isChanged = true
                            }
                        }
                        else -> {}
                    }
                }
            }
            ivLostFoundDetailActionFavorite.setOnClickListener {
                if(isFavorite){
                    setFavorite(false)
                    if(delcomLostFound != null){
                        viewModel.deleteLocalTodo(delcomLostFound!!)
                    }
                    Toast.makeText(
                        this@ObjectDetailActivity,
                        "LostFound berhasil dihapus dari daftar favorite",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    delcomLostFound = DelcomObjectEntity(
                        id = lostfound.id,
                        title = lostfound.title,
                        description = lostfound.description,
                        isCompleted = lostfound.isCompleted,
                        cover = lostfound.cover,
                        createdAt = lostfound.createdAt,
                        updatedAt = lostfound.updatedAt,
                        status = ""
                    )

                    setFavorite(true)
                    viewModel.insertLocalObject(delcomLostFound!!)
                    Toast.makeText(
                        this@ObjectDetailActivity,
                        "LostFound berhasil ditambahkan ke daftar favorite",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            ivObjectDetailActionDelete.setOnClickListener {
                showDeleteConfirmationDialog(lostfound.id)
            }
            ivObjectDetailActionEdit.setOnClickListener {
                editObject(lostfound)
            }
        }
    }

    private fun observeGetObject(lostfoundId: Int) {
        viewModel.getObject(lostfoundId).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    loadObject(result.data.data.lostFound)
                }
                is MyResult.Error -> {
                    showToast(result.error)
                    showLoading(false)
                    finishAfterTransition()
                }
            }
        }
    }

    private fun observeDeleteObject(lostfoundId: Int) {
        showComponent(false)
        showLoading(true)
        viewModel.delete(lostfoundId).observeOnce { result ->
            when (result) {
                is MyResult.Error -> {
                    showComponent(true)
                    showLoading(false)
                    showToast("Gagal menghapus object: ${result.error}")
                }
                is MyResult.Success -> {
                    showLoading(false)
                    showToast("Berhasil menghapus object")
                    val resultIntent = Intent()
                    resultIntent.putExtra(KEY_IS_CHANGED, true)
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }
                else -> {}
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@ObjectDetailActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun setFavorite(status: Boolean){
        isFavorite = status
        if(status){
            binding.ivLostFoundDetailActionFavorite
                .setImageResource(R.drawable.ic_favorite_24)
        }else{
            binding.ivLostFoundDetailActionFavorite
                .setImageResource(R.drawable.ic_favorite_border_24)
        }
    }

    private fun showDeleteConfirmationDialog(lostfoundId: Int) {
        val builder = AlertDialog.Builder(this@ObjectDetailActivity)
        builder.setTitle("Konfirmasi Hapus Object")
            .setMessage("Anda yakin ingin menghapus object ini?")
        builder.setPositiveButton("Ya") { _, _ ->
            observeDeleteObject(lostfoundId)
        }
        builder.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun editObject(lostfound: LostFoundObjectResponse) {
        val delcomObject = DelcomObject(
            lostfound.id,
            lostfound.title,
            lostfound.description,
            lostfound.isCompleted == 1,
            lostfound.status,
            lostfound.cover
        )
        val intent = Intent(
            this@ObjectDetailActivity,
            ObjectManageActivity::class.java
        )
        intent.putExtra(ObjectManageActivity.KEY_IS_ADD, false)
        intent.putExtra(ObjectManageActivity.KEY_OBJECT, delcomObject)
        startActivityForResult(intent, ObjectManageActivity.RESULT_CODE)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbObjectDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showComponent(status: Boolean) {
        binding.llObjectDetail.visibility = if (status) View.VISIBLE else View.GONE
    }

    companion object {
        const val KEY_OBJECT_ID = "object_id"
        const val KEY_IS_CHANGED = "is_changed"
        const val RESULT_CODE = 1001
    }
}
