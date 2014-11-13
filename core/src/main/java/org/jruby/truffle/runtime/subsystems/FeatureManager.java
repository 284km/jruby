/*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.subsystems;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import com.oracle.truffle.api.source.Source;
import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.*;
import org.jruby.truffle.runtime.control.*;
import org.jruby.truffle.runtime.core.RubyArray;
import org.jruby.util.cli.Options;

/**
 * Manages the features loaded into Ruby. This basically means which library files are loaded, but
 * Ruby often talks about requiring features, not files.
 * 
 */
public class FeatureManager {

    private RubyContext context;

    public FeatureManager(RubyContext context) {
        this.context = context;
    }

    public boolean require(String feature, RubyNode currentNode) throws IOException {
        final RubyConstant dataConstantBefore = ModuleOperations.lookupConstant(context, LexicalScope.NONE, context.getCoreLibrary().getObjectClass(), "DATA");

        try {
            // Some features are handled specially

            if (feature.equals("stringio")) {
                context.getCoreLibrary().loadRubyCore("jruby/truffle/standard/stringio.rb");
                return true;
            }

            if (feature.equals("zlib")) {
                context.getWarnings().warn("zlib not yet implemented");
                return true;
            }

            if (feature.equals("enumerator")) {
                context.getWarnings().warn("enumerator not yet implemented");
                return true;
            }

            if (feature.equals("rbconfig")) {
                // Kernel#rbconfig is always there
                return true;
            }

            if (feature.equals("pp")) {
                // Kernel#pretty_inspect is always there
                return true;
            }

            if (feature.equals("thread")) {
                return true;
            }

            // Get the load path

            // Try as a full path

            if (requireInPath("", feature, currentNode)) {
                return true;
            }

            // Try as a path relative to the current director

            if (requireInPath(context.getRuntime().getCurrentDirectory(), feature, currentNode)) {
                return true;
            }

            // Try each load path in turn

            for (Object pathObject : context.getCoreLibrary().getLoadPath().slowToArray()) {
                final String path = pathObject.toString();

                if (requireInPath(path, feature, currentNode)) {
                    return true;
                }
            }

            // Didn't find the feature

            throw new RaiseException(context.getCoreLibrary().loadErrorCannotLoad(feature, currentNode));
        } finally {
            if (dataConstantBefore == null) {
                context.getCoreLibrary().getObjectClass().removeConstant(currentNode, "DATA");
            } else {
                context.getCoreLibrary().getObjectClass().setConstant(currentNode, "DATA", dataConstantBefore.getValue());
            }
        }
    }

    public boolean requireInPath(String path, String feature, RubyNode currentNode) throws IOException {
        if (requireFile(feature, currentNode)) {
            return true;
        }

        if (requireFile(feature + ".rb", currentNode)) {
            return true;
        }

        if (requireFile(path + File.separator + feature, currentNode)) {
            return true;
        }

        if (requireFile(path + File.separator + feature + ".rb", currentNode)) {
            return true;
        }

        return false;
    }

    private boolean requireFile(String fileName, RubyNode currentNode) throws IOException {
        if (Arrays.asList(context.getCoreLibrary().getLoadedFeatures().slowToArray()).contains(fileName)) {
            return true;
        }

        if (Options.TRUFFLE_LOAD_PRINT.load()) {
            context.getWarnings().warn("%s being loaded", fileName);
        }

        /*
         * There is unfortunately no way to check if a string is a file path, or a URL. file:foo.txt
         * is a valid file name, as well as a valid URL. We try as a file path first.
         */

        if (new File(fileName).isFile()) {
            context.loadFile(fileName, currentNode);
            context.getCoreLibrary().getLoadedFeatures().slowPush(context.makeString(fileName));
            return true;
        } else {
            URL url;

            try {
                url = new URL(fileName);
            } catch (MalformedURLException e) {
                return false;
            }

            InputStream inputStream;

            try {
                inputStream = url.openConnection().getInputStream();
            } catch (IOException e) {
                return false;
            }

            // TODO(CS): can probably simplify this with the new Source API in Truffle

            context.load(Source.fromReader(new InputStreamReader(inputStream), url.toString()), currentNode);
            context.getCoreLibrary().getLoadedFeatures().slowPush(context.makeString(fileName));
            ((RubyArray) context.getCoreLibrary().getGlobalVariablesObject().getInstanceVariable("$LOADED_FEATURES")).slowPush(context.makeString(fileName));
            return true;

        }
    }

}
