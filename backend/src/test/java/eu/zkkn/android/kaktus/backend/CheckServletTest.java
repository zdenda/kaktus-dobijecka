package eu.zkkn.android.kaktus.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


class CheckServletTest {

    @Test
    void textMatchesPattern_false() {
        assertFalse(CheckServlet.textMatchesPattern(""));
        assertFalse(CheckServlet.textMatchesPattern("Test"));
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
    }
}
