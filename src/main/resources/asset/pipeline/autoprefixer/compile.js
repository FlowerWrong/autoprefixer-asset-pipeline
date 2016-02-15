var compile = function (fileText, paths) {
    var me = this;
    globalPaths = paths;

    var outputCss = autoprefixer.process(fileText);

    return outputCss;
};