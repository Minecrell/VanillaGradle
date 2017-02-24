/*
 * VanillaGradle - ForgeGradle extension for Vanilla libraries
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.minecrell.vanillagradle

import static net.minecraftforge.gradle.common.Constants.CONFIG_MC_DEPS
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_DC_RESOLVED
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_DP_RESOLVED
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_MC
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_PROVIDED
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_START
import static net.minecraftforge.gradle.user.UserConstants.TASK_DD_COMPILE
import static net.minecraftforge.gradle.user.UserConstants.TASK_DD_PROVIDED
import static net.minecraftforge.gradle.user.UserConstants.TASK_MAKE_START
import static net.minecraftforge.gradle.user.UserConstants.TASK_SETUP_DECOMP
import static net.minecraftforge.gradle.user.UserConstants.TASK_SETUP_DEV

import net.minecraftforge.gradle.user.ReobfMappingType
import net.minecraftforge.gradle.user.ReobfTaskFactory
import net.minecraftforge.gradle.user.UserBaseExtension
import net.minecraftforge.gradle.user.UserVanillaBasePlugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.JavaExec
import org.gradle.plugins.ide.idea.model.IdeaModel

abstract class VanillaPlugin extends UserVanillaBasePlugin<UserBaseExtension> {

    @Override
    protected void applyVanillaUserPlugin() {
        // Scan resource directory for access transformers
        extension.atSource(((JavaPluginConvention) project.convention.plugins.java).sourceSets.main)
    }

    @Override
    protected void setupReobf(ReobfTaskFactory.ReobfTaskWrapper reobf) {
        super.setupReobf(reobf)

        // Re-obfuscate to SEARGE by default
        reobf.mappingType = ReobfMappingType.SEARGE;
    }

    @Override
    protected void afterEvaluate() {
        super.afterEvaluate()

        // Remove start dependency
        project.configurations.getByName(CONFIG_START).dependencies.clear()
    }

    @Override
    protected void makeDecompTasks(String globalPattern, String localPattern,
            Object inputJar, String inputTask, Object mcpPatchSet, Object mcpInject) {
        super.makeDecompTasks(globalPattern, localPattern, inputJar, inputTask, mcpPatchSet, mcpInject)

        project.tasks.with {
            getByName(TASK_SETUP_DEV).dependsOn.remove 'makeStart'
            getByName(TASK_SETUP_DECOMP).dependsOn.remove 'makeStart'
            getByName(TASK_MAKE_START).enabled = false
            withType(JavaExec) { t ->
                if (t.dependsOn.contains('makeStart')) {
                    t.enabled = false
                }
            }
        }
    }

    @Override
    protected void configureCompilation() {
        project.with {
            Configuration[] mcConfigs = [
                    configurations.getByName(CONFIG_MC),
                    configurations.getByName(CONFIG_MC_DEPS),
                    configurations.getByName(CONFIG_DC_RESOLVED),
            ]
            Configuration[] provided = [
                    configurations.getByName(CONFIG_PROVIDED),
                    configurations.getByName(CONFIG_DP_RESOLVED),
            ]

            // For compile we map all dependencies to PROVIDED
            configurations.getByName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME).with {
                extendsFrom(mcConfigs)
                extendsFrom(provided)
            }

            // For the test, we map it to COMPILE (so it is available for tests)
            configurations.getByName(JavaPlugin.TEST_COMPILE_CONFIGURATION_NAME).extendsFrom(mcConfigs)
            configurations.getByName(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME).extendsFrom(provided)
        }
    }

    @Override
    protected void configureEclipse() {
        // Classpath should be handled by compileOnly configuration

        // Other dependencies
        project.tasks.eclipseClasspath.dependsOn TASK_DD_COMPILE, TASK_DD_PROVIDED
    }

    @Override
    protected void configureIntellij() {
        project.with {
            extensions.getByType(IdeaModel).module.with {
                excludeDirs.addAll files('.gradle', 'build', '.idea', 'out').files

                // Classpath should be handled by compileOnly configuration

                // Fix problems with resource files
                inheritOutputDirs = true
            }

            // Add deobf task dependencies
            tasks.ideaModule.dependsOn TASK_DD_COMPILE, TASK_DD_PROVIDED
        }
    }


    @Override
    protected final boolean hasServerRun() {
        false
    }

    @Override
    protected final boolean hasClientRun() {
        false
    }

    @Override
    protected final String getClientTweaker(UserBaseExtension ext) {
        ""
    }

    @Override
    protected final String getServerTweaker(UserBaseExtension ext) {
        ""
    }

    @Override
    protected final String getClientRunClass(UserBaseExtension ext) {
        ""
    }

    @Override
    protected final List<String> getClientJvmArgs(UserBaseExtension ext) {
        []
    }

    @Override
    protected final String getServerRunClass(UserBaseExtension ext) {
        ""
    }

    @Override
    protected final List<String> getServerJvmArgs(UserBaseExtension ext) {
        []
    }

}
