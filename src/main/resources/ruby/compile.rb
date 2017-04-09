require "autoprefixer-rails"
require "json"

class Compile
  def initialize
  end

  def exec(css, *browsers)
    if browsers.empty? || browsers.nil? || browsers == [nil]
      return AutoprefixerRails.process(css).css
    else
      # ["[\"last 30 versions\",\"opera 12\"]"]
      browsers = JSON.parse(browsers[0])
      browsers.map! { |b| b.start_with?("\"") ? b[1..(b.size-2)] : b }
      return AutoprefixerRails.process(css, browsers: browsers).css
    end
  end
end

Compile.new

# css = "a {\n    tab-size: 2\n}"
# p Compile.new.exec(css, "last 2 versions", "opera 12")
