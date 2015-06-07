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

import net.minecraftforge.gradle.GradleConfigurationException
import net.minecraftforge.gradle.delayed.DelayedFile
import net.minecraftforge.gradle.tasks.ProcessJarTask
import net.minecraftforge.gradle.user.UserBasePlugin
import net.minecraftforge.gradle.user.UserExtension

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

abstract class BaseVanillaPlugin<T extends UserExtension> extends UserBasePlugin<T> {

    // FML version used for downloading mappings
    private static final Map<String, String> FML_VERSIONS = [
        '1.7.2' :'1.7.2-7.2.158.889', // because why not?
        '1.7.10':'1.7.10-7.10.18.952',
        '1.8'   :'1.8-8.0.49.1047'
    ].asImmutable()
    private static final String CACHE_DIR = "{CACHE_DIR}/minecraft/net/minecrell/vanilla/{FML_VERSION}"

    protected void applyJson(File file, String depConfig, String nativeConfig, Logger log) {
        super.readAndApplyJson(file, depConfig, nativeConfig, log)
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
        "minecraft_${project.name}_src"
    }

    @Override
    protected String getBinDepName() {
        "minecraft_${project.name}_bin"
    }

    @Override
    protected boolean hasApiVersion() {
        false
    }

    @Override
    protected String getApiVersion(T ext) {
        null
    }

    @Override
    protected String getMcVersion(T ext) {
        ext.version
    }

    @Override
    protected String getApiCacheDir(T ext) {
        '{BUILD_DIR}/minecraft/net/minecraft/minecraft_merged/{MC_VERSION}'
    }

    @Override
    protected String getSrgCacheDir(T vanillaExtension) {
        "$CACHE_DIR/srgs"
    }

    @Override
    protected String getUserDevCacheDir(T vanillaExtension) {
        "$CACHE_DIR/unpacked"
    }

    private String getFmlVersion(String mcver) {
        FML_VERSIONS.get(mcver)
    }

    @Override
    protected String getUserDev() {
        def mcver = getMcVersion(extension)
        def module = 'net.minecraftforge:fml'
        if (mcver.startsWith('1.7'))
            module = 'cpw.mods:fml'
        "$module:{FML_VERSION}"
    }

    @Override
    protected String getStartDir() {
        '{BUILD_DIR}/start'
    }

    @Override
    protected Iterable<String> getClientRunArgs() {
        []
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
        if (!FML_VERSIONS.containsKey(version))
            throw new GradleConfigurationException("Minecraft $version is not supported currently")
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
        new LoadingDelayedFile(this, "{USER_DEV}/${extension.version}.json", { File file ->
            if (!file.exists() && new File(file.parentFile, 'dev.json').exists()) {
                def i = BaseVanillaPlugin.getResourceAsStream("/versions/${extension.version}.json")
                if (i != null) {
                    file.createNewFile()
                    i.withStream {
                        file.withOutputStream { o -> o << i }
                    }
                }
            }

            file
        })
    }

    @Override
    protected T getOverlayExtension() {
        null
    }

    @Override
    String resolve(String pattern, Project project, T exten) {
        super.resolve(pattern, project, exten).replace('{FML_VERSION}', getFmlVersion(getMcVersion(exten)))
    }
}
