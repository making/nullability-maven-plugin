def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("NullAway") : "Build should fail due to NullAway error"
