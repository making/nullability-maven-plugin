def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("[nullability] Plugin is skipped") : "Plugin should log skip message"
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed"
