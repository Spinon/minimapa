plugins {
  alias(libs.plugins.kotlin.jvm)
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(project(":core:domain"))
  implementation(project(":core:policy"))
  implementation(project(":core:quest-contract"))
  testImplementation(libs.junit)
}
