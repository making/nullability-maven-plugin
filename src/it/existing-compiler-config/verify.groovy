def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("[nullability] Configuring ErrorProne") : "Plugin should log configuration message"
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed with existing compiler config preserved"

// Verify MapStruct annotation processor worked: generated mapper implementation must exist
def mapperImpl = new File(basedir, "target/generated-sources/annotations/com/example/UserMapperImpl.java")
assert mapperImpl.exists() : "MapStruct should generate UserMapperImpl.java"
def mapperImplContent = mapperImpl.text
assert mapperImplContent.contains("class UserMapperImpl") : "Generated file should contain UserMapperImpl class"
assert mapperImplContent.contains("setName") : "Generated mapper should map the name property"

// Verify the compiled mapper implementation class exists
def mapperClass = new File(basedir, "target/classes/com/example/UserMapperImpl.class")
assert mapperClass.exists() : "UserMapperImpl.class should be compiled"

// Verify NullAway is active: the @NullMarked code compiled successfully with ErrorProne + NullAway,
// which means NullAway checked the nullability contracts and found no violations.
// The NullAway-specific compiler arguments are present in the configuration message.
assert buildLog.contains("NullAway") : "Build log should mention NullAway configuration"
