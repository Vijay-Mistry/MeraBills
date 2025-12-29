package com.example.merabills

import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import kotlin.random.Random

class SquareDataManager private constructor(
    private val rows: ArrayList<Row>
) {

    data class Row(val squares: ArrayList<Square>) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.createTypedArrayList(Square.CREATOR) ?: arrayListOf()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeTypedList(squares)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Row> {
            override fun createFromParcel(parcel: Parcel): Row = Row(parcel)
            override fun newArray(size: Int): Array<Row?> = arrayOfNulls(size)
        }
    }

    data class Square(val globalIndex: Int, val color: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(globalIndex)
            parcel.writeInt(color)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Square> {
            override fun createFromParcel(parcel: Parcel): Square = Square(parcel)
            override fun newArray(size: Int): Array<Square?> = arrayOfNulls(size)
        }
    }

    fun getRowCount(): Int = rows.size

    fun getRow(index: Int): Row = rows[index]

    fun removeSquare(rowIndex: Int, squareIndex: Int): Boolean {
        if (rowIndex >= rows.size) return false
        val row = rows[rowIndex]
        if (squareIndex >= row.squares.size) return false

        row.squares.removeAt(squareIndex)
        return true
    }

    fun isRowEmpty(rowIndex: Int): Boolean {
        return rowIndex < rows.size && rows[rowIndex].squares.isEmpty()
    }

    fun removeRow(rowIndex: Int) {
        if (rowIndex < rows.size) {
            rows.removeAt(rowIndex)
        }
    }

    fun save(bundle: Bundle) {
        bundle.putParcelableArrayList(KEY_ROWS, rows)
    }

    companion object {
        private const val KEY_ROWS = "rows"
        private const val TOTAL_SQUARES = 10000
        private const val SQUARES_PER_ROW = 100
        private const val TOTAL_ROWS = TOTAL_SQUARES / SQUARES_PER_ROW

        private const val COLOR_MASK = 0xFF000000.toInt()
        private const val COLOR_MAX = 256

        fun initialize(): SquareDataManager {
            val rows = ArrayList<Row>(TOTAL_ROWS)
            val random = Random.Default
            var currentIndex = 0

            repeat(TOTAL_ROWS) {
                val squares = ArrayList<Square>(SQUARES_PER_ROW)

                repeat(SQUARES_PER_ROW) {
                    squares.add(Square(currentIndex++, generateRandomColor(random)))
                }

                rows.add(Row(squares))
            }

            return SquareDataManager(rows)
        }

        fun restore(bundle: Bundle): SquareDataManager {
            val rows = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelableArrayList(KEY_ROWS, Row::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelableArrayList(KEY_ROWS)
            }

            return if (!rows.isNullOrEmpty()) {
                SquareDataManager(rows)
            } else {
                initialize()
            }
        }

        private fun generateRandomColor(random: Random): Int {
            return COLOR_MASK or
                    (random.nextInt(COLOR_MAX) shl 16) or
                    (random.nextInt(COLOR_MAX) shl 8) or
                    random.nextInt(COLOR_MAX)
        }
    }
}