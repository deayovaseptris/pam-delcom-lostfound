package com.ifs21017.lostfound.presentation.main

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
import com.ifs21017.lostfound.data.remote.MyResult
import com.ifs21017.lostfound.data.remote.response.DelcomObjectsResponse
import com.ifs21017.lostfound.data.remote.response.LostFoundsItemResponse
import com.ifs21017.lostfound.databinding.ActivityMainBinding
import com.ifs21017.lostfound.helper.Utils.Companion.observeOnce
import com.ifs21017.lostfound.presentation.ViewModelFactory
import com.ifs21017.lostfound.presentation.login.LoginActivity
import com.ifs21017.lostfound.presentation.lostfound.ObjectDetailActivity
import com.ifs21017.lostfound.presentation.lostfound.ObjectFavoriteActivity
import com.ifs21017.lostfound.presentation.lostfound.ObjectManageActivity
import com.ifs21017.lostfound.presentation.profile.ProfileActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ObjectManageActivity.RESULT_CODE) {
            recreate()
        }
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }
    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)
        binding.appbarMain.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)
        observeGetObjects()
    }

    private fun setupAction() {
        binding.appbarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mainMenuProfile -> {
                    openProfileActivity()
                    true
                }
                R.id.mainMenuLogout -> {
                    viewModel.logout()
                    openLoginActivity()
                    true
                }
                R.id.mainMenuFavoriteTodos -> {
                    openFavoriteObjectActivity()
                    true
                }
                else -> false
            }
        }
        binding.fabMainAddObject.setOnClickListener {
            openAddObjectActivity()
        }
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                openLoginActivity()
            } else {
                observeGetObjects()
            }
        }
    }
    private fun observeGetObjects() {
        viewModel.getObjects().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }
                    is MyResult.Success -> {
                        showLoading(false)
                        loadObjectsToLayout(result.data)
                    }
                    is MyResult.Error -> {
                        showLoading(false)
                        showEmptyError(true)
                    }
                }
            }
        }
    }
    private fun loadObjectsToLayout(response: DelcomObjectsResponse) {
        val objects = response.data.lostFounds
        val layoutManager = LinearLayoutManager(this)
        binding.rvMainObjects.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvMainObjects.addItemDecoration(itemDecoration)
        if (objects.isEmpty()) {
            showEmptyError(true)
            binding.rvMainObjects.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)
            val adapter = ObjectsAdapter()
            adapter.submitOriginalList(objects)
            binding.rvMainObjects.adapter = adapter
            adapter.setOnItemClickCallback(object : ObjectsAdapter.OnItemClickCallback {
                override fun onCheckedChangeListener(
                    lostfound: LostFoundsItemResponse,
                    isChecked: Boolean
                ) {
                    adapter.filter(binding.svMain.query.toString())
                    viewModel.putObject(
                        lostfound.id,
                        lostfound.title,
                        lostfound.description,
                        lostfound.status,
                        isChecked
                    ).observeOnce {
                        when (it) {
                            is MyResult.Error -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Gagal menyelesaikan object: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Gagal batal menyelesaikan object: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            is MyResult.Success -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil menyelesaikan object: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil batal menyelesaikan object: " + lostfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            else -> {}
                        }
                    }
                }
                override fun onClickDetailListener(lostfoundId: Int) {
                    val intent = Intent(
                        this@MainActivity,
                        ObjectDetailActivity::class.java
                    )
                    intent.putExtra(ObjectDetailActivity.KEY_OBJECT_ID, lostfoundId)
                    launcher.launch(intent)
                }
            })
            binding.svMain.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }
                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvMainObjects.layoutManager?.scrollToPosition(0)
                        return true
                    }
                })
        }
    }
    private fun showLoading(isLoading: Boolean) {
        binding.pbMain.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
    private fun openProfileActivity() {
        val intent = Intent(applicationContext, ProfileActivity::class.java)
        startActivity(intent)
    }
    private fun showComponentNotEmpty(status: Boolean) {
        binding.svMain.visibility =
            if (status) View.VISIBLE else View.GONE
        binding.rvMainObjects.visibility =
            if (status) View.VISIBLE else View.GONE
    }
    private fun showEmptyError(isError: Boolean) {
        binding.tvMainEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }
    private fun openLoginActivity() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    private fun openAddObjectActivity() {
        val intent = Intent(
            this@MainActivity,
            ObjectManageActivity::class.java
        )
        intent.putExtra(ObjectManageActivity.KEY_IS_ADD, true)
        launcher.launch(intent)
    }
    private fun openFavoriteObjectActivity() {
        val intent = Intent(
            this@MainActivity,
            ObjectFavoriteActivity::class.java
        )
        launcher.launch(intent)
    }
}