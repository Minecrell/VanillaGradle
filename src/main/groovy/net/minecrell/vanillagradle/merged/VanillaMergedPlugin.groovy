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

package net.minecrell.vanillagradle.merged

import static net.minecraftforge.gradle.common.Constants.DIR_LOCAL_CACHE
import static net.minecraftforge.gradle.common.Constants.JAR_MERGED
import static net.minecraftforge.gradle.common.Constants.JSON_VERSION
import static net.minecraftforge.gradle.common.Constants.MCP_INJECT
import static net.minecraftforge.gradle.common.Constants.MCP_PATCHES_MERGED
import static net.minecraftforge.gradle.common.Constants.REPLACE_CACHE_DIR
import static net.minecraftforge.gradle.common.Constants.REPLACE_MC_VERSION
import static net.minecraftforge.gradle.common.Constants.TASK_DL_VERSION_JSON
import static net.minecraftforge.gradle.common.Constants.TASK_MERGE_JARS
import static net.minecraftforge.gradle.user.UserConstants.CONFIG_MC
import static net.minecraftforge.gradle.user.UserConstants.TASK_SETUP_CI
import static net.minecraftforge.gradle.user.UserConstants.TASK_SETUP_DEV

import net.minecraftforge.gradle.common.Constants
import net.minecraftforge.gradle.user.UserBasePlugin

class VanillaMergedPlugin extends UserBasePlugin<VanillaMergedExtension> {

    private static final String MCP_INSERT = Constants.REPLACE_MCP_CHANNEL + "/" + Constants.REPLACE_MCP_VERSION
    private static final String CLEAN_ROOT = REPLACE_CACHE_DIR + "/net/minecraft/minecraft_merged/" + REPLACE_MC_VERSION + "/" + MCP_INSERT

    @Override
    protected void applyUserPlugin() {
        // patterns
        def cleanSuffix = "%s-" + REPLACE_MC_VERSION
        def dirtySuffix = "%s-" + REPLACE_MC_VERSION + "-PROJECT(" + project.name + ")"

        makeDecompTasks(
                CLEAN_ROOT + "/minecraft_merged" + cleanSuffix,
                DIR_LOCAL_CACHE + "/minecraft_merged" + dirtySuffix,
                delayedFile(JAR_MERGED),
                TASK_MERGE_JARS,
                delayedFile(MCP_PATCHES_MERGED),
                delayedFile(MCP_INJECT)
        )

        // Add version JSON task to CI and dev workspace tasks
        project.tasks.getByName(TASK_SETUP_CI).dependsOn(TASK_DL_VERSION_JSON)
        project.tasks.getByName(TASK_SETUP_DEV).dependsOn(TASK_DL_VERSION_JSON)
    }

    @Override
    protected void afterDecomp(boolean isDecomp, boolean useLocalCache, String mcConfig) {
        project.allprojects {
            addFlatRepo(it, "VanillaMcRepo", delayedFile(useLocalCache ? DIR_LOCAL_CACHE : CLEAN_ROOT).call())
        }

        // Add the MC dependency
        def group = "net.minecraft"
        def artifact = "minecraft_merged" + (isDecomp ? "Src" : "Bin")
        def version = delayedString(REPLACE_MC_VERSION).call() + (useLocalCache ? "-PROJECT(" + project.name + ")" : "")

        project.dependencies.add(CONFIG_MC, [group: group, name: artifact, version: version])
    }

    @Override
    protected void afterEvaluate() {
        def jsonFile = delayedFile(JSON_VERSION).call()
        if (jsonFile.exists()) {
            parseAndStoreVersion(jsonFile, jsonFile.parentFile)
        }

        super.afterEvaluate()
    }

    @Override
    protected boolean hasServerRun() {
        return true
    }

    @Override
    protected boolean hasClientRun() {
        return true
    }

    @Override
    protected Object getStartDir() {
        return delayedFile(DIR_LOCAL_CACHE + '/start')
    }

    @Override
    protected String getClientTweaker(VanillaMergedExtension ext) {
        return ''
    }

    @Override
    protected String getServerTweaker(VanillaMergedExtension ext) {
        return ''
    }

    @Override
    protected String getClientRunClass(VanillaMergedExtension ext) {
        return ext.mainClass
    }

    @Override
    protected List<String> getClientRunArgs(VanillaMergedExtension ext) {
        return ext.resolvedClientRunArgs
    }

    @Override
    protected List<String> getClientJvmArgs(VanillaMergedExtension ext) {
        return ext.resolvedClientJvmArgs
    }

    @Override
    protected String getServerRunClass(VanillaMergedExtension ext) {
        return ext.mainClass
    }

    @Override
    protected List<String> getServerRunArgs(VanillaMergedExtension ext) {
        return ext.resolvedServerRunArgs
    }

    @Override
    protected List<String> getServerJvmArgs(VanillaMergedExtension ext) {
        return ext.resolvedServerJvmArgs
    }
}
