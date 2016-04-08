package org.codarama.haxsync.entities;

/**
 * <p>Facebook buddy</p>
 * <p>Contains all the publicly available information for a contact, that is synced</p>
 */
public interface ProfilePicture {

    /**
     * @return The ID of this contact in the list of contacts
     */
    Long getGoogleId();

    /**
     * @return the URL pointing to where the profile picture is stored at
     */
    String getURL();

    /**
     * @return the height of the profile picture as it is at the remote URL address
     */
    long getHeight();

    /**
     * @return the width of the profile picture as it is at the remote URL address
     */
    long getWidth();
}
