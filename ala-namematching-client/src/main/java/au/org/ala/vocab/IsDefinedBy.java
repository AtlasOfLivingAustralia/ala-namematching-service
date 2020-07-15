package au.org.ala.vocab;

import java.lang.annotation.*;

/**
 * A reference to the defining term for a parameter, field, methods etc
 * allowing references to the intention of the element to be documented
 * and propagated.
 * <p>
 * Generally, the value is the URI for the equivalent term or the fully qualified name
 * of a class that provides a vocabulary.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@IsDefinedBy("http://www.w3.org/2000/01/rdf-schema#isDefinedBy")
public @interface IsDefinedBy {
    String value();
}
