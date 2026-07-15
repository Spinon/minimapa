plugins {
  alias(libs.plugins.kotlin.jvm)
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(project(":core:config"))
  implementation(project(":core:contracts"))
  testImplementation(libs.junit)
}
