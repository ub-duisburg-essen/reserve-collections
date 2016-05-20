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

function toastrDurable(msg) {

    var options = {
        "closeButton": true,
        "preventDuplicates": true,
        "timeOut": "-1",
        "extendedTimeOut": "-1",
        "positionClass": "toast-bottom-right"
    }
    toastr.info(msg, '', options);
}

function toastrDurableLink(msg, href) {
    var link = '<a href="' + href + '">' + msg + '</a>';
    toastrDurable(link);
}

function toastrSuccess(msg) {
    var options = {
        "positionClass": "toast-bottom-right"
    }
    toastr.success(msg);
}
