function applyChosen(id) {

  var select = jQuery('#' + id);
  if (select) {
    
    select.chosen({
      no_results_text : Messages.chosenNoResults,
      search_contains : true
    }).change(function() {
      jQuery(this).trigger('liszt:updated');
    });
  }
}
