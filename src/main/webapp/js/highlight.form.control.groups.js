(function ($) {

    function highlightFocused(input) {
        var group = input.parents('.form-group')
        if (!group.hasClass('focused'))
            group.addClass('focused');
    }

    $(document).ready(function () {

        $('.paper input').focusout(function () {
            $(this).parents('.form-group').removeClass('focused');
        }).focusin(function () {
            highlightFocused($(this));
        });
        highlightFocused($(':focus'));
    });
})(jQuery);
