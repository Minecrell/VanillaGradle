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

class VanillaPlugin extends BaseVanillaPlugin<VanillaExtension> {

    @Override
    protected Class<VanillaExtension> getExtensionClass() {
        VanillaExtension
    }

    @Override
    protected void configureDeps() {
        super.configureDeps()
        if (extension.launchWrapper) {
            project.dependencies {
                minecraftDeps "net.minecraft:launchwrapper:$extension.launchWrapper"
            }
        }
    }

    @Override
    protected String getClientTweaker() {
        extension.clientTweaker
    }

    @Override
    protected String getServerTweaker() {
        extension.serverTweaker
    }

    @Override
    protected String getClientRunClass() {
        'net.minecraft.launchwrapper.Launch'
    }

    @Override
    protected String getServerRunClass() {
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

}
