package unidue.rc.ui;


import java.lang.annotation.*;

/**
 * Created by nils on 22.05.15.
 */

/**
 * Specifies that the class is a "protected page", one that must not be accessible by users that are not logged in.
 * This annotation is applied to a Tapestry page class. The protection is provided by {@link unidue.rc.ui.services.CollectionRequestFilter}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtectedPage {

    boolean isAuthenticationNeeded() default true;
}
