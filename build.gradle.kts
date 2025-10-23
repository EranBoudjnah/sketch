plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ktlint) apply false
}

private fun Project.installGitHookFromAutomation() {
    val projectRootDirectory = rootProject.rootDir
    val gitDirectory = File(projectRootDirectory, ".git")
    val gitHooksDirectory = File(gitDirectory, "hooks")
    val preCommitScriptFile = File(projectRootDirectory, "automation/pre-commit")

    if (!gitDirectory.exists()) {
        logger.lifecycle("⚠\uFE0F Skipping git hook installation: .git directory not found at ${gitDirectory.absolutePath}")
        return
    }
    if (!preCommitScriptFile.exists()) {
        logger.lifecycle("⚠\uFE0F Skipping git hook installation: script not found at ${preCommitScriptFile.absolutePath}")
        return
    }

    if (!gitHooksDirectory.exists()) gitHooksDirectory.mkdirs()
    val scriptDestinationFile = File(gitHooksDirectory, "pre-commit")
    preCommitScriptFile.copyTo(scriptDestinationFile, overwrite = true)
    logger.lifecycle("✅ Installed git pre-commit hook -> ${scriptDestinationFile.absolutePath}")
    if (!scriptDestinationFile.setExecutable(true)) {
        logger.lifecycle("⚠\uFE0F Failed to make pre-commit git hook script executable")
    }
}

gradle.projectsEvaluated {
    project.installGitHookFromAutomation()
}
