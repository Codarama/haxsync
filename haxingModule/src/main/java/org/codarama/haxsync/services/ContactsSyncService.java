package org.codarama.haxsync.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * <p>This implementation of the {@link Service} class is used to bind the {@link ContactsSyncAdapter}
 * with the Android Sync framework. Mostly boilerplate code.</p>
 *
 * @see Service
 * @see android.content.AbstractThreadedSyncAdapter
 */
public class ContactsSyncService extends Service {

    // Storage for an instance of the sync adapter
    private static ContactsSyncAdapter syncAdapter = null;

    // Object to use as a thread-safe lock
    private static final Object syncAdapterLock = new Object();

    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new ContactsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return syncAdapter.getSyncAdapterBinder();
    }
}
