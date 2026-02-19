def buildLog = new File(basedir, "build.log").text
assert buildLog.contains("BUILD SUCCESS") : "Build should succeed"

def generatedDir = new File(basedir, "target/generated-sources/nullability")

// com.example already has package-info.java in source, should NOT be generated
def comExamplePackageInfo = new File(generatedDir, "com/example/package-info.java")
assert !comExamplePackageInfo.exists() : "package-info.java should NOT be generated for com.example (already exists in source)"

// com.example.sub does not have package-info.java, should be generated
def subPackageInfo = new File(generatedDir, "com/example/sub/package-info.java")
assert subPackageInfo.exists() : "package-info.java should be generated for com.example.sub"
assert subPackageInfo.text.contains("@NullMarked") : "Generated file should contain @NullMarked"
assert subPackageInfo.text.contains("package com.example.sub;") : "Generated file should contain correct package"
