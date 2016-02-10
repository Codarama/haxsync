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
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.codarama.haxsync.R;

public class AboutPopup extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_popup);
        TextView thanksView = (TextView) findViewById(R.id.thanksView);
        thanksView.setClickable(true);
        thanksView.setMovementMethod(LinkMovementMethod.getInstance());
        thanksView.setText(Html.fromHtml(getString(R.string.thanks)));

		/*AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Copyright (c) 2011 Mathias Roth. \n" +
				"Uses Code by Sam Steele (www.c99.org) licensed under the Apache Public license.");
		dialog.show();*/
    }

}
