
(function ($) {

    $(document).ready(function () {

        $.datepicker.setDefaults(
            $.extend(
                {'dateFormat':'dd.mm.yy'},
                $.datepicker.regional['de']
            )
        );

        jQuery('.datepicker').datepicker({
            dateFormat: "dd.mm.yy",
            changeMonth: true,
            changeYear: true,
            showButtonPanel: true

        });
    });
})(jQuery);