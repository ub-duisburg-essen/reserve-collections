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
function showOverlay(spinner) {
    jQuery('.loader').fadeIn(200);

    var target = $('bookSearchForm');
    spinner.spin(target);
}

function hideOverlay(spinner) {
    jQuery('.loader').fadeOut(200);
    spinner.stop();
}

jQuery(document).ready(function() {

    var opts = {
        lines: 15, // The number of lines to draw
        length: 12, // The length of each line
        width: 2, // The line thickness
        radius: 12, // The radius of the inner circle
        corners: 0, // Corner roundness (0..1)
        rotate: 0, // The rotation offset
        direction: 1, // 1: clockwise, -1: counterclockwise
        color: '#F26D00', // #rgb or #rrggbb or array of colors
        speed: 1.2, // Rounds per second
        trail: 36, // Afterglow percentage
        shadow: true, // Whether to render a shadow
        hwaccel: false, // Whether to use hardware acceleration
        className: 'spinner', // The CSS class to assign to the spinner
        zIndex: 2e9, // The z-index (defaults to 2000000000)
        top: 'auto', // Top position relative to parent in px
        left: 'auto' // Left position relative to parent in px
    };

    jQuery('#create-book-help').tooltip({
        placement: 'left',
        container: 'body'
    });

    var spinner = new Spinner(opts);
    jQuery('#bookSearchForm').submit(function(){
        showOverlay(spinner);
    });

    $('bookSearchZone').observe(Tapestry.ZONE_UPDATED_EVENT, function() {
        hideOverlay(spinner);
    });
});
