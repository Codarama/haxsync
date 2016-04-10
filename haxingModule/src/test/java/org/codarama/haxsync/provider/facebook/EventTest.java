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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by tishun on 22.11.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, resourceDir = "./src/test/res")
public class EventTest {

    private JSONObject eventSource = null;

    // TODO setting up a resources directory in gradle, for the love of god, is beyond me
    private static final String content =
            "    {\n" +
            "      \"name\": \"Nightwish - 10 December - Romexpo - Endless Forms Most Beautiful On Tour\",\n" +
            "      \"id\": \"692905047487314\",\n" +
            "      \"start_time\": \"2015-12-10T17:00:00+0200\",\n" +
            "      \"end_time\": \"2015-12-10T23:00:00+0200\",\n" +
            "      \"description\": \"Central Pavilion, Romexpo, Bucharest, Romania, 10th of December 2015, #EFMBtour:\n" +
            "      17:00 - 22:30 Open Doors (acces eveniment)\n" +
            "      18:50 Amorphis (35')\n" +
            "      19:40 Arch Enemy (50')\n" +
            "      21:10 Nightwish (120')\n" +
            "      Be there!\n" +
            "\n" +
            "      Nightwish pornește în turneu alături de Arch Enemy și Amorphis cu o nouă producție, un convoi de tiruri angrenate în cel mai ambițios proiect al formației și un nou album: Endless Forms Most Beautiful alături de Floor Jansen. În noua formulă de succes, Nightwish își va încheia turneul cu un concert pe Wembley Arena din Londra.\n" +
            "\n" +
            "      Din respect pentru #Colectiv, spectacolul Nightwish de la Bucuresti, se va desfasura fara efecte pirotehnice: \\\"Dear people of Romania. We feel really sorry about the club fire and the victims and their families. We want to encourage all of you to carry on, even though it might be really hard at the moment. We truly respect the memory of all the victims. We will not have any pyro at our own show. Peace to all of you.\\\"\n" +
            "\n" +
            "      ARTmania Events prezintă singurul concertul din Balcani ce va avea loc pe 10 decembrie 2015 în Pavilionul Central Romexpo, unul dintre cele 20 de show-uri organizate în turneul de promovare Endless Forms Most Beautiful. Biletele în număr limitat sunt disponibile în rețelele partenere având prețuri între 190 și 380 lei.\n" +
            "\n" +
            "      Parteneri: Concerte TuborgSound, Rock FM, Utv, Zile si Nopti, Urban.ro, Nine O'Clock, Catavencii, Times New Roman, Orasul Meu, MetalfanRomania, 9AM.ro, Mixtopia, infomusic.ro, iConcert.ro, ROL.ro, Calendar Evenimente, Tribuna Sibiu, RockStage Romania, Informația Harghitei, Let's Rock Romania, Mobzine Romania, Musicnights Romania, Prin Galati, anyplace.ro online magazine, ROEvents Media, Rockout, VinSiEu.ro - Evenimente din Romania, dordeduca, artasunetelor.ro, Jurnal ROCK, 220.ro, Metal Hangar 18, Teen Art Out.\n" +
            "\n" +
            "      Endless Forms Most Beautiful starts touring at the end of this year as the newest Nightwish production, one of their most ambitious projects up to date, featuring a route that will include Bucharest shortly after Prague. The tour closes in London on Wembley Arena. ARTmania is presenting the show taking place in Bucharest, Romania on the 10th of December 2015 at Romexpo, one of the 20 concerts promoting the Endless Forms Most Beautiful album.\n" +
            "\n" +
            "      This performance was considerably supported by the artist’s enthusiasm for their Romanian fans and ARTmania’s motivation, trying to offer as many concerts as possible for its dedicated audience during its 10-years anniversary year. Arch Enemy and Amorphis will be warming up the atmosphere for the Nightwish concerts this tour, making the event  more of a festival, the ideal combination for metal music lovers.\n" +
            "\n" +
            "      Endless Forms Most Beautiful is the 8th studio album of the best-known symphonic power metal band and was released in Europe on March 27, 2015. It’s the band’s first album featuring the new lead singer, Floor Jansen and Troy Donockley, who was turned from guest musician to full-time member.\n" +
            "\n" +
            "      It’s also their first album without drummer Jukka Nevalainen, who has taken a break from all recordings and touring activities due to chronic insomnia. Therefore, the drummer position has been filled by Kai Hahto, who Romanian fans have already listen to during the Swallow the Sun concert at ARTmania Festival Sibiu 2010.\n" +
            "\n" +
            "      The album has been inspired, according to Tuomas Holopainen, by a famous quote from the Origin of Species book, released by naturalist Charles Darwin in 1859. This quote included the words “endless forms most beautiful”, used by Darwin to describe the evolution from one common ancestor to all living organisms, that were subsequently chosen as the title of the album.\n" +
            "\n" +
            "      Because the whole band is interested in the subjects of science, the lyrics lean on the areas of biology, evolution and science: „It focuses on the fact that all living beings on earth are related with each other, own a common origin and that’s why everybody is on the same level“, reveals Tuomas. „However, this should not degrade humanity, but symbolize merely the connections of all kinds!“ Nightwish connects the present, the past, and the future in a perfect whole – Endless Forms Most Beautiful.\n" +
            "\n" +
            "      Starting today you can get your ticket directly from our Facebook Page. No more fuzz with vouchers, couriers, extra taxes! See you all on the 10th of December for the one and only Nightwish concert in the neighborhood (South-East Europe). \\m/\n" +
            "\n" +
            "      For Romania: Biletele pentru show-ul Nightwish din 10 decembrie 2015, inclusiv biletele cu locuri în tribune, sunt disponibile în rețelele Eventim.ro, Myticket, la sediul ARTmania Events din București și ramburs, cu livrare prin curier, pe http://nightwish.artmania.ro.\",\n" +
            "    \"rsvp_status\": \"attending\"\n" +
            "    }";

    @Before
    public void initializeTest() throws IOException, JSONException {
        this.eventSource = new JSONObject(content.toString());
    }

    @Test
    public void test() {
        Event event = new Event(eventSource);
        Assert.assertEquals(event.getName(), "Nightwish - 10 December - Romexpo - Endless Forms Most Beautiful On Tour");
        Assert.assertTrue(event.getDescription().startsWith("Central Pavilion, Romexpo, Bucharest, Romania, 10th of December 2015, #EFMB"));
        Assert.assertEquals(692905047487314L, event.getEventID());
        Assert.assertEquals(1, event.getRsvp());
        Assert.assertEquals(1449759600000L, event.getStartTime());
        Assert.assertEquals(1449781200000L, event.getEndTime());
    }
}
