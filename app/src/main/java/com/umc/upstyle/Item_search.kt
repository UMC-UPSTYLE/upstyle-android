import android.os.Parcelable
import com.umc.upstyle.data.model.Ootd
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item_search(
    val description: String,
    val imageUrl: String,
    val categoryId: Int? = null, // ✅ 새롭게 추가
    val fitId: Int? = null,      // ✅ 새롭게 추가
    val colorId: Int? = null     // ✅ 새롭게 추가

) : Parcelable
