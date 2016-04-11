/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
