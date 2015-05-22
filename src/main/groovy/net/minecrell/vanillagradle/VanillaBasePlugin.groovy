/*
 * VanillaGradle - Temporary ForgeGradle extension for Vanilla mods
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

import static net.minecraftforge.gradle.common.Constants.NATIVES_DIR
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_DEPS
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_MC
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_NATIVES
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_USERDEV

import net.minecraftforge.gradle.tasks.ExtractConfigTask
import net.minecraftforge.gradle.user.UserExtension
import org.gradle.api.tasks.JavaExec

class VanillaBasePlugin extends BaseVanillaPlugin<UserExtension> {

    @Override
    protected void configureDeps() {
        project.with {
            configurations.create(CONFIG_USERDEV)
            configurations.create(CONFIG_NATIVES)
            configurations.create(CONFIG_DEPS)
            configurations.create(CONFIG_MC)

            // Setup extractUserDev
            task('extractUserDev', type: ExtractConfigTask) {
                out = delayedFile '{USER_DEV}'
                config = CONFIG_USERDEV
                doesCache = true
                dependsOn 'getVersionJson'
                doLast {
                    applyJson(devJson.call(), CONFIG_DEPS, CONFIG_NATIVES, it.logger)
                }
            }

            tasks.getAssetsIndex.dependsOn 'extractUserDev'

            // Setup extractNatives
            task('extractNatives', type: ExtractConfigTask) {
                out = delayedFile NATIVES_DIR
                config = CONFIG_NATIVES
                exclude 'META-INF/**', 'META-INF/**'
                doesCache = true
                dependsOn 'extractUserDev'
            }

            def deps = configurations.getByName(CONFIG_DEPS)
            def mc = configurations.getByName(CONFIG_MC)

            dependencies {
                compile fileTree('libs')
                testCompile deps
                testCompile mc
            }

            sourceSets {
                main {
                    compileClasspath += deps
                    compileClasspath += mc
                }
            }

            def main = sourceSets.main
            tasks.javadoc.classpath = main.output + main.compileClasspath

            eclipse {
                classpath {
                    plusConfigurations += [deps, mc]
                    noExportConfigurations += [deps, mc]
                }
            }

            idea {
                module {
                    scopes.PROVIDED.plus += [deps, mc]
                }
            }
        }
    }

    @Override
    void delayedTaskConfig() {
        super.delayedTaskConfig()

        project.with {
            tasks.setupDevWorkspace.dependsOn.remove 'makeStart'
            tasks.setupDecompWorkspace.dependsOn.remove 'makeStart'
            tasks.makeStart.enabled = false
            tasks.withType(JavaExec) {
                if (dependsOn.contains('makeStart')) {
                    enabled = false
                }
            }
        }
    }

    @Override
    protected String getClientTweaker() {
        ''
    }

    @Override
    protected String getServerTweaker() {
        ''
    }

    @Override
    protected String getClientRunClass() {
        'net.minecraft.client.main.Main'
    }

    @Override
    protected String getServerRunClass() {
        'net.minecraft.server.MinecraftServer'
    }


}
