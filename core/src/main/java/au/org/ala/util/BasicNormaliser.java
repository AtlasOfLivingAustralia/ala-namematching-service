package au.org.ala.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic normaliser that can be used to clean a string to varying degrees.
 * <p>
 * Strings may contain non-breaking spaces.
 * They may also contain punctuation that is a bit weird and
 * they may also contain accented characters and ligatures.
 * This class provides various levels of cleanliness for input strings.
 * </p>
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class BasicNormaliser implements Normaliser {
    /** The multiple space pattern */
    private static final Pattern SPACES = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);
    /** The diacritic pattern */
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+", Pattern.UNICODE_CHARACTER_CLASS);
    /** The punctuation translation table */
    private static final Substitute[] PUNCT_TRANSLATE = {
            new Substitute('\u00a0', ' '), // Non breaking space
            new Substitute('\u00ad', '-'), // Soft hyphen
            new Substitute('\u2010', '-'), // Hyphen
            new Substitute('\u2011', '-'), // Non-breaking hyphen
            new Substitute('\u2012', '-'), // Figure dash
            new Substitute('\u2013', '-'), // En-dash
            new Substitute('\u2014', '-'), // Em-dash
            new Substitute('\u2015', '-'), // Horizontal bar
            new Substitute('\u2018', '\''), // Single left quotation
            new Substitute('\u2019', '\''), // Single right quotation
            new Substitute('\u201a', '\''), // Single low quotation
            new Substitute('\u201b', '\''), // Single high reversed quotation
            new Substitute('\u201c', '"'), // Left quote
            new Substitute('\u201d', '"'), // Right quote
            new Substitute('\u201e', '"'), // Low quote
            new Substitute('\u201f', '"'), // Reversed high quote
            new Substitute('\u2027', ""), // Hyphenation point
            new Substitute('\u2028', ' '), // Line seperator
            new Substitute('\u2029', ' '), // Paragraph seperator
            new Substitute('\u202a', ""), // Left to right embedding
            new Substitute('\u202b', ""), // Right to left embeddiong
            new Substitute('\u202c', ""), // Pop directional formatting
            new Substitute('\u202d', ""), // Left to right override
            new Substitute('\u202e', ""), // Right to left override
            new Substitute('\u202f', ' '), // Narrow no break space
    };

    /** The special character translation table for spelling some things out and replacing interesting punctuation with basic latin versions */
    private static final Substitute[] SYMBOL_TRANSLATE = {
            new Substitute('\u00a1', '!'), // Inverted exclamation
            new Substitute('\u00a2', 'c'), // Cent sign
            new Substitute('\u00a3', '#'), // Pound sign
            new Substitute('\u00a4', '#'), // Currency sign
            new Substitute('\u00a5', 'Y'), // Yen
            new Substitute('\u00a6', '|'), // Borken bar
            new Substitute('\u00a7', '$'), // Section sign
            new Substitute('\u00a8', ""), // Diaresis
            new Substitute('\u00a9', 'c'), // Copyright
            new Substitute('\u00aa', ""), // Feminine ordinal
            new Substitute('\u00ab', "<<"), // Left angle quotation
            new Substitute('\u00ac', '~'), // Not sign
            new Substitute('\u00d7', " x "), // Multiplication sign
            new Substitute('\u00ae', 'r'), // Registerd
            new Substitute('\u00af', ' '), // Macron
            new Substitute('\u00b0', 'o'), // Degree
            new Substitute('\u00b1', "+-"), // Plus-minus
            new Substitute('\u00b2', '2'), // Superscipt 2
            new Substitute('\u00b3', '3'), // Superscript 3
            new Substitute('\u00b4', ""), // Acute accent
            new Substitute('\u00b5', 'u'), // Micro
            new Substitute('\u00b6', '@'), // Pilcrow
            new Substitute('\u00b7', '.'), // Middle dot
            new Substitute('\u00b8', ""), // Cedilla
            new Substitute('\u00b9', '1'), // Superscript 1
            new Substitute('\u00bb', ">>"), // Right angle quotation
            new Substitute('\u00bf', '?'), // Inverted question mark
            new Substitute('\u00df', "ss"), // Small sharp s
            new Substitute('\u03b1', " alpha "),
            new Substitute('\u03b2', " beta "),
            new Substitute('\u03b3', " gamma "),
            new Substitute('\u03b4', " delta "),
            new Substitute('\u03b5', " epsilon "),
            new Substitute('\u03b6', " zeta "),
            new Substitute('\u03b7', " eta"),
            new Substitute('\u03b8', " theta "),
            new Substitute('\u03ba', " kappa "),
            new Substitute('\u03bb', " lambda "),
            new Substitute('\u03bc', " mu "),
            new Substitute('\u03bd', " nu "),
            new Substitute('\u03be', " xi "),
            new Substitute('\u03bf', " omicron "),
            new Substitute('\u03c0', " pi "),
            new Substitute('\u03c1', " rho "),
            new Substitute('\u03c2', " sigma "),
            new Substitute('\u03c3', " sigma"),
            new Substitute('\u03c4', " tau "),
            new Substitute('\u03c5', " upsilon "),
            new Substitute('\u03c6', " phi "),
            new Substitute('\u03c7', " chi "),
            new Substitute('\u03c8', " psi "),
            new Substitute('\u03c9', " omega "),
            new Substitute('\u1e9e', "SS"), // Capital sharp s
            new Substitute('\u2016', '|'), // Double vertical line
            new Substitute('\u2017', '-'), // Double low line
            new Substitute('\u2020', '*'), // Dagger
            new Substitute('\u2021', '*'), // Double dagger
            new Substitute('\u2022', '*'), // Bullet
            new Substitute('\u2023', '*'), // Triangular bullet
            new Substitute('\u2024', '.'), // One dot leader
            new Substitute('\u2025', '.'), // Two dot leader
            new Substitute('\u2026', '.'), // Three dot leader
            new Substitute('\u2030', '%'), // Per mille
            new Substitute('\u2031', '%'), // Per ten thousand
            new Substitute('\u2032', '\''), // Prime
            new Substitute('\u2033', '"'), // Double prime
            new Substitute('\u2034', '"'), // Triple prime
            new Substitute('\u2035', '\''), // Reversed prime
            new Substitute('\u2036', '"'), // Reversed double prime
            new Substitute('\u2037', '"'), // Reversed triple prime
            new Substitute('\u2038', '^'), // Caret
            new Substitute('\u2039', '<'), // Left angle quote
            new Substitute('\u203a', '>'), // Right angle quote
            new Substitute('\u203b', '*'), // Reference mark
            new Substitute('\u203c', "!!"), // Double exclamation
            new Substitute('\u203d', "?!"), // Interrobang
            new Substitute('\u203e', '-'), // Overline
            new Substitute('\u203f', '_'), // Undertie
            new Substitute('\u2040', '-'), // Character tie
            new Substitute('\u2041', '^'), // Caret insertion point
            new Substitute('\u2042', '*'), // Asterism
            new Substitute('\u2043', '*'), // Hyphen bullet
            new Substitute('\u2044', '/'), // Fraction slash
            new Substitute('\u2045', '['), // Left bracket with quill
            new Substitute('\u2046', ']'), // Right bracket with quill
            new Substitute('\u2047', "??"), // Double question mark
            new Substitute('\u2715', " x "), // Multiplication x
            new Substitute('\u2a09', " x "), // n-ary cross
            new Substitute('\u2a7f', " x ") // Cross product
    };

    /** Reduce runs of spaces to a single space */
    @JsonProperty
    private final boolean normaliseSpaces;
    /** Turn things like open/close quotes into single direction quotes */
    @JsonProperty
    private final boolean normalisePunctuation;
    /** Spell out unusual symbols */
    @JsonProperty
    private final boolean normaliseSymbols;
    /** Turn accented characters into non-accented characters */
    @JsonProperty
    private final boolean normaliseAccents;
    /** Convert case into upper/lower case */
    @JsonProperty
    private final boolean normaliseCase;
    /** Strip surrounding quotes */
    @JsonProperty
    private final boolean stripSurroundingQuotes;

    private static Map<Character, String> PUNCT_MAP = null;
    private static Map<Character, String> SYMBOL_MAP = null;

    /**
     * Get the map for translating unicode punctuation to ASCII punctuation
     *
     * @return The punctuation map
     */
    protected static synchronized Map<Character, String> getPunctuationMap() {
        if (PUNCT_MAP == null) {
            PUNCT_MAP = new HashMap<Character, String>(100);
            for (Substitute sub: PUNCT_TRANSLATE)
                PUNCT_MAP.put(sub.ch, sub.sub);
        }
        return PUNCT_MAP;
    }

    /**
     * Get the map for translating unicode characters to ASCII spellings
     *
     * @return The punctuation map
     */
    protected static synchronized Map<Character, String> getSymbolMap() {
        if (SYMBOL_MAP == null) {
            SYMBOL_MAP = new HashMap<Character, String>(100);
            for (Substitute sub: SYMBOL_TRANSLATE)
                SYMBOL_MAP.put(sub.ch, sub.sub);
        }
        return SYMBOL_MAP;
    }

    /**
     * Construct a normaliser
     *
     * @param normaliseSpaces Normalise spaces into a single space
     * @param normalisePunctuation Normalise open/close punctuation characters
     * @param normaliseSymbols Normalise symbols such as \beta
     * @param normaliseAccents Convert accented characters into unaccented characters
     * @param normaliseCase Ensure all lower-case
     * @param stripSurroundingQuotes Remove matching open/close quotes around a string
     */
    public BasicNormaliser(boolean normaliseSpaces, boolean normalisePunctuation, boolean normaliseSymbols, boolean normaliseAccents, boolean normaliseCase, boolean stripSurroundingQuotes) {
        this.normaliseSpaces = normaliseSpaces;
        this.normalisePunctuation = normalisePunctuation;
        this.normaliseSymbols = normaliseSymbols;
        this.normaliseAccents = normaliseAccents;
        this.normaliseCase = normaliseCase;
        this.stripSurroundingQuotes = stripSurroundingQuotes;
    }

    /**
     * Construct a default normaliser that performs all normalisations
     */
    public BasicNormaliser() {
        this.normaliseSpaces = true;
        this.normalisePunctuation = true;
        this.normaliseSymbols = true;
        this.normaliseAccents = true;
        this.normaliseCase = true;
        this.stripSurroundingQuotes = true;
    }

    /**
     * Normalise spaces.
     * <p>
     * Replace all sequences of whitespace with a single space.
     * Remove fancy whitespace.
     * </p>
     *
     * @param s The string to translate
     *
     * @return The normalised string
     */
    protected String normaliseSpaces(String s) {
        Matcher matcher = SPACES.matcher(s);
        return matcher.replaceAll(" ").trim();
    }


    /**
     * Strip accents.
     * <p>
     * Decompose anhy accented characters into diacritics and base characters and then remove the diacritics.
     * </p>
     *
     * @param s The string to translate
     *
     * @return The de-accented string
     */
    protected String removeAccents(String s) {
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        Matcher matcher = DIACRITICS.matcher(s);
        return matcher.replaceAll("").trim();
    }

    /**
     * Translate a string according to a translation map.
     *
     * @param name The string
     * @param map The translation map
     *
     * @return The translated string
     */
    protected String translate(String name, Map<Character, String> map) {
        StringBuilder builder = new StringBuilder(name.length());
        int i, len = name.length();

        for (i = 0; i < len; i++) {
            char ch = name.charAt(i);
            String r = map.get(ch);

            if (r == null)
                builder.append(ch);
            else
                builder.append(r);
        }
        return builder.toString();
    }

    protected boolean isSurroundedByQuotes(String s) {
        if (s == null || s.length() < 2)
            return false;
        char start = s.charAt(0);
        char end = s.charAt(s.length() - 1);
        return
                (start == '\'' && end == '\'') ||
                (start == '"' && end == '"') ||
                (start == '\u2018' && end == '\u2019') ||
                (start == '\u201a' && end == '\u2019') ||
                (start == '\u201c' && end == '\u201d') ||
                (start == '\u201e' && end == '\u2019');
    }

    @Override
    public String normalise(String s) {
        if (s == null)
            return null;
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC); // Get into canonical form
        if (this.normalisePunctuation)
            s = this.translate(s, getPunctuationMap());
        if (this.normaliseSymbols)
            s = this.translate(s, getSymbolMap());
        if (this.normaliseAccents)
            s = removeAccents(s);
        if (this.normaliseSpaces)
            s = this.normaliseSpaces(s);
        if (this.normaliseCase)
            s = s.toLowerCase();
        if (this.stripSurroundingQuotes && isSurroundedByQuotes(s))
            s = s.substring(0, s.length() - 1).substring(1);
        return s.trim();
    }

    protected static class Substitute {
        public Character ch;
        public String sub;

        public Substitute(Character ch, String sub) {
            this.ch = ch;
            this.sub = sub;
        }

        public Substitute(char ch, char sub) {
            this.ch = ch;
            this.sub = new String(new char[] { sub });
        }
    }

}

