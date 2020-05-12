package pl.consultantassistant.data.firebase

import android.os.Handler
import androidx.lifecycle.LiveData
import com.google.firebase.database.*
import pl.consultantassistant.utils.LoadingListener


class FirebaseQueryLiveData : LiveData<DataSnapshot> {

    private val query: Query
    private val handler: Handler = Handler()
    private val removeListener: Runnable
    private val listener = MyValueEventListener()
    private var listenerRemovePending = false

    companion object {
        var loadingListener: LoadingListener? = null
    }

    constructor(query: Query) {
        this.query = query
        removeListener = Runnable {
            query.removeEventListener(listener)
            listenerRemovePending = false
        }
    }

    constructor(ref: DatabaseReference) {
        this.query = ref
        removeListener = Runnable {
            query.removeEventListener(listener)
            listenerRemovePending = false
        }
    }

    override fun onActive() {
        if (listenerRemovePending) {
            handler.removeCallbacks(removeListener)
        } else {
            loadingListener?.onStarted()
            query.addValueEventListener(listener)
        }
        listenerRemovePending = false
    }

    override fun onInactive() {
        // Listener removal is scheduled on a two second delay
        handler.postDelayed(removeListener, 2000)
        listenerRemovePending = true
    }

    private inner class MyValueEventListener : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            loadingListener?.onSuccess()
            value = dataSnapshot
        }

        override fun onCancelled(databaseError: DatabaseError) {
            loadingListener?.onCanceled()
        }
    }
}