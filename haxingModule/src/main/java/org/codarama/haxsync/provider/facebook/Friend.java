package org.codarama.haxsync.provider.facebook;

import java.util.ArrayList;

/**
 * <p>Facebook buddy</p>
 *
 * <p>Contains all the publicly available information for a contact, that is synced</p>
 */
public interface Friend {

    /**
     * @param ignoreMiddleNames if we should ignore the middle name
     *
     * @return the full name of the contact
     */
    String getName(boolean ignoreMiddleNames);

    /**
     * @return the username as it is in Facebook
     */
    String getUserName();

    /**
     * The URL of the profile picture for this contact
     * @return
     */
    String getPicURL();

    /**
     * @return the timestamp of the profile picture
     */
    long getPicTimestamp();

    /**
     * @return an {@link ArrayList} of recent statuses
     */
    ArrayList<Status> getStatuses();
}
