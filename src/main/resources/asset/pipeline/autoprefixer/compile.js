var compile = function(fileText, browsers, map, paths) {
    var me = this;
    globalPaths = paths;
    return autoprefixer.process(fileText, {browsers: JSON.parse(browsers), map: map}).css;
};
