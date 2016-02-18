/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


(function($){  
  $.fn.slideupmenu = function(options){

    var opts = jQuery.extend({ slideUpSpeed: 150, slideDownSpeed: 200, ease: "easeOutQuad", stopQueue: true }, options);
      
    $(this).find('.top-menu-main').hover(function(){
    $(this).addClass('hover');
      var $o = $(this).find('ul');
      if (opts.stopQueue) $o = $o.stop(true, true).slideDown(opts.slideUpSpeed, opts.ease);
    
    }, function() {
    $(this).removeClass('hover');
      var $o = $(this).find('ul');
      if (opts.stopQueue) $o = $o.stop(true, true).fadeOut(300)
    });
  }

})(jQuery);