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
function print(url, id) {
    var components = {
        iframe: function (url) {
            return '<iframe id="' + id + '" name="printPage' + id + '" src=' + url + ' style="position: absolute; top: -1000px; @media print { display: block; }"></iframe>';

        }
    };
    jQuery("body").append(components.iframe(url));
    jQuery('#' + id).on("load", function () {
        var frm = window.frames[id].contentWindow;
        frm.focus();
        frm.print();
    });
};