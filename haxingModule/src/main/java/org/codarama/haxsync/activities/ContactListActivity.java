/*
 * Copyright (c) 2016 Codarama.org, All Rights Reserved
 *
 * Codarama HaxSync is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * Codarama HaxSync is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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
