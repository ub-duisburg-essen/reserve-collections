
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