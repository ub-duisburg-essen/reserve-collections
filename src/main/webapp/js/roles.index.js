/**
 * Created by nils on 21.01.14.
 */
function setSelectFormControl() {

    jQuery('.t-palette select').addClass('form-control');
}

jQuery(document).ready(function() {
    setSelectFormControl();

    $('availableActionsZone').observe(Tapestry.ZONE_UPDATED_EVENT, function() {
        setSelectFormControl();
    });
});
