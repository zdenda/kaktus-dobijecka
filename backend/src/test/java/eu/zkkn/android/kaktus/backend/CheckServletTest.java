package eu.zkkn.android.kaktus.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;


class CheckServletTest {

    @Test
    void textMatchesPattern_false() {
        assertFalse(CheckServlet.textMatchesPattern(""));
        assertFalse(CheckServlet.textMatchesPattern("Test"));
        assertFalse(CheckServlet.textMatchesPattern("dneska"));
        assertFalse(CheckServlet.textMatchesPattern(" dvojnásob "));
        assertFalse(CheckServlet.textMatchesPattern("15. 9."));
        assertFalse(CheckServlet.textMatchesPattern(" 15.9. "));
        assertFalse(CheckServlet.textMatchesPattern(" 15. 9. "));
        assertFalse(CheckServlet.textMatchesPattern(" 2. 2. 2023 "));
        assertFalse(CheckServlet.textMatchesPattern("2x dneska 15. 9."));
        assertFalse(CheckServlet.textMatchesPattern("*Bonusový kredit za dobití z, kvůli technickým problémům původně zrušené dobíječky, 9. 6. 2023 mezi 15 a 18 hodinou bude připsán do půlnoci 12. 6. 2023. Omlouváme se za případné nepříjemnosti."));
    }

