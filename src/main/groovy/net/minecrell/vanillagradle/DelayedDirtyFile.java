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
package net.minecrell.vanillagradle;

import net.minecraftforge.gradle.delayed.DelayedFile;
import net.minecraftforge.gradle.tasks.ProcessJarTask;

import org.gradle.api.Project;

import java.io.File;

// java class because I couldn't get groovy to behave
@SuppressWarnings("serial")
public class DelayedDirtyFile extends DelayedFile {

    private String name;
    private String classifier;
    private String ext;
    private boolean mappings;

    public DelayedDirtyFile(String name, String classifier, String ext, boolean mappings, Project owner, String pattern, IDelayedResolver<?>... resolvers) {
        super(owner, pattern, resolvers);
        this.name = name;
        this.classifier = classifier;
        this.ext = ext;
        this.mappings = mappings;
    }

    @Override
    public File resolveDelayed() {
        ProcessJarTask decompDeobf = (ProcessJarTask) this.project.getTasks().getByName("deobfuscateJar");
        pattern = (decompDeobf.isClean() ? "{API_CACHE_DIR}/" + (mappings ? "{MAPPING_CHANNEL}/{MAPPING_VERSION}/" : "") : "{BUILD_DIR}/dirtyArtifacts") + "/";

        if (!isNullOrEmpty(name)) {
            pattern += name;
        } else {
            pattern += "{API_NAME}";
        }

        if (!decompDeobf.isClean())
            pattern += "_" + project.getName();
        pattern += "-{MC_VERSION}";

        if (!isNullOrEmpty(classifier))
            pattern += "-" + classifier;
        if (!isNullOrEmpty(ext))
            pattern += "." + ext;

        return super.resolveDelayed();
    }

    public boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

}
