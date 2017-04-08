var compile = function (fileText, paths) {
    var me = this;
    globalPaths = paths;

    var outputCss = autoprefixer.process(fileText).css;

    return outputCss;
};

// var processor;
// var process = function(fileText, paths) {
//     var result = autoprefixer.process.apply(autoprefixer, arguments);
//     var warns  = result.warnings().map(function (i) {
//         delete i.plugin;
//         return i.toString();
//     });
//     var map = result.map ? result.map.toString() : null;
//     return { css: result.css, map: map, warnings: warns };
// };