var compile = function (fileText, browsers, paths) {
    var me = this;
    globalPaths = paths;
    return autoprefixer.process(fileText, {browsers: JSON.parse(browsers)}).css;
};
