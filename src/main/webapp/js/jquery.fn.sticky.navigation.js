/**
 * Created with IntelliJ IDEA.
 * User: nverheyen
 * Date: 05.09.13
 * Time: 15:45
 * To change this template use File | Settings | File Templates.
 */
/**
 * User: Nils Verheyen
 * Date: 22.08.13
 * Time: 12:29
 */
(function ($) {

    $.fn.stickyOnScroll = function (options) {

        var settings = $.extend({
            fixed: function() {},
            relative: function() {}
        }, options);

        return this.each(function () {

            var element = $(this);

            // grab the initial top offset of the navigation
            var sticky_navigation_offset_top = element.offset().top;

            var element_width = element[0].getBoundingClientRect().width;

            // our function that decides weather the navigation bar should have "fixed" css position or not.
            var sticky_navigation = function () {
                var scroll_top = jQuery(window).scrollTop(); // our current vertical position from the top

                // if we've scrolled more than the navigation, change its position to fixed to stick to top,
                // otherwise change it back to relative
                if (scroll_top > sticky_navigation_offset_top) {
                    if (element.css('position') != 'fixed') {
                        element.css({ 'position': 'fixed', 'top': 0, 'width': element_width });
                        settings.fixed();
                    }

                } else {
                    element.css({ 'position': 'relative' });
                    settings.relative();
                }
            };

            // initialize sticky navigation
            sticky_navigation();

            // and run it again every time you scroll
            jQuery(window).scroll(function () {
                sticky_navigation();
            });
        });
    }
}(jQuery));