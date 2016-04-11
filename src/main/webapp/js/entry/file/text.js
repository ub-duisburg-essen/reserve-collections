
jQuery(document).ready(function () {
    jQuery('pre code').each(function (i, block) {
        hljs.highlightBlock(block);
    });
});
