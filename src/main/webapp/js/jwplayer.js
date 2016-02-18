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