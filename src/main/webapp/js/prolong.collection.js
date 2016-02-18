/**
 * Created by nils on 18.01.16.
 */

function calcDissolveDate() {

    var validTo = jQuery('input[name="validTo"]').val();
    var weeks = jQuery('input[name="dissolve_in_weeks"]').val();
    var dt = new Date(validTo).getTime();
    var dissolveDate = '';
    if (weeks > 0) {
        dt += weeks * 7 * 24 * 60 * 60 * 1000;
        dt = new Date(dt);
        dissolveDate = dt.toLocaleDateString();
    }
    jQuery('#dissolve_at_date').text(dissolveDate);
}

jQuery(document).ready(function() {
    jQuery('input[name="dissolve_in_weeks"]').change(calcDissolveDate);
    calcDissolveDate();
});