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

    var smallDeviceWidth = 779;
    var mediumDeviceWidth = 991;
    var toggleDuration = 200;

    function applyTouchNavigationToggle() {
        // bootstrap width for devices with low width
        if (window.innerWidth <= mediumDeviceWidth) {
            $('.navbar-mobile-menu-toggle button').text('');
        }
    }

    function toggle(selector, direction) {

        direction = direction == undefined ? 'left' : direction;

        $(selector).toggle('drop', {
            direction: direction,
            duration: toggleDuration
        });
    }

    $(document).ready(function () {

        applyTouchNavigationToggle();

        $(window).resize(function() {
            applyTouchNavigationToggle();

            if (window.innerWidth > smallDeviceWidth) {

                // remove all display styles that are directly that were previously set to actionbar or navigation
                $('.hide-on-small-devices').each(function() {

                    var visibility = $(this).css('display');
                    if (visibility == 'none') {
                        $(this).css('display', '');
                    }
                });
            }
        });

        $('.navbar-mobile-menu-toggle .basenav-toggle').click(function() {
            toggle('nav[role="application-nav"]');
        });

        $('.navbar-mobile-menu-toggle .actionbar-toggle').click(function() {
            toggle('#actionbar', 'right');
        });
    });

})(jQuery);