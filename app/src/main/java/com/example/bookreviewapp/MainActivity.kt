package com.example.bookreviewapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.bookreviewapp.adapter.BookAdapter
import com.example.bookreviewapp.adapter.HistoryAdapter
import com.example.bookreviewapp.api.BookService
import com.example.bookreviewapp.databinding.ActivityMainBinding
import com.example.bookreviewapp.model.BestSellerDto
import com.example.bookreviewapp.model.History
import com.example.bookreviewapp.model.SearchBookDto
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BookAdapter
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var bookService: BookService

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBookRecyclerView()
        initHistoryRecyclerView()

        db = getAppDatabase(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://book.interpark.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)

        bookService.getBestSellerBooks(getString(R.string.interparkApikey))
            .enqueue(object: Callback<BestSellerDto>{

//                api 요청 성공 -> 성공 처리에 대한 코드 작성
                override fun onResponse(
                    call: Call<BestSellerDto>,
                    response: Response<BestSellerDto>
                ) {
                    if(response.isSuccessful.not()) {
                        Log.e(TAG, "Not !! Success")
                        return
                    }

                    response.body()?.let {
                        Log.d(TAG, it.toString())
                        it.books.forEach { book ->
                            Log.d(TAG, book.toString())
                        }
                        adapter.submitList(it.books)
                    }

                }

//                api 요청 실패 -> 실패 처리에 대한 코드 작성
                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                    Log.e(TAG, t.toString())
                }

            })


    }

    private fun search(keyword: String) {
        bookService.getBooksByName(getString(R.string.interparkApikey), keyword)
            .enqueue(object: Callback<SearchBookDto>{

                //                api 요청 성공 -> 성공 처리에 대한 코드 작성
                override fun onResponse(call: Call<SearchBookDto>, response: Response<SearchBookDto>) {
                    if(response.isSuccessful.not()) {
                        Log.e(TAG, "Not !! Success")
                        return
                    }
                    hideHistoryView()
                    saveSearchKeyword(keyword)

                    adapter.submitList(response.body()?.books.orEmpty())

                }

                //                api 요청 실패 -> 실패 처리에 대한 코드 작성
                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    hideHistoryView()
                    Log.e(TAG, t.toString())
                }
            })
    }

    private fun initBookRecyclerView() {
        adapter = BookAdapter(itemClickedListener = {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("bookModel", it)
            startActivity(intent)
        })
        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = adapter
    }

    private fun initHistoryRecyclerView() {
        historyAdapter = HistoryAdapter(historyDeleteClickedListener = {
            deleteSearchKeyword(it)
        })

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter
        initSearchEditText()
    }

    private fun showHistoryView() {
        Thread{
            val keywords = db.historyDao().getAll().reversed()

            runOnUiThread{
                binding.historyRecyclerView.isVisible = true
                historyAdapter.submitList(keywords.orEmpty())
            }
        }.start()

        binding.historyRecyclerView.isVisible = true
    }

    private fun hideHistoryView() {
        binding.historyRecyclerView.isVisible = false
    }

    private fun saveSearchKeyword(keyword: String) {
        Thread{
            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }

    private fun deleteSearchKeyword(keyword: String) {
        Thread{
            db.historyDao().delete(keyword)
            showHistoryView()
        }.start()
    }

    private fun initSearchEditText() {
        binding.searchEditText.setOnKeyListener { v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN) {
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        binding.searchEditText.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN) {
                showHistoryView()
            }
            return@setOnTouchListener false
        }
    }

    companion object{
        private const val TAG = "MainActivity"

    }

}