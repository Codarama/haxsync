package org.codarama.haxsync.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import org.codarama.haxsync.R;
import org.codarama.haxsync.fragments.ContactDetailFragment;
import org.codarama.haxsync.fragments.ContactListFragment;

public class ContactListActivity extends Activity
        implements ContactListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        if (findViewById(R.id.contact_detail_container) != null) {
            mTwoPane = true;

            ((ContactListFragment) getFragmentManager()
                    .findFragmentById(R.id.contact_list))
                    .setActivateOnItemClick(true);

        }
    }

    @Override
    public void onItemSelected(long id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putLong(ContactDetailFragment.CONTACT_ID, id);
            Fragment fragment = new ContactDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.contact_detail_container, fragment)
                    .commitAllowingStateLoss();

        } else {
            Intent detailIntent = new Intent(this, ContactDetailActivity.class);
            detailIntent.putExtra(ContactDetailFragment.CONTACT_ID, id);
            startActivity(detailIntent);
        }
    }


}
