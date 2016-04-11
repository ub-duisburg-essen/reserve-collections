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
 * Created by nils on 11.08.15.
 */


(function ($) {

    $.fn.startjw = function(options) {

        // This is the easiest way to have default options.
        var settings = $.extend({
            // These are the defaults.
            width: '720',
            height: '480',
            id: 'jwcontainer'
        }, options );

        var modes = new Array()
        if (settings.html5Source != undefined) {
            modes.push({
                type: 'html5',
                config: {
                    file: settings.html5Source,
                    provider: 'video'
                }
            });
        }
        if (settings.rtmpSource != undefined) {
            modes.push({
                type: 'flash',
                src: settings.player,
                config: {
                    file: encodeURI(settings.rtmpSource),
                    streamer: 'rtmp://streaming.uni-due.de:1935/semapp/',
                    provider: 'rtmp',
                    bufferlength: 3
                }
            });
        }

        jwplayer(settings.id).setup({
            id: 'jwplayer',
            width: settings.width,
            height: settings.height,
            autostart: 'true',
            skin: settings.skin,
            modes: modes
        });
    };
})(jQuery);