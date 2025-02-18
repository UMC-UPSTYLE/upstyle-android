import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item_result(
    val description: String,
    val imageUrl: String,
    val kindId: Int?,
    val kindName: String?,
    val categoryId: Int?,
    val categoryName: String?,
    val fitId: Int?,
    val fitName: String?,
    val colorId: Int?,
    val colorName: String?
)
 : Parcelable
