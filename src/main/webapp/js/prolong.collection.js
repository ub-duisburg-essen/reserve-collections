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