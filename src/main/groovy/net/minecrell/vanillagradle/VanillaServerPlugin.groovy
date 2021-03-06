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
import static net.minecraftforge.gradle.common.Constants.CONFIG_MC_DEPS_CLIENT
import static net.minecraftforge.gradle.common.Constants.JAR_SERVER_PURE
import static net.minecraftforge.gradle.common.Constants.MCP_INJECT
import static net.minecraftforge.gradle.common.Constants.MCP_PATCHES_SERVER
import static net.minecraftforge.gradle.common.Constants.TASK_SPLIT_SERVER

import groovy.transform.CompileStatic

@CompileStatic
class VanillaServerPlugin extends VanillaPlugin {

    @Override
    protected void applyVanillaUserPlugin() {
        super.applyVanillaUserPlugin()

        // Remove client dependencies
        def config = project.configurations.getByName(CONFIG_MC_DEPS)
        config.extendsFrom = config.extendsFrom - [project.configurations.getByName(CONFIG_MC_DEPS_CLIENT)]
    }

    @Override
    protected String getJarName() {
        "minecraft_server"
    }

    @Override
    protected void createDecompTasks(String globalPattern, String localPattern) {
        makeDecompTasks(globalPattern, localPattern,
                delayedFile(JAR_SERVER_PURE), TASK_SPLIT_SERVER, delayedFile(MCP_PATCHES_SERVER), delayedFile(MCP_INJECT))
    }

}
