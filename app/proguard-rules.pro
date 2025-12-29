# Keep data classes used for state preservation
-keepclassmembers class com.example.squareview.SquareDataManager$** {
    *;
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Optimize
-optimizationpasses 5
-dontpreverify

# Keep ViewBinding classes
-keep class com.example.squareview.databinding.** { *; }

# RecyclerView optimizations
-keep class androidx.recyclerview.widget.RecyclerView { *; }
-keep class androidx.recyclerview.widget.RecyclerView$** { *; }