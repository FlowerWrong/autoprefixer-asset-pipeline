var compile = function (fileText, paths) {
    var me = this;
    globalPaths = paths;

    var result;

    var output = autoprefixer.process(fileText);

    var compileResults = {success: true, css: output.css};

    Packages.asset.pipeline.autoprefixer.AutoprefixerProcessor.setResults(compileResults);

    return result;
};