def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed because NullAway severity is WARN"
assert buildLog.contains("warning: [NullAway]") : "Build log should contain a NullAway warning"
assert !buildLog.contains("error: [NullAway]") : "Build log should not contain a NullAway error when severity is WARN"
