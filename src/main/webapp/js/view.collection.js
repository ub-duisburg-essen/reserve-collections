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
function bindSortable() {
    jQuery('#entries').sortable({

        handle: '.draggable',
        placeholder: 'ui-state-highlight',
        forcePlaceholderSize: true,
        update: function (event, ui) {
            jQuery('#entries > li').each(function (index) {
                var indexInput = jQuery(this).find("input[id^='index_']");
                index++;
                indexInput.val(index).attr('id', 'index_' + index);
            });

            /*
             workaround! use prototype here to click the link to submit all entries
             tapestry needs an element which is selected to submit the form.
             */
            jQuery('a[id^="linksubmit"]')[0].click();
        }
    });
}

jQuery(document).ready(function () {
    var toggleBtn = jQuery('#toggle-details');

    toggleBtn.click(function () {

        jQuery(this).attr('disabled', 'disabled');

        jQuery('#reserve-collection-meta dl').toggle({

            effect: 'slideDown',
            complete: function () {

                var dl = jQuery(this);
                var text = dl.is(':visible')
                    ? toggleBtn.attr('data-hide-msg')
                    : toggleBtn.attr('data-show-msg');
                toggleBtn.removeAttr('disabled');
                toggleBtn.text(text);
            }
        });
    });

    bindSortable();
});