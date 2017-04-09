package asset.pipeline.autoprefixer

import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

class RubyWrapper {

    def filename
    def jruby
    def rubyObject

    RubyWrapper(filename){
        this.filename = filename
        loadRubyFile()
    }

    def propertyMissing(String name) {
        rubyObject.callMethod(name)
    }

    def propertyMissing(String name, value) {
        def args = new IRubyObject[1]
        args[0] = JavaEmbedUtils.javaToRuby(rubyObject.getRuntime(), value)
        rubyObject.callMethod("${name}=", args)
    }

    def methodMissing(String name, args) {
        def rubyArgs = new IRubyObject[args.size()]
        args.eachWithIndex{ arg, index ->
            rubyArgs[index] = JavaEmbedUtils.javaToRuby(rubyObject.getRuntime(), arg)
        }
        rubyObject.callMethod(name,rubyArgs)
    }

    def private loadRubyFile(){
        jruby = new ScriptingContainer()
        def scriptStream = this.class.classLoader.getResourceAsStream("ruby/${filename}.rb")
        rubyObject = jruby.runScriptlet(new InputStreamReader(scriptStream, "UTF-8"), new File("resources/ruby/${filename}.rb").getAbsolutePath())
    }

}
