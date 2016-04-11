
function setupJWPlayer(sources) {
    jwplayer('audio-container').setup({
        width: '100%',
        height: 150,
        playlist: [{
            sources: eval(sources)
        }],
        primary: "flash"
    });
}
