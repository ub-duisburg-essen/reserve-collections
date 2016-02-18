/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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
// A class that updates a zone on any client-side event.
// Based on http://tinybits.blogspot.com/2010/03/new-and-better-zoneupdater.html
// and some help from Inge Solvoll.

ZoneUpdater = Class.create( {

    initialize : function(spec) {
        this.element = $(spec.elementId);
        this.listenerURI = spec.listenerURI;
        $(this.element).getStorage().zoneId = spec.zoneId;

        if (spec.clientEvent) {
            this.clientEvent = spec.clientEvent;
            this.element.observe(this.clientEvent, this.updateZone.bindAsEventListener(this));
        }
    },

    updateZone : function() {
        var zoneManager = Tapestry.findZoneManager(this.element);

        if (!zoneManager) {
            return;
        }

        var listenerURIWithValue = this.listenerURI;

        if (this.element.value) {
            var param = this.element.value;
            if (param) {
                listenerURIWithValue = addQueryStringParameter(listenerURIWithValue, 'param', param);
            }
        }

        zoneManager.updateFromURL(listenerURIWithValue);
    }

} )

function addQueryStringParameter(url, name, value) {
    if (url.indexOf('?') < 0) {
        url += '?'
    } else {
        url += '&';
    }
    value = encodeURI(value);
    url += name + '=' + value;
    return url;
}