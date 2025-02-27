import android.os.Parcelable
import com.umc.upstyle.data.model.Ootd
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item_result(
    val id: Int,
    val description: String,
    val imageUrl: String,
    val kindId: Int?,
    val kindName: String?,
    val categoryId: Int?,
    val categoryName: String?,
    val fitId: Int?,
    val fitName: String?,
    val colorId: Int?,
    val colorName: String?,
    val ootd: Ootd,
)
 : Parcelable
