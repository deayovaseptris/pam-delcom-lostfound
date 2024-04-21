package com.ifs21017.lostfound.presentation.lostfound

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs21017.lostfound.R
import com.ifs21017.lostfound.adapter.ObjectsAdapter
import com.ifs21017.lostfound.data.entity.DelcomObjectEntity
import com.ifs21017.lostfound.data.remote.MyResult
import com.ifs21017.lostfound.data.remote.response.LostFoundsItemResponse
import com.ifs21017.lostfound.databinding.ActivityObjectFavoriteBinding
import com.ifs21017.lostfound.helper.Utils.Companion.entitiesToResponses
import com.ifs21017.lostfound.helper.Utils.Companion.observeOnce
import com.ifs21017.lostfound.presentation.ViewModelFactory

class ObjectFavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObjectFavoriteBinding
    private val viewModel by viewModels<ObjectViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ObjectDetailActivity.RESULT_CODE) {
            result.data?.let {
                val isChanged = it.getBooleanExtra(
                    ObjectDetailActivity.KEY_IS_CHANGED,
                    false
                )
                if (isChanged) {
                    recreate()
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObjectFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }
    private fun setupAction() {
        binding.appbarObjectFavorite.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(ObjectDetailActivity.KEY_IS_CHANGED, true)
            setResult(ObjectDetailActivity.RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }
    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)
        binding.appbarObjectFavorite.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)
        observeGetObjects()
    }
    private fun observeGetObjects() {
        viewModel.getLocalObjects().observe(this) { lostfound ->
            loadObjectsToLayout(lostfound)
        }
    }
    private fun loadObjectsToLayout(lostfound: List<DelcomObjectEntity>?) {
        showLoading(false)
        val layoutManager = LinearLayoutManager(this)
        binding.rvLostFoundFavoriteLostFounds.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvLostFoundFavoriteLostFounds.addItemDecoration(itemDecoration)
        if (lostfound.isNullOrEmpty()) {
            showEmptyError(true)
            binding.rvLostFoundFavoriteLostFounds.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)
            val adapter = ObjectsAdapter()
            adapter.submitOriginalList(entitiesToResponses(lostfound))
            binding.rvLostFoundFavoriteLostFounds.adapter = adapter
            adapter.setOnItemClickCallback(
                object : ObjectsAdapter.OnItemClickCallback {
                    override fun onCheckedChangeListener(
                        lostFoundsItemResponse: LostFoundsItemResponse,
                        isChecked: Boolean
                    ) {
                        adapter.filter(binding.svLostFoundFavorite.query.toString())
                        val newObject = DelcomObjectEntity(
                            id = lostFoundsItemResponse.id,
                            title = lostFoundsItemResponse.title,
                            description = lostFoundsItemResponse.description,
                            cover = lostFoundsItemResponse.cover,
                            createdAt = lostFoundsItemResponse.createdAt,
                            updatedAt = lostFoundsItemResponse.updatedAt,
                            isCompleted = lostFoundsItemResponse.isCompleted,
                            status = lostFoundsItemResponse.status
                        )
                        viewModel.putObject(
                            lostFoundsItemResponse.id,
                            lostFoundsItemResponse.title,
                            lostFoundsItemResponse.description,
                            lostFoundsItemResponse.status,
                            isChecked
                        ).observeOnce {
                            when (it) {
                                is MyResult.Error -> {
                                    if (isChecked) {
                                        Toast.makeText(
                                            this@ObjectFavoriteActivity,
                                            "Gagal menyelesaikan Object: " + lostFoundsItemResponse.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@ObjectFavoriteActivity,
                                            "Gagal batal menyelesaikan Object: " + lostFoundsItemResponse.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                is MyResult.Success -> {
                                    if (isChecked) {
                                        Toast.makeText(
                                            this@ObjectFavoriteActivity,
                                            "Berhasil menyelesaikan Object: " + lostFoundsItemResponse.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@ObjectFavoriteActivity,
                                            "Berhasil batal menyelesaikan Object: " + lostFoundsItemResponse.title,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    viewModel.insertLocalObject(newObject)
                                }
                                else -> {}
                            }
                        }
                    }
                    override fun onClickDetailListener(objectId: Int) {
                        val intent = Intent(
                            this@ObjectFavoriteActivity,
                            ObjectDetailActivity::class.java
                        )
                        intent.putExtra(ObjectDetailActivity.KEY_OBJECT_ID, objectId)
                        launcher.launch(intent)
                    }
                })
            binding.svLostFoundFavorite.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }
                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvLostFoundFavoriteLostFounds
                            .layoutManager?.scrollToPosition(0)

                        return true
                    }
                })
        }
    }

    private fun showComponentNotEmpty(status: Boolean) {
        binding.svLostFoundFavorite.visibility =
            if (status) View.VISIBLE else View.GONE
        binding.rvLostFoundFavoriteLostFounds.visibility =
            if (status) View.VISIBLE else View.GONE
    }
    private fun showEmptyError(isError: Boolean) {
        binding.tvLostFoundFavoriteEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }
    private fun showLoading(isLoading: Boolean) {
        binding.pbLostFoundFavorite.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
}