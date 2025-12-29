package com.example.merabills


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING
import com.example.merabills.databinding.ItemRowBinding

class RowAdapter(
    private val dataManager: SquareDataManager,
    private val sharedPool: RecyclerView.RecycledViewPool,
    private val onSquareClick: (rowIndex: Int, squareIndex: Int) -> Unit
) : RecyclerView.Adapter<RowAdapter.RowViewHolder>() {

    private var isScrolling = false

    init {
        setHasStableIds(false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val binding = ItemRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RowViewHolder(binding, sharedPool)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(dataManager.getRow(position), position, onSquareClick, isScrolling)
    }

    override fun onBindViewHolder(
        holder: RowViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.updateData(dataManager.getRow(position), true)
        }
    }

    override fun getItemCount(): Int = dataManager.getRowCount()

    override fun onViewRecycled(holder: RowViewHolder) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }

    fun notifyScrollStateChanged(idle: Boolean) {
        val wasScrolling = isScrolling
        isScrolling = !idle

        if (wasScrolling && idle) {
            notifyItemRangeChanged(0, itemCount, PAYLOAD_REFRESH)
        }
    }

    class RowViewHolder(
        private val binding: ItemRowBinding,
        private val sharedPool: RecyclerView.RecycledViewPool
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentAdapter: SquareAdapter? = null
        private var currentRowIndex: Int = -1

        init {
            binding.horizontalRecyclerView.apply {
                val layoutMgr = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                ).apply {
                    isItemPrefetchEnabled = true
                    initialPrefetchItemCount = 4
                }
                layoutManager = layoutMgr
                setHasFixedSize(true)
                setRecycledViewPool(sharedPool)
                setItemViewCacheSize(12)

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    private var lastScrollState = SCROLL_STATE_IDLE

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        if (lastScrollState == SCROLL_STATE_SETTLING && newState == SCROLL_STATE_IDLE) {
                            recyclerView.invalidateItemDecorations()
                        }
                        lastScrollState = newState
                    }
                })
            }
        }

        fun bind(
            row: SquareDataManager.Row,
            rowIndex: Int,
            onSquareClick: (rowIndex: Int, squareIndex: Int) -> Unit,
            isScrolling: Boolean
        ) {
            currentRowIndex = rowIndex

            if (currentAdapter == null) {
                currentAdapter = SquareAdapter(row.squares) { squareIndex ->
                    val currentRow = bindingAdapterPosition
                    if (currentRow != RecyclerView.NO_POSITION) {
                        onSquareClick(currentRow, squareIndex)
                    }
                }
                binding.horizontalRecyclerView.adapter = currentAdapter
            } else {
                currentAdapter?.updateData(row.squares, !isScrolling)
            }
        }

        fun updateData(row: SquareDataManager.Row, useOptimizedUpdate: Boolean) {
            currentAdapter?.updateData(row.squares, useOptimizedUpdate)
        }

        fun onRecycled() {
            binding.horizontalRecyclerView.stopScroll()
        }
    }

    companion object {
        private const val PAYLOAD_REFRESH = "refresh"
    }
}