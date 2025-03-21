package eu.zkkn.android.kaktus.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


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
}
