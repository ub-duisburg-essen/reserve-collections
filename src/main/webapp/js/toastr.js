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
    initialize: function(target, elementId, type, title, event, options) {
        this.target = $(target);
        this.toastrElement = $(elementId);
        this.type = type;
        this.title = title;
        this.options = options;

        Event.observe(this.target, event, this.show.bind(this));
    },

    show: function () {
        var message = this.toastrElement.innerHTML;
        toastr[this.type](message, this.title, this.options);
    }
});

Tapestry.Initializer.toastr = function(spec) {
    new Toastr(spec.target, spec.elementId, spec.type, spec.title, spec.event, spec.options);
}