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
    $(document).ready(function () {
        $('#actionbar').stickyOnScroll();

        // fix action bar scroll
        var hash = jQuery(location).attr('hash');
        if (hash) {
            var offset = jQuery(hash).offset();
            var actionbar = jQuery('#actionbar:visible');
            var headerScroll = 0;
            if (actionbar) {
                headerScroll = actionbar.outerHeight(true);
            }
            var scrollto = offset.top - headerScroll; // minus fixed header height
            jQuery('html, body').animate({
                scrollTop: scrollto
            }, 1000);
        }
    });
})(jQuery);