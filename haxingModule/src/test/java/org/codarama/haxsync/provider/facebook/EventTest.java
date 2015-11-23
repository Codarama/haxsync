/*
 * Copyright (c) 2015 Codarama.Org, All Rights Reserved
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 */

package org.codarama.haxsync.provider.facebook;

import junit.framework.Assert;

import org.codarama.haxsync.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Scanner;

/**
 * Created by tishun on 22.11.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class EventTest {

    private JSONObject eventSource = null;

    @Before
    public void initializeTest() throws IOException, JSONException {
        StringBuilder content = new StringBuilder();
        FileReader fileReader = null;
        BufferedReader reader = null;

        try {
            String fileName = this.getClass().getClassLoader().getResource("event.json").getPath();
            fileReader = new FileReader(fileName);
            reader = new BufferedReader(fileReader);
            while (reader.ready()) {
                content.append(reader.readLine());
            }
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
            if (reader != null) {
                reader.close();
            }
        }

        this.eventSource = new JSONObject(content.toString());
    }

    @Test
    public void test() {
        Event event = new Event(eventSource);
        Assert.assertEquals(event.getName(), "Nightwish - 10 December - Romexpo - Endless Forms Most Beautiful On Tour");
        Assert.assertTrue(event.getDescription().startsWith("Central Pavilion, Romexpo, Bucharest, Romania, 10th of December 2015, #EFMB"));
        Assert.assertEquals(event.getEventID(), 692905047487314L);
        Assert.assertEquals(event.getRsvp(), "attending");
        Assert.assertEquals(event.getStartTime(), 1);
        Assert.assertEquals(event.getEndTime(), 1);
    }
}
