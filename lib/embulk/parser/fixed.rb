Embulk::JavaPlugin.register_parser(
  "fixed", "org.embulk.parser.fixed.FixedParserPlugin",
  File.expand_path('../../../../classpath', __FILE__))
