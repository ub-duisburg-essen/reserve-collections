/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
(function ($) {

    $(document).ready(
        function () {

            /* after a click inside a linklist the list item should be marked. */
            $('.linklist a').click(
                function (event) {

                    // remove active class if the list item is already active
                    if ($(this).parent('li').hasClass('active')) {

                        $(this).parent('li').removeClass('active');
                    } else {
                        /*
                         * otherwise the list item is marked as active element and all other active items are marked as not
                         * active.
                         */

                        $(this).parent('li').addClass('active').siblings().removeClass(
                            'active');
                    }
                }
            );
        }
    );
})(jQuery);