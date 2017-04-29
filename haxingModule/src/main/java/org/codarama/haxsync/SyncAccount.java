package org.codarama.haxsync;

import android.accounts.Account;
import android.accounts.AccountManager;

/**
 * Created by tishu on 29-Dec-16.
 */

public class SyncAccount {

    private AccountManager accountManager;

    public SyncAccount(AccountManager manager) {
        this.accountManager = manager;
    }

    public Account getHaxSyncAccount(String accountName) {
        Account[] accounts = accountManager.getAccountsByType(accountName);
        return accounts.length > 0 ? accounts[0] : null;
    }
}
