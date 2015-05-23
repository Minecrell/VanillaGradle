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
package net.minecrell.vanillagradle.file

import net.minecraftforge.gradle.delayed.DelayedBase
import net.minecraftforge.gradle.delayed.DelayedFile
import net.minecraftforge.gradle.delayed.DelayedBase.IDelayedResolver
import net.minecraftforge.gradle.tasks.ProcessJarTask
import org.gradle.api.Project

class DelayedDirtyFile extends DynamicDelayedFile {

    private final String name;
    private final String classifier;
    private final String ext;
    private final boolean mappings;

    DelayedDirtyFile(String name, String classifier, String ext, boolean mappings, Project owner, String pattern, IDelayedResolver... resolvers) {
        super(owner, pattern, resolvers)
        this.name = name;
        this.classifier = classifier;
        this.ext = ext;
        this.mappings = mappings;
    }

    @Override
    File resolveDelayed() {
        ProcessJarTask decompDeobf = this.project.tasks.deobfuscateJar

        String pattern
        if (decompDeobf.clean) {
            pattern = '{API_CACHE_DIR}/'
            if (mappings) {
                pattern += '{MAPPING_CHANNEL}/{MAPPING_VERSION}/'
            }
        } else {
            pattern = '{BUILD_DIR}/dirtyArtifacts/'
        }

        pattern += name ?: '{API_NAME}'
        if (!decompDeobf.clean) {
            pattern += "_$project.name"
        }

        pattern += '-{MC_VERSION}'

        if (classifier) {
            pattern += "-$classifier"
        }
        if (ext) {
            pattern += ".$ext"
        }

        setPattern(pattern)
        return super.resolveDelayed()
    }

}
