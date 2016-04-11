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