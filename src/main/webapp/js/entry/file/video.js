
function setupJWPlayer(sources) {
    jwplayer('video-container').setup({
        width: '100%',
        height: 480,
        playlist: [{
            sources: eval(sources)
        }],
        primary: "flash"
    });
}