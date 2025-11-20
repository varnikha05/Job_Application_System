/*
* --------------------------------------------------------------------
* jQuery plugin to make radio buttons deselectable.
* by Reed Hewitt, reed@reedhewitt.com
* https://reedhewitt.com
* https://github.com/reedhewitt/deselectable-radio-buttons
* --------------------------------------------------------------------
*/

(function($){
  $(document).ready(function(){
    $(document.body).on('click mousedown keydown keyup', 'input[type="radio"]', function(e){
      let radioButton = $(e.target);
      if(('keydown' === e.type  && 32 === e.keyCode) || 'mousedown' === e.type){
        radioButton.data('originally_checked', radioButton.prop('checked'));
      }
      if(('keyup' === e.type && 32 === e.keyCode) || 'click' === e.type){
        if(radioButton.data('originally_checked')){
          radioButton.prop('checked', false).removeAttr('checked');
        } else {
          radioButton.prop('checked', true).attr('checked', 'checked');
        }
        radioButton.removeData('originally_checked').trigger('done_changing');
      }
    });
  });
}(jQuery));
