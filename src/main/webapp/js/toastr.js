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
Toastr = Class.create({
    initialize: function(observe, elementId, type, title, options) {
        this.elementId = elementId;
        this.type = type;
        this.title = title;
        this.options = options;
        Event.observe($(observe), 'click', this.show.bindAsEventListener())
    },

    show: function () {
        var message = $(this.elementId).innerHTML;
        toastr[this.type](message, this.title, this.options);
    }
});

Tapestry.Initializer.toastr = function(spec) {
    new Toastr(spec.observe, spec.elementId, spec.type, spec.title, spec.options);
}