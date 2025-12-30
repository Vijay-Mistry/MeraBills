package com.example.merabills


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.merabills.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rowAdapter: RowAdapter
    private lateinit var dataManager: SquareDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = if (savedInstanceState != null) {
            SquareDataManager.restore(savedInstanceState)
        } else {
            SquareDataManager.initialize()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            setItemViewCacheSize(20)

            val pool = recycledViewPool
            pool.setMaxRecycledViews(0, 25)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        rowAdapter.notifyScrollStateChanged(true)
                    } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        rowAdapter.notifyScrollStateChanged(false)
                    }
                }
            })
        }

        rowAdapter = RowAdapter(dataManager, createSharedPool()) { rowIndex, squareIndex ->
            handleSquareClick(rowIndex, squareIndex)
        }
        binding.recyclerView.adapter = rowAdapter
    }

    private fun createSharedPool(): RecyclerView.RecycledViewPool {
        return RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(0, 100)
        }
    }

    private fun handleSquareClick(rowIndex: Int, squareIndex: Int) {
        val removed = dataManager.removeSquare(rowIndex, squareIndex)

        if (removed) {
            if (dataManager.isRowEmpty(rowIndex)) {
                dataManager.removeRow(rowIndex)
                rowAdapter.notifyItemRemoved(rowIndex)
            } else {
                val viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(rowIndex) as? RowAdapter.RowViewHolder
                val squareAdapter = viewHolder?.getSquareAdapter()
                squareAdapter?.notifyItemRemoved(squareIndex)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        dataManager.save(outState)
    }
}