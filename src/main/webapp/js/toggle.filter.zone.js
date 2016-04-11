function bindToggleFilter() {

    jQuery('.collapsible legend').on('click', function() {
        jQuery(this)
            .toggleClass('collapsed')
            .siblings('.collapsedContent')
                .toggle('blind', 200);
    });
}

jQuery(document).ready(function() {
    bindToggleFilter();

    $('filterZone').observe(Tapestry.ZONE_UPDATED_EVENT, function() {
        bindToggleFilter();
    });
});