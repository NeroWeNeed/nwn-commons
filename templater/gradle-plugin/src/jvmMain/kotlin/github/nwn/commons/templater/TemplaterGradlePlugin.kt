package github.nwn.commons.templater

import github.nwn.BuildConfig
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.gradle.api.Project
import org.gradle.api.provider.Provider

class TemplaterGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        super.apply(target)
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider { emptyList() }



    override fun getCompilerPluginId(): String = "templater-gradle-plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        BuildConfig.KOTLIN_PLUGIN_GROUP,
        BuildConfig.KOTLIN_PLUGIN_NAME,
        BuildConfig.KOTLIN_PLUGIN_VERSION
    )

    override fun getPluginArtifactForNative(): SubpluginArtifact = SubpluginArtifact(
        BuildConfig.KOTLIN_PLUGIN_GROUP,
        BuildConfig.KOTLIN_PLUGIN_NAME + "-native",
        BuildConfig.KOTLIN_PLUGIN_VERSION
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}