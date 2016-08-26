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
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_MC
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_PROVIDED
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_START
import static net.minecraftforge.gradle.user.UserConstants.TASK_DD_COMPILE
import static net.minecraftforge.gradle.user.UserConstants.TASK_DD_PROVIDED
import static net.minecraftforge.gradle.user.UserConstants.TASK_MAKE_START
import static net.minecraftforge.gradle.user.UserConstants.TASK_SETUP_DECOMP
import static net.minecraftforge.gradle.user.UserConstants.TASK_SETUP_DEV

import com.google.common.base.Strings
import net.minecraftforge.gradle.user.tweakers.ServerTweaker
import net.minecraftforge.gradle.user.tweakers.TweakerExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Jar

class VanillaServerPlugin extends ServerTweaker {

    @Override
    protected void afterEvaluate() {
        boolean hasTweakClass = Strings.isNullOrEmpty(extension.tweakClass)
        if (!hasTweakClass) {
            extension.tweakClass = 'null'
        }

        super.afterEvaluate()

        if (!hasTweakClass) {
            Jar jar = (Jar) project.tasks.jar
            jar.manifest.attributes.remove 'TweakClass'
        }

        project.configurations.getByName(CONFIG_START).dependencies.clear()
    }

    @Override
    protected void makeDecompTasks(String globalPattern, String localPattern, Object inputJar, String inputTask, Object mcpPatchSet, Object mcpInject) {
        super.makeDecompTasks(globalPattern, localPattern, inputJar, inputTask, mcpPatchSet, mcpInject)

        project.with {
            tasks.getByName(TASK_SETUP_DEV).dependsOn.remove 'makeStart'
            tasks.getByName(TASK_SETUP_DECOMP).dependsOn.remove 'makeStart'
            tasks.getByName(TASK_MAKE_START).enabled = false
            tasks.withType(JavaExec) {
                if (dependsOn.contains('makeStart')) {
                    enabled = false
                }
            }
        }
    }

    @Override
    protected void configureEclipse() {
        project.with {
            def mc = configurations.getByName(CONFIG_MC)
            def mcDeps = configurations.getByName(CONFIG_MC_DEPS)
            def provided = configurations.getByName(CONFIG_PROVIDED)

            eclipse.classpath.plusConfigurations.add(mcDeps)
            eclipse.classpath.plusConfigurations.add(mc)
            eclipse.classpath.plusConfigurations.add(provided)

            // other dependencies
            tasks.eclipseClasspath.dependsOn TASK_DD_COMPILE, TASK_DD_PROVIDED
        }
    }

    @Override
    protected void configureIntellij() {
        project.with {
            def mc = configurations.getByName(CONFIG_MC)
            def mcDeps = configurations.getByName(CONFIG_MC_DEPS)
            def provided = configurations.getByName(CONFIG_PROVIDED)

            idea {
                module {
                    excludeDirs += files('.idea', 'out').files

                    scopes.PROVIDED.plus.add(mcDeps)
                    scopes.PROVIDED.plus.add(mc)
                    scopes.PROVIDED.plus.add(provided)

                    // Fix the idea bug
                    inheritOutputDirs = true
                }
            }

            // Add deobf task dependencies
            tasks.ideaModule.dependsOn TASK_DD_COMPILE, TASK_DD_PROVIDED
        }
    }

    @Override
    protected String getClientRunClass(TweakerExtension ext) {
        return ""
    }

    @Override
    protected List<String> getClientRunArgs(TweakerExtension ext) {
        return []
    }

    @Override
    protected String getServerRunClass(TweakerExtension ext) {
        return getClientRunClass(ext)
    }

    @Override
    protected List<String> getServerRunArgs(TweakerExtension ext) {
        return getClientRunArgs(ext)
    }

    @Override
    protected boolean hasServerRun() {
        return false
    }

}
