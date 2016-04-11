/*
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Created by nils on 09.06.15.
 */
(function($) {

    T5.extendInitializers(function() {

        function init(specs) {

            //JQuery dialog box configuration, if used.
            if (!specs.useDefaultConfirm) {
                var dialogBox = $('<div id=\'dialogConfirmationJQuery\' />').html(specs.message).dialog({
                    autoOpen : false,
                    resizable : specs.isResizable,
                    height : specs.height,
                    resize : 'auto',
                    title : specs.title,
                    modal : specs.isModal,
                    draggable : specs.isDraggable,
                    buttons : [ {
                        text : specs.validationMsg,
                        click : function() {
                            $(this).dialog("close");

                            trigger($("#" + specs.id));
                        }
                    }, {
                        text : specs.cancelMsg,
                        click : function() {
                            $(this).dialog("close");
                        }
                    } ]
                });
            }

            $("#" + specs.id).click(function(event) {
                if (specs.useDefaultConfir) {
                    //Default javascript confirmation box.
                    return confirm(specs.message);
                } else {
                    //JQuery confirmation box : we stop the event propagation before displaying the box.
                    //Otherwise the redirection will occur while confirmation box is displayed.

                    event.preventDefault();
                    event.stopImmediatePropagation();

                    dialogBox.dialog("open");
                }
            });
        }

        /**
         * Trigger url redirection or firm submission from element.
         *
         * @param HTML element
         */
        function trigger(element) {
            var tagName = element.prop("tagName");
            switch (tagName) {
                //Simple link (pagelink, actionlink, etc...)
                case "A":
                    var href = element.prop("href");
                    if (href != undefined) {
                        var urlSuffix = href.substring(href.lastIndexOf('.') + 1);
                        if (urlSuffix == element.prop("id")) {
                            //ActionLink
                            element.trigger(Tapestry.TRIGGER_ZONE_UPDATE_EVENT);
                        } else {
                            window.location.href = href;
                        }
                    }
                    break;
                //submit button.
                case "INPUT":
                    element.parents("form").submit();
                    break;
                default:
                    break;
            }
        }

        return {
            confirm : init
        }
    });
})(jQuery);