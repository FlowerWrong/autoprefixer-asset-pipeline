package asset.pipeline.autoprefixer

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import com.google.gson.Gson
import groovy.util.logging.Commons
import org.mozilla.javascript.*

@Commons
class AutoprefixerProcessor extends AbstractProcessor {

    public static final ThreadLocal threadLocal = new ThreadLocal()
    public static final ThreadLocal localCompiler = new ThreadLocal()

    Scriptable globalScope
    ClassLoader classLoader

    boolean enabled = true
    boolean map = true
    def browsers

    AutoprefixerProcessor(AssetCompiler precompiler) {
        super(precompiler)

        if (config?.enabled == false) {
            log.info 'Disabling Autoprefixer'
            enabled = false
            return
        }

        if (config?.map == false) {
            map = false
        }

        if (config?.browsers) {
            browsers = new Gson().toJson(config.browsers)
        }

        try {
            classLoader = getClass().getClassLoader()

            def shellJsResource = classLoader.getResource('asset/pipeline/autoprefixer/shell.js')
            def envRhinoJsResource = classLoader.getResource('asset/pipeline/autoprefixer/env.rhino.js')
            def autoprefixerJsResource = classLoader.getResource('asset/pipeline/autoprefixer/autoprefixer.js')
            def compileJsResource = classLoader.getResource('asset/pipeline/autoprefixer/compile.js')
            Context cx = Context.enter()

            cx.setOptimizationLevel(-1)
            globalScope = cx.initStandardObjects()
            this.evaluateJavascript(cx, shellJsResource)
            this.evaluateJavascript(cx, envRhinoJsResource)
            this.evaluateJavascript(cx, autoprefixerJsResource)
            this.evaluateJavascript(cx, compileJsResource)
        } catch (Exception e) {
            throw new Exception("Autoprefixer initialization failed.", e)
        } finally {
            try {
                Context.exit()
            } catch (IllegalStateException e) {
            }
        }
    }

    def evaluateJavascript(context, resource) {
        context.evaluateString globalScope, resource.getText('UTF-8'), resource.file, 1, null
    }

    String process(String input, AssetFile assetFile) {
        try {
            threadLocal.set(assetFile);
            localCompiler.set(precompiler)

            def cx = Context.enter()
            def compileScope = cx.newObject(globalScope)
            compileScope.setParentScope(globalScope)
            compileScope.put('cssSourceContent', compileScope, input)
            compileScope.put('browsers', compileScope, browsers)
            compileScope.put('map', compileScope, map)

            def res = cx.evaluateString(compileScope, "compile(cssSourceContent, browsers, map)", 'Autoprefix command', 0, null)
            return cx.toString(res)
        } catch (JavaScriptException e) {
            NativeObject errorMeta = (NativeObject) e.value

            def errorDetails = "Autoprefixer Compiler Failed - ${assetFile.path}.\n"
            if (precompiler) {
                errorDetails += "**Did you mean to compile this file individually (check docs on exclusion)?**\n"
            }
            if (errorMeta && errorMeta.get('message')) {

                errorDetails += " -- ${errorMeta.get('message')} Near Line: ${errorMeta.line}, Column: ${errorMeta.column}\n"
            }
            if (errorMeta != null && errorMeta.get('extract') != null) {
                List extractArray = (NativeArray) errorMeta.get('extract')
                errorDetails += "    --------------------------------------------\n"
                extractArray.each { error ->
                    errorDetails += "    ${error}\n"
                }
                errorDetails += "    --------------------------------------------\n\n"
            }

            if (config?.failOnError == false) {
                println errorDetails
            } else {
                throw new Exception(errorDetails, e)
            }
        } catch (Exception e) {
            println(e.getMessage())
        } finally {
            Context.exit()
        }
        return input
    }

    static def getConfig() {
        AssetPipelineConfigHolder.config?.autoprefixer
    }

    static void print(text) {
        println text
    }

    static void error(text) {
        log.error('Autoprefixer Compile Error: ' + text)
    }

}