    @Test
    void textMatchesPattern_true() {
        assertTrue(CheckServlet.textMatchesPattern("Pokud si dneska 22.7. od 16:00 do 20:00 hodin dobiješ alespoň 200 Kč, dáme ti dvojnásob."));
        assertTrue(CheckServlet.textMatchesPattern("Pokud si dneska 30. 5. 2023 od 16:00 do 18:00 hodin dobiješ alespoň 200 Kč, dáme ti dvojnásob ;)"));
        // Stačí dobít dnes 20. 6. mezi 15 a 17 hodinou 200 - 500 kaček a pak už jen rozjet pořádný pekla s dvojnásobným kreditem. 😈💚
        assertTrue(CheckServlet.textMatchesPattern("Stačí dobít dnes 20. 6. mezi 15 a 17 hodinou 200 - 500 kaček a pak už jen rozjet pořádný pekla s dvojnásobným kreditem. \uD83D\uDE08\uD83D\uDC9A"));
        // Postačí, když si dneska 26. 6. dobiješ za 200 - 500 Kč mezi 17 a 19 a my ti aktivujem 2x tolik.🤩
        assertTrue(CheckServlet.textMatchesPattern("Postačí, když si dneska 26. 6. dobiješ za 200 - 500 Kč mezi 17 a 19 a my ti aktivujem 2x tolik.\uD83E\uDD29"));
        // Stačí dnes 11. 7. naladit 200 - 500 kaček mezi 16 a 19 hodinou a Kaktus ti nabrnkne 2x takovej nářez.🔥
        assertTrue(CheckServlet.textMatchesPattern("Stačí dnes 11. 7. naladit 200 - 500 kaček mezi 16 a 19 hodinou a Kaktus ti nabrnkne 2x takovej nářez.\uD83D\uDD25"));
        // Udělej randál 💦 s dvojitym kreditem! Postačí dnes 25. 7. dobít mezi 17 a 19 hodinou 200 - 500 kaček a my ti nalejem 2x tolik.💦💸
        assertTrue(CheckServlet.textMatchesPattern("Udělej randál \uD83D\uDCA6 s dvojitym kreditem! Postačí dnes 25. 7. dobít mezi 17 a 19 hodinou 200 - 500 kaček a my ti nalejem 2x tolik.\uD83D\uDCA6\uD83D\uDCB8"));
        // Probuď v sobě kreditovýho ninju! 🐢 Dobij si dnes 10. 8. od 17 do 20 hodin 2 až 5 kil a nauč se prastarýmu umění dvojitýho kreditu.
        assertTrue(CheckServlet.textMatchesPattern("Probuď v sobě kreditovýho ninju! \uD83D\uDC22 Dobij si dnes 10. 8. od 17 do 20 hodin 2 až 5 kil a nauč se prastarýmu umění dvojitýho kreditu."));

        // This text doesn't contain any date
        // Udělej ze svýho kreditu pořádný žihadlo. 😎 Podráždi ho 2 až 5 stovkama mezi 16 a 18 hodinou a my už ti píchnem, aby byl 2x takovej. 🐝
        //assertTrue(CheckServlet.textMatchesPattern("Udělej ze svýho kreditu pořádný žihadlo. \uD83D\uDE0E Podráždi ho 2 až 5 stovkama mezi 16 a 18 hodinou a my už ti píchnem, aby byl 2x takovej. \uD83D\uDC1D"));
        // But it was later fixed, and the date was added
        // Udělej ze svýho kreditu pořádný žihadlo. 😎 Podráždi ho 2 až 5 stovkama dneska 21. 8. mezi 16 a 18 hodinou a my už ti píchnem, aby byl 2x takovej. 🐝
        assertTrue(CheckServlet.textMatchesPattern("Udělej ze svýho kreditu pořádný žihadlo. \uD83D\uDE0E Podráždi ho 2 až 5 stovkama dneska 21. 8. mezi 16 a 18 hodinou a my už ti píchnem, aby byl 2x takovej. \uD83D\uDC1D"));

        // Nakopni svůj kredit dvakrát takovou náloží. 💥 Dobij dnes 13. 9. mezi 17 a 19 hodinou 200 až 500 Kč a my ti nasolíme 🧂 tuplovanou sumu, ani nemrkneš. 🦾🌵
        assertTrue(CheckServlet.textMatchesPattern("Nakopni svůj kredit dvakrát takovou náloží. \uD83D\uDCA5 Dobij dnes 13. 9. mezi 17 a 19 hodinou 200 až 500 Kč a my ti nasolíme \uD83E\uDDC2 tuplovanou sumu, ani nemrkneš. \uD83E\uDDBE\uD83C\uDF35"));

        // Udělej díru do světa 🌍 nebo jiný libovolný planety s dvojitym kreditem. Stačí chytit dobíječku dneska 19. 8. mezi 17 a 19 hodinou a pyšnit se intergalaktickou 🚀 porcí kreditu.
        assertTrue(CheckServlet.textMatchesPattern("Udělej díru do světa \uD83C\uDF0D nebo jiný libovolný planety s dvojitym kreditem. Stačí chytit dobíječku dneska 19. 8. mezi 17 a 19 hodinou a pyšnit se intergalaktickou \uD83D\uDE80 porcí kreditu."));

        // Vejdi v dobíječkový pokušení. Dvojitej kredit, dneska 26. 11. mezi 16. - 18. hodinou a dobítí za 200 - 500 Kč. Ty víš, co máš dělat. 👹
        assertTrue(CheckServlet.textMatchesPattern("Vejdi v dobíječkový pokušení. Dvojitej kredit, dneska 26. 11. mezi 16. - 18. hodinou a dobítí za 200 - 500 Kč. Ty víš, co máš dělat. \uD83D\uDC79"));

        // Budoucnost je tady. 🚀 Od teď umíme klonovat kredity! Vyzkoušej to i ty dnes 10. 3. mezi 17 a 20. Stačí dobít 200 - 500 Kč a máš jednou tolik. 😎🤟
        assertTrue(CheckServlet.textMatchesPattern("Budoucnost je tady. \uD83D\uDE80 Od teď umíme klonovat kredity! Vyzkoušej to i ty dnes 10. 3. mezi 17 a 20. Stačí dobít 200 - 500 Kč a máš jednou tolik. \uD83D\uDE0E\uD83E\uDD1F"));

    }

    @Test
    void linkMatchesPattern_false() {
        assertFalse(CheckServlet.linkMatchesPattern(""));
        assertFalse(CheckServlet.linkMatchesPattern("Test"));
        assertFalse(CheckServlet.linkMatchesPattern("https://www.mujkaktus.cz/api/download?docUrl=%2Fapi%2Fdocuments%2Ffile%2FOP-Odmena-za-dobiti-FB_04062025.pdf&filename=OP-Odmena-za-dobiti-FB_04062025.pdf+"));
        assertFalse(CheckServlet.linkMatchesPattern("https://www.mujkaktus.cz/api/download?docUrl=%2Fapi%2Fdocuments%2Ffile%2FOP-Odmena-za-dobiti-FB_00062025.pdf&filename=OP-Odmena-za-dobiti-FB_00062025.pdf"));
        assertFalse(CheckServlet.linkMatchesPattern("https://www.mujkaktus.cz/api/download?docUrl=%2Fapi%2Fdocuments%2Ffile%2FOP-Odmena-za-dobiti-FB_01132025.pdf&filename=OP-Odmena-za-dobiti-FB_01132025.pdf"));
        assertFalse(CheckServlet.linkMatchesPattern("https://www.mujkaktus.cz/api/download?docUrl=%2Fapi%2Fdocuments%2Ffile%2FOP-Odmena-za-dobiti-FB_32122025.pdf&filename=OP-Odmena-za-dobiti-FB_32122025.pdf"));
        //TODO assertFalse(CheckServlet.linkMatchesPattern("https://www.mujkaktus.cz/api/download?docUrl=%2Fapi%2Fdocuments%2Ffile%2FOP-Odmena-za-dobiti-FB_29022025.pdf&filename=OP-Odmena-za-dobiti-FB_29022025.pdf"));
    }

    @Test
    void linkMatchesPattern_true() {
        assertTrue(CheckServlet.linkMatchesPattern("https://www.mujkaktus.cz/api/download?docUrl=%2Fapi%2Fdocuments%2Ffile%2FOP-Odmena-za-dobiti-FB_04062025.pdf&filename=OP-Odmena-za-dobiti-FB_04062025.pdf"));
        assertTrue(CheckServlet.linkMatchesPattern("https://www.mujkaktus.cz/api/download?docUrl=%2Fapi%2Fdocuments%2Ffile%2FOP-Odmena-za-dobiti-FB_31122025.pdf&filename=OP-Odmena-za-dobiti-FB_31122025.pdf"));
    }

    @Test
    void containsDate_false() {
        assertFalse(CheckServlet.containsDate("9.7.2025 16:00 - 18:00", new Date()));
        assertFalse(CheckServlet.containsDate("9.7.2025 16:00 - 18:00", Date.from(LocalDate.of(2025, Month.JULY, 7).atStartOfDay(ZoneId.of("Europe/Prague")).toInstant())));
        assertFalse(CheckServlet.containsDate("9. 7. 2025 16:00 - 18:00", Date.from(LocalDate.of(2025, Month.JUNE, 9).atStartOfDay(ZoneId.of("Europe/Prague")).toInstant())));
        assertFalse(CheckServlet.containsDate("9. 7. 25 16:00 - 18:00", Date.from(LocalDate.of(2024, Month.JULY, 9).atStartOfDay(ZoneId.of("Europe/Prague")).toInstant())));
    }

    @Test
    void containsDate_true() {
        assertTrue(CheckServlet.containsDate("9.7.2025 16:00 - 18:00", Date.from(LocalDate.of(2025, Month.JULY, 9).atStartOfDay(ZoneId.of("Europe/Prague")).toInstant())));
        assertTrue(CheckServlet.containsDate("31. 12. 2025 00:00 - 23:59", Date.from(LocalDate.of(2025, Month.DECEMBER, 31).atStartOfDay(ZoneId.of("Europe/Prague")).toInstant())));
        assertTrue(CheckServlet.containsDate("31.12.25 00:00 - 23:59", Date.from(LocalDate.of(2025, Month.DECEMBER, 31).atStartOfDay(ZoneId.of("Europe/Prague")).toInstant())));
        assertTrue(CheckServlet.containsDate("9. 7. 25 16:00 - 18:00", Date.from(LocalDate.of(2025, Month.JULY, 9).atStartOfDay(ZoneId.of("Europe/Prague")).toInstant())));
        assertTrue(CheckServlet.containsDate("Dobíječka 9.7.2025 16:00 - 18:00", Date.from(LocalDate.of(2025, Month.JULY, 9).atStartOfDay(ZoneId.of("Europe/Prague")).toInstant())));
    }

    @Test
    void timeInfoMatchesPattern_false() {
        assertFalse(CheckServlet.timeInfoMatchesPattern(""));
        assertFalse(CheckServlet.timeInfoMatchesPattern("Test"));
        assertFalse(CheckServlet.timeInfoMatchesPattern("32.12.2025 16:00 - 18:00"));
        assertFalse(CheckServlet.timeInfoMatchesPattern("1. 13. 2025 16:00 - 18:00"));
        assertFalse(CheckServlet.timeInfoMatchesPattern("1.1.25 00:60 - 18:00"));
        assertFalse(CheckServlet.timeInfoMatchesPattern("1.1.2025 16 - 25"));
        assertFalse(CheckServlet.timeInfoMatchesPattern(" 9.7.2025 16:00 - 18:00 "));
        //TODO assertFalse(CheckServlet.timeInfoMatchesPattern("29.2.2025 16:00 - 18:00"));
    }

    @Test
    void timeInfoMatchesPattern_true() {
        assertTrue(CheckServlet.timeInfoMatchesPattern("9.7.2025 16:00 - 18:00"));
        assertTrue(CheckServlet.timeInfoMatchesPattern("31.12.2099 00:00 - 23:59"));
        assertTrue(CheckServlet.timeInfoMatchesPattern("9.7.25 16:00 - 18:00"));
        assertTrue(CheckServlet.timeInfoMatchesPattern("9. 7. 2025 16:00 - 18:00"));
        assertTrue(CheckServlet.timeInfoMatchesPattern("9.7.2025 6:00 - 8:00"));
        assertTrue(CheckServlet.timeInfoMatchesPattern("9.7.2025 06:30 - 08:30"));
        assertTrue(CheckServlet.timeInfoMatchesPattern("9.7.2025 16 - 18"));
        assertTrue(CheckServlet.timeInfoMatchesPattern("9.7.25 16 - 18"));
        assertTrue(CheckServlet.timeInfoMatchesPattern("9. 7. 25 6 - 8"));
        assertTrue(CheckServlet.timeInfoMatchesPattern("9. 7. 25 06 - 08"));
    }

    @Test
    void parseTimeInfo_error() {
        assertNull(CheckServlet.parseTimeInfo(""));
        assertNull(CheckServlet.parseTimeInfo("Test"));
        assertNull(CheckServlet.parseTimeInfo("31.2.2025 16:00 - 18:00"));
    }

    @Test
    void parseTimeInfo_ok() {
        TimeInfo timeInfo = new TimeInfo(
                ZonedDateTime.of(2025, 7, 9, 16, 0, 0, 0, ZoneId.of("Europe/Prague")),
                ZonedDateTime.of(2025, 7, 9, 18, 0, 0, 0, ZoneId.of("Europe/Prague")));

        assertEquals(timeInfo, CheckServlet.parseTimeInfo("9.7.2025 16:00 - 18:00"));
        assertEquals(timeInfo, CheckServlet.parseTimeInfo("9. 7. 2025 16:00 - 18:00"));

        // TODO:
        //assertEquals(timeInfo, CheckServlet.parseTimeInfo("9. 7. 2025 16 - 18"));
        //assertEquals(timeInfo, CheckServlet.parseTimeInfo("9. 7. 25 16 - 18"));
        // and other cases from timeInfoMatchesPattern_true()
    }

}
