package eu.zkkn.android.kaktus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

import eu.zkkn.android.kaktus.sync.SyncUtils


class MainViewModel(application: Application) : AndroidViewModel(application) {

    fun getLastFacebookPost(): LiveData<FacebookPostsRepository.Data> {
        return FacebookPostsRepository.getLastPost(getApplication())
    }

    fun refreshLastFacebookPost() {
        SyncUtils.startSync(getApplication())
    }

    fun enableFbSync() {
        SyncUtils.enableSync(getApplication())
        refreshLastFacebookPost()
    }

    fun disableFbSync() {
        SyncUtils.disableSync(getApplication())
    }

    fun isFbSyncEnabled(): Boolean {
        return SyncUtils.isSyncEnabled(getApplication())
    }

}
