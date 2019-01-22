package eu.zkkn.android.kaktus

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import eu.zkkn.android.kaktus.sync.SyncUtils

import java.util.Date


/**
 *
 */
object FacebookPostsRepository {

    class FbPost(
            var date: Date,
            var text: String?,
            var imageUrl: String?
    )

    class Data private constructor(
            val status: Status,
            val data: FbPost? = null,
            val message: String? = null
    ){

        enum class Status { SUCCESS, EMPTY, ERROR, LOADING }

        companion object {

            fun success(data: FbPost): Data {
                return Data(Status.SUCCESS, data)
            }

            fun empty(): Data {
                return Data(Status.EMPTY)
            }

            fun error(message: String, data: FbPost?): Data {
                return Data(Status.ERROR, data, message)
            }

            fun loading(data: FbPost?): Data {
                return Data(Status.LOADING, data)
            }

        }

    }


    private val TAG = FacebookPostsRepository::class.simpleName

    private val lastPost: MutableLiveData<Data> = MutableLiveData()


    fun getLastPost(context: Context): LiveData<Data> {
        if (lastPost.value?.data == null) {
            val lastFbPost = Preferences.getLastFbPost(context)
            if (SyncUtils.isSyncActive(context)) {
                lastPost.value = Data.loading(lastFbPost)
            } else {
                lastPost.value = if (lastFbPost != null) Data.success(lastFbPost) else Data.empty()
            }
        }
        return lastPost
    }

    fun saveLastPost(context: Context, fbPost: FbPost) {
        //TODO: use ContentProvider
        Preferences.setLastFbPost(context, fbPost)
        lastPost.postValue(Data.success(fbPost))
    }

    @JvmOverloads
    fun setErrorLastFacebookPost(message: String, data: FbPost? = null) {
        lastPost.postValue(Data.error(message, data ?: lastPost.value?.data))
    }

    fun setLoadingLastFacebookPost() {
        lastPost.postValue(Data.loading(lastPost.value?.data))
    }

}
