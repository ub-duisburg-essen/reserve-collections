
(function ($) {
    $.datepicker.setDefaults(
        $.extend(
            {'dateFormat':'dd.mm.yy'},
            $.datepicker.regional['de']
        )
    );

    $(document).ready(function () {
        jQuery('.datepicker').datepicker({
            dateFormat: "dd.mm.yy",
            changeMonth: true,
            changeYear: true,
            minDate: new Date(),
            showButtonPanel: true
        });
    });
})(jQuery);