package com.example.merabills


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.merabills.databinding.ItemSquareBinding

class SquareAdapter(
    private var squares: List<SquareDataManager.Square>,
    private val onSquareClick: (squareIndex: Int) -> Unit
) : RecyclerView.Adapter<SquareAdapter.SquareViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SquareViewHolder {
        val binding = ItemSquareBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SquareViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SquareViewHolder, position: Int) {
        holder.bind(squares[position], onSquareClick)
    }

    override fun getItemCount(): Int = squares.size

    override fun getItemId(position: Int): Long {
        return squares[position].globalIndex.toLong()
    }

    fun updateData(newSquares: List<SquareDataManager.Square>, useOptimizedUpdate: Boolean) {
        if (!useOptimizedUpdate) {
            val oldSize = squares.size
            val newSize = newSquares.size
            squares = newSquares

            when {
                oldSize == newSize -> notifyItemRangeChanged(0, newSize, PAYLOAD_UPDATE)
                oldSize > newSize -> {
                    val sizeDiff = oldSize - newSize
                    notifyItemRangeChanged(0, newSize, PAYLOAD_UPDATE)
                    notifyItemRangeRemoved(newSize, sizeDiff)
                }
                else -> {
                    notifyItemRangeChanged(0, oldSize, PAYLOAD_UPDATE)
                    notifyItemRangeInserted(oldSize, newSize - oldSize)
                }
            }
            return
        }

        val oldSquares = squares
        squares = newSquares

        if (oldSquares.isEmpty() && newSquares.isEmpty()) return

        if (oldSquares.isEmpty()) {
            notifyItemRangeInserted(0, newSquares.size)
            return
        }

        if (newSquares.isEmpty()) {
            notifyItemRangeRemoved(0, oldSquares.size)
            return
        }

        val diffCallback = SquareDiffCallback(oldSquares, newSquares)
        val diffResult = DiffUtil.calculateDiff(diffCallback, true)
        diffResult.dispatchUpdatesTo(this)
    }

    class SquareViewHolder(private val binding: ItemSquareBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentIndex: Int = -1

        fun bind(
            square: SquareDataManager.Square,
            onSquareClick: (squareIndex: Int) -> Unit
        ) {
            if (currentIndex != square.globalIndex) {
                binding.squareView.setBackgroundColor(square.color)
                binding.textView.text = square.globalIndex.plus(1).toString()
                currentIndex = square.globalIndex
            }

            binding.squareView.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onSquareClick(currentPosition)
                }
            }
        }
    }

    private class SquareDiffCallback(
        private val oldList: List<SquareDataManager.Square>,
        private val newList: List<SquareDataManager.Square>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].globalIndex == newList[newItemPosition].globalIndex
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return PAYLOAD_UPDATE
        }
    }

    companion object {
        private const val PAYLOAD_UPDATE = "update"
    }
}