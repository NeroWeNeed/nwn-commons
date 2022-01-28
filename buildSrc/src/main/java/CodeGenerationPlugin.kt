import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject
import kotlin.reflect.full.createInstance


open class CodeGenerationDefinitionExtension {

    var classPackage: String = ""
    var className: String = ""
    var sourceSet: String = "main"
    var generator: String = ""
}


interface CodeGenerator {
    operator fun invoke(
        packageName: String,
        className: String,
        output: File
    )
}

open class CodeGenerationTask @Inject constructor() : DefaultTask() {
    companion object {
        const val CODEGEN_GROUP = "codegen"
        const val DEFAULT_SOURCESET = "main"
    }
    @Input var generator: String = ""
    @Input var classPackage: String = ""
    @Input var className: String = ""
    @Input var sourceSet: String = ""


    @TaskAction
    fun execute() {

        val output = File(project.projectDir,
            "src/${sourceSet.ifBlank { DEFAULT_SOURCESET }}/kotlin/${
                classPackage.replace('.', '/')
            }/${className}.kt"
        )
        output.parentFile?.mkdirs()
        output.createNewFile()

        val generator = Class.forName(generator).kotlin.createInstance() as CodeGenerator
        generator.invoke(classPackage, className, output)
    }
}

/*
class CodeGenerationPlugin : Plugin<Project> {
    companion object {
        const val CODEGEN_GROUP = "codegen"
        const val DEFAULT_SOURCESET = "main"
    }

    override fun apply(target: Project) {
        val extension = target.extensions.create("generator", CodeGenerationDefinitionExtension::class.java)

        */
/*     target.tasks.withType(CodeGenerationTask::class.java) { task ->
                 task.group = CODEGEN_GROUP

                 task.doFirst {
                     with(it as CodeGenerationTask) {
                         packageProperty.set(extension.classPackage)
                         classNameProperty.set(extension.className)
                         sourceSetProperty.set(extension.sourceSet)
                         generatorProperty.set(extension.generator)
                         outputProperty.set(
                             File(
                                 "src/${sourceSetProperty.getOrElse(DEFAULT_SOURCESET)}/kotlin/${
                                     packageProperty.get().replace('.', '/')
                                 }/${classNameProperty.get()}.kt"
                             )
                         )
                     }


                 }
             }*//*

    }
}*/
