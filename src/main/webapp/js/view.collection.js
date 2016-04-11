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