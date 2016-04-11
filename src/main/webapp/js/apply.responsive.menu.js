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