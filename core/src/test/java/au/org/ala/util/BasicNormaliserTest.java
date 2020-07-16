

package au.org.ala.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for normalisation
 */
public class BasicNormaliserTest {

    @Test
    public void testNormaliseSpaces1() {
        BasicNormaliser normaliser = new BasicNormaliser(true, false, false, false, false, false);
        assertEquals("Something good", normaliser.normalise("Something good"));
        assertEquals("Something good", normaliser.normalise("   Something    good    "));
        assertEquals("‘Something good’", normaliser.normalise("‘Something good’"));
        assertEquals("“Something good”", normaliser.normalise("“Something good”"));
        assertEquals("Something—good", normaliser.normalise("Something—good"));
        assertEquals("Something good α", normaliser.normalise("Something good α"));
        assertEquals("ẞomething good α", normaliser.normalise("ẞomething good α"));
        assertEquals("Garçon désolé", normaliser.normalise("Garçon désolé"));
    }

    @Test
    public void testNormalisePunctuation1() {
        BasicNormaliser normaliser = new BasicNormaliser(false, true, false, false, false, false);
        assertEquals("Something good", normaliser.normalise("Something good"));
        assertEquals("Something    good", normaliser.normalise("   Something    good    ")); // Trims
        assertEquals("'Something good'", normaliser.normalise("‘Something good’"));
        assertEquals("\"Something good\"", normaliser.normalise("“Something good”"));
        assertEquals("Something-good", normaliser.normalise("Something—good")); // Normal dash replaces em-dash
        assertEquals("Something good α", normaliser.normalise("Something good α"));
        assertEquals("ẞomething good α", normaliser.normalise("ẞomething good α"));
        assertEquals("Garçon désolé", normaliser.normalise("Garçon désolé"));
   }

    @Test
    public void testNormaliseSymbols1() {
        BasicNormaliser normaliser = new BasicNormaliser(false, false, true, false, false, false);
        assertEquals("Something good", normaliser.normalise("Something good"));
        assertEquals("Something    good", normaliser.normalise("   Something    good    "));
        assertEquals("‘Something good’", normaliser.normalise("‘Something good’"));
        assertEquals("“Something good”", normaliser.normalise("“Something good”"));
        assertEquals("Something—good", normaliser.normalise("Something—good"));
        assertEquals("Something good  alpha", normaliser.normalise("Something good α"));
        assertEquals("SSomething good  alpha", normaliser.normalise("ẞomething good α"));
        assertEquals("Garçon désolé", normaliser.normalise("Garçon désolé"));
    }

    @Test
    public void testNormaliseAccents1() {
        BasicNormaliser normaliser = new BasicNormaliser(false, false, false, true, false, false);
        assertEquals("Something good", normaliser.normalise("Something good"));
        assertEquals("Something    good", normaliser.normalise("   Something    good    "));
        assertEquals("‘Something good’", normaliser.normalise("‘Something good’"));
        assertEquals("“Something good”", normaliser.normalise("“Something good”"));
        assertEquals("Something—good", normaliser.normalise("Something—good"));
        assertEquals("Something good α", normaliser.normalise("Something good α"));
        assertEquals("ẞomething good α", normaliser.normalise("ẞomething good α"));
        assertEquals("Garcon desole", normaliser.normalise("Garçon désolé"));
    }

    @Test
    public void testNormaliseCase1() {
        BasicNormaliser normaliser = new BasicNormaliser(false, false, false, false, true, false);
        assertEquals("something good", normaliser.normalise("Something good"));
        assertEquals("something    good", normaliser.normalise("   Something    good    "));
        assertEquals("‘something good’", normaliser.normalise("‘Something good’"));
        assertEquals("“something good”", normaliser.normalise("“Something good”"));
        assertEquals("something—good", normaliser.normalise("Something—good"));
        assertEquals("something good α", normaliser.normalise("Something good α"));
        assertEquals("ßomething good α", normaliser.normalise("ẞomething good α"));
        assertEquals("garçon désolé", normaliser.normalise("Garçon désolé"));
    }

    @Test
    public void testNormaliseQuotes1() {
        BasicNormaliser normaliser = new BasicNormaliser(true, false, false, false, false, true);
        assertEquals("Something good", normaliser.normalise("Something good"));
        assertEquals("Something good", normaliser.normalise("   Something    good    "));
        assertEquals("Something good", normaliser.normalise("‘Something good’"));
        assertEquals("Something good", normaliser.normalise("“Something good”"));
        assertEquals("Something—good", normaliser.normalise("Something—good"));
        assertEquals("Something good α", normaliser.normalise("Something good α"));
        assertEquals("ẞomething good α", normaliser.normalise("ẞomething good α"));
        assertEquals("Garçon désolé", normaliser.normalise("Garçon désolé"));
    }

    @Test
    public void testNormaliseAll1() {
        BasicNormaliser normaliser = new BasicNormaliser();
        assertEquals("something good", normaliser.normalise("Something good"));
        assertEquals("something good", normaliser.normalise("   Something    good    "));
        assertEquals("something good", normaliser.normalise("‘Something good’"));
        assertEquals("something good", normaliser.normalise("“Something good”"));
        assertEquals("something-good", normaliser.normalise("Something—good"));
        assertEquals("something good alpha", normaliser.normalise("Something good α"));
        assertEquals("ssomething good alpha", normaliser.normalise("ßomething good α"));
        assertEquals("garcon desole", normaliser.normalise("Garçon désolé"));
    }
}
