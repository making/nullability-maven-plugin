def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("[nullability] Configuring ErrorProne") : "Plugin should log configuration message"
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed with existing ErrorProne config"
assert buildLog.contains("NullAway") : "Build log should mention NullAway configuration"
