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
package net.minecrell.vanilla.gradle

import net.minecraftforge.gradle.GradleConfigurationException
import net.minecraftforge.gradle.delayed.DelayedFile
import net.minecraftforge.gradle.tasks.ProcessJarTask
import net.minecraftforge.gradle.user.UserBasePlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

class VanillaGradle extends UserBasePlugin<VanillaExtension> {

    // FML version used for downloading mappings
    private static final String FML_VERSION = '1.8-8.0.49.1047'
    private static final String CACHE_DIR = "{CACHE_DIR}/minecraft/net/minecrell/vanilla/$FML_VERSION"

    @Override
    protected Class<VanillaExtension> getExtensionClass() {
        VanillaExtension
    }

    @Override
    void applyPlugin() {
        super.applyPlugin()
    }

    @Override
    protected void delayedTaskConfig() {
        project.with {
            ProcessJarTask binDeobf = tasks.deobfBinJar
            ProcessJarTask decompDeobf = tasks.deobfuscateJar

            JavaPluginConvention java = convention.plugins['java']
            SourceSet main = java.sourceSets.main

            main.resources.files.each {
                if (it.name.endsWith('_at.cfg')) {
                    logger.lifecycle("Found AccessTransformer in main resources: $it.name")
                    binDeobf.addTransformer(it)
                    decompDeobf.addTransformer(it)
                }
            }
        }

        super.delayedTaskConfig()
    }

    @Override
    String getApiName() {
        'minecraft_merged'
    }

    @Override
    protected String getSrcDepName() {
        'minecraft_merged_src'
    }

    @Override
    protected String getBinDepName() {
        'minecraft_merged_bin'
    }

    @Override
    protected boolean hasApiVersion() {
        false
    }

    @Override
    protected String getApiVersion(VanillaExtension ext) {
        null
    }

    @Override
    protected String getMcVersion(VanillaExtension ext) {
        ext.version
    }

    @Override
    protected String getApiCacheDir(VanillaExtension ext) {
        '{BUILD_DIR}/minecraft/net/minecraft/minecraft_merged/{MC_VERSION}'
    }

    @Override
    protected String getSrgCacheDir(VanillaExtension vanillaExtension) {
        "$CACHE_DIR/srgs"
    }

    @Override
    protected String getUserDevCacheDir(VanillaExtension vanillaExtension) {
        "$CACHE_DIR/unpacked"
    }

    @Override
    protected String getUserDev() {
        "net.minecraftforge:fml:$FML_VERSION"
    }

    @Override
    protected String getClientTweaker() {
        project.minecraft.clientTweaker
    }

    @Override
    protected String getServerTweaker() {
        project.minecraft.serverTweaker
    }

    @Override
    protected String getStartDir() {
        '{BUILD_DIR}/start'
    }

    @Override
    protected String getClientRunClass() {
        'net.minecraft.launchwrapper.Launch'
    }

    @Override
    protected Iterable<String> getClientRunArgs() {
        def result = ['--noCoreSearch']
        def args = project.properties['runArgs']
        if (args != null) {
            result << args
        }

        result
    }

    @Override
    protected String getServerRunClass() {
        'net.minecraft.launchwrapper.Launch'
    }

    @Override
    protected Iterable<String> getServerRunArgs() {
        getClientRunArgs()
    }

    @Override
    protected void configureDeobfuscation(ProcessJarTask processJarTask) {
    }

    @Override
    protected void doVersionChecks(String version) {
        if (version != '1.8')
            throw new GradleConfigurationException('Only Minecraft 1.8 is supported currently')
    }

    @Override
    void applyOverlayPlugin() {
    }

    @Override
    boolean canOverlayPlugin() {
        false
    }

    @Override
    protected DelayedFile getDevJson() {
        new LoadingDelayedFile(this, "$CACHE_DIR/unpacked/dev.json", { File file ->
            if (file.exists()) {
                def i = VanillaGradle.getResourceAsStream("/versions/$project.minecraft.version")
                if (i != null) {
                    i.withStream {
                        file.withOutputStream { o ->
                            o << i
                        }
                    }
                }
            }

            file
        })
    }

    @Override
    protected VanillaExtension getOverlayExtension() {
        null
    }

}
