/***** BEGIN LICENSE BLOCK *****
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2007-2011 Nick Sieger <nicksieger@gmail.com>
 * Copyright (C) 2009 Joseph LaFata <joe@quibb.org>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.truffle.options;

import jnr.posix.util.Platform;
import org.jruby.truffle.core.string.StringSupport;
import org.jruby.truffle.language.control.JavaException;
import org.jruby.util.ClasspathLauncher;
import org.jruby.util.FileResource;
import org.jruby.util.InputStreamMarkCursor;
import org.jruby.util.JRubyFile;
import org.jruby.truffle.util.SafePropertyAccessor;
import org.jruby.util.cli.Options;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A structure used to configure new JRuby instances. All publicly-tweakable
 * aspects of Ruby can be modified here, including those settable by command-
 * line options, those available through JVM properties, and those suitable for
 * embedding.
 */
public class RubyInstanceConfig {

    public RubyInstanceConfig(boolean isSecurityRestricted) {
        this.isSecurityRestricted = isSecurityRestricted;
        currentDirectory = isSecurityRestricted ? "/" : JRubyFile.getFileProperty("user.dir");

        if (isSecurityRestricted) {
            compileMode = org.jruby.RubyInstanceConfig.CompileMode.OFF;
        } else {
            if (COMPILE_EXCLUDE != null) {
                excludedMethods.addAll(StringSupport.split(COMPILE_EXCLUDE, ','));
            }
        }
        initEnvironment();
    }

    private void initEnvironment() {
        environment = new HashMap<String,String>();
        try {
            environment.putAll(System.getenv());
        }
        catch (SecurityException se) { /* ignore missing getenv permission */ }
        setupEnvironment(getJRubyHome());
    }

    public void processArguments(String[] arguments) {
        new ArgumentProcessor(arguments, this).processArguments();
        tryProcessArgumentsWithRubyopts();
    }

    public void tryProcessArgumentsWithRubyopts() {
        try {
            processArgumentsWithRubyopts();
        } catch (SecurityException se) {
            // ignore and do nothing
        }
    }

    public void processArgumentsWithRubyopts() {
        // environment defaults to System.getenv normally
        Object rubyoptObj = environment.get("RUBYOPT");
        String rubyopt = rubyoptObj == null ? null : rubyoptObj.toString();

        if (rubyopt == null || rubyopt.length() == 0) return;

        String[] rubyoptArgs = rubyopt.split("\\s+");
        if (rubyoptArgs.length != 0) {
            new ArgumentProcessor(rubyoptArgs, false, true, true, this).processArguments();
        }
    }

    // This method does not work like previous version in verifying it is
    // a Ruby shebang line.  Looking for ruby before \n is possible to add,
    // but I wanted to keep this short.
    private boolean isShebang(InputStreamMarkCursor cursor) throws IOException {
        if (cursor.read() == '#') {
            int c = cursor.read();
            if (c == '!') {
                cursor.endPoint(-2);
                return true;
            } else if (c == '\n') {
                cursor.rewind();
            }
        } else {
            cursor.rewind();
        }

        return false;
    }

    private static final Pattern RUBY_SHEBANG = Pattern.compile("#!.*ruby.*");

    protected static boolean isRubyShebangLine(String line) {
        return RUBY_SHEBANG.matcher(line).matches();
    }

    private static final String URI_PREFIX_STRING = "^(uri|jar|file|classpath):([^:/]{2,}:([^:/]{2,}:)?)?";
    private static final Pattern ROOT_PATTERN = Pattern.compile(URI_PREFIX_STRING + "/?/?$");
    public static Pattern PROTOCOL_PATTERN = Pattern.compile(URI_PREFIX_STRING + ".*");

    private String calculateJRubyHome() {
        String newJRubyHome = null;

        // try the normal property first
        if (!isSecurityRestricted) {
            newJRubyHome = SafePropertyAccessor.getProperty("jruby.home");
        }

        if (newJRubyHome == null && getLoader().getResource("META-INF/jruby.home/.jrubydir") != null) {
            newJRubyHome = "uri:classloader://META-INF/jruby.home";
        }
        if (newJRubyHome != null) {
            // verify it if it's there
            newJRubyHome = verifyHome(newJRubyHome, error);
        } else {
            try {
                newJRubyHome = SafePropertyAccessor.getenv("JRUBY_HOME");
            } catch (Exception e) {}

            if (newJRubyHome != null) {
                // verify it if it's there
                newJRubyHome = verifyHome(newJRubyHome, error);
            } else {
                // otherwise fall back on system temp location
                newJRubyHome = SafePropertyAccessor.getProperty("java.io.tmpdir");
            }
        }

        // RegularFileResource absolutePath will canonicalize resources so that will change c: paths to C:.
        // We will cannonicalize on windows so that jruby.home is also C:.
        // assume all those uri-like pathnames are already in absolute form
        if (Platform.IS_WINDOWS && !PROTOCOL_PATTERN.matcher(newJRubyHome).matches()) {
            try {
                newJRubyHome = new File(newJRubyHome).getCanonicalPath();
            }
            catch (IOException e) {} // just let newJRubyHome stay the way it is if this fails
        }

        return newJRubyHome == null ? null : JRubyFile.normalizeSeps(newJRubyHome);
    }

    // We require the home directory to be absolute
    private static String verifyHome(String home, PrintStream error) {
        if ("uri:classloader://META-INF/jruby.home".equals(home) || "uri:classloader:/META-INF/jruby.home".equals(home)) {
            return home;
        }
        if (home.equals(".")) {
            home = SafePropertyAccessor.getProperty("user.dir");
        }
        else if (home.startsWith("cp:")) {
            home = home.substring(3);
        }
        if (home.startsWith("jar:") || ( home.startsWith("file:") && home.contains(".jar!/") ) ||
                home.startsWith("classpath:") || home.startsWith("uri:")) {
            error.println("Warning: JRuby home with uri like paths may not have full functionality - use at your own risk");
        }
        // do not normalize on plain jar like pathes coming from jruby-rack
        else if (!home.contains(".jar!/") && !home.startsWith("uri:")) {
            File file = new File(home);
            if (!file.exists()) {
                final String tmpdir = SafePropertyAccessor.getProperty("java.io.tmpdir");
                error.println("Warning: JRuby home \"" + file + "\" does not exist, using " + tmpdir);
                return tmpdir;
            }
            if (!file.isAbsolute()) {
                home = file.getAbsolutePath();
            }
        }
        return home;
    }

    public byte[] inlineScript() {
        return inlineScript.toString().getBytes();
    }

    public InputStream getScriptSource() {
        try {
            // KCode.NONE is used because KCODE does not affect parse in Ruby 1.8
            // if Ruby 2.0 encoding pragmas are implemented, this will need to change
            if (hasInlineScript) {
                return new ByteArrayInputStream(inlineScript());
            } else if (isForceStdin() || getScriptFileName() == null) {
                // can't use -v and stdin
                if (isShowVersion()) {
                    return null;
                }
                return getInput();
            } else {
                final String script = getScriptFileName();
                FileResource resource = JRubyFile.createRestrictedResource(getCurrentDirectory(), getScriptFileName());
                if (resource != null && resource.exists()) {
                    if (resource.canRead() && !resource.isDirectory()) {
                        if (isXFlag()) {
                            // search for a shebang line and
                            // return the script between shebang and __END__ or CTRL-Z (0x1A)
                            return findScript(resource.inputStream());
                        }
                        return resource.inputStream();
                    }
                    else {
                        throw new FileNotFoundException(script + " (Not a file)");
                    }
                }
                else {
                    throw new FileNotFoundException(script + " (No such file or directory)");
                }
            }
        } catch (IOException e) {
            throw new JavaException(e);
        }
    }

    private static InputStream findScript(InputStream is) throws IOException {
        StringBuilder buf = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String currentLine = br.readLine();
        while (currentLine != null && !isRubyShebangLine(currentLine)) {
            currentLine = br.readLine();
        }

        buf.append(currentLine);
        buf.append("\n");

        do {
            currentLine = br.readLine();
            if (currentLine != null) {
                buf.append(currentLine);
                buf.append("\n");
            }
        } while (!(currentLine == null || currentLine.contains("__END__") || currentLine.contains("\026")));
        return new BufferedInputStream(new ByteArrayInputStream(buf.toString().getBytes()), 8192);
    }

    public String displayedFileName() {
        if (hasInlineScript) {
            if (scriptFileName != null) {
                return scriptFileName;
            } else {
                return "-e";
            }
        } else if (isForceStdin() || getScriptFileName() == null) {
            return "-";
        } else {
            return getScriptFileName();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and setters for config settings.
    ////////////////////////////////////////////////////////////////////////////

    public String getJRubyHome() {
        if (jrubyHome == null) {
            jrubyHome = calculateJRubyHome();
        }
        return jrubyHome;
    }

    public void setCompileMode(org.jruby.RubyInstanceConfig.CompileMode compileMode) {
        this.compileMode = compileMode;
    }

    public void setInput(InputStream newInput) {
        input = newInput;
    }

    public InputStream getInput() {
        return input;
    }

    public void setOutput(PrintStream newOutput) {
        output = newOutput;
    }

    public PrintStream getOutput() {
        return output;
    }

    public void setError(PrintStream newError) {
        error = newError;
    }

    public PrintStream getError() {
        return error;
    }

    public void setCurrentDirectory(String newCurrentDirectory) {
        currentDirectory = newCurrentDirectory;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setEnvironment(Map<String, String> newEnvironment) {
        environment = new HashMap<String, String>();
        if (newEnvironment != null) {
            environment.putAll(newEnvironment);
        }
        setupEnvironment(getJRubyHome());
    }

    private void setupEnvironment(String jrubyHome) {
        if (PROTOCOL_PATTERN.matcher(jrubyHome).matches() && !environment.containsKey("RUBY")) {
            // the assumption that if JRubyHome is not a regular file that jruby
            // got launched in an embedded fashion
            environment.put("RUBY", ClasspathLauncher.jrubyCommand(defaultClassLoader()));
        }
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }


    public String[] getArgv() {
        return argv;
    }

    public void setArgv(String[] argv) {
        this.argv = argv;
    }

    public StringBuffer getInlineScript() {
        return inlineScript;
    }

    public void setHasInlineScript(boolean hasInlineScript) {
        this.hasScriptArgv = true;
        this.hasInlineScript = hasInlineScript;
    }

    public Collection<String> getRequiredLibraries() {
        return requiredLibraries;
    }

    public List<String> getLoadPaths() {
        return loadPaths;
    }

    /**
     * @see Options#CLI_HELP
     */
    public void setShouldPrintUsage(boolean shouldPrintUsage) {
        this.shouldPrintUsage = shouldPrintUsage;
    }

    /**
     * @see Options#CLI_HELP
     */
    public boolean getShouldPrintUsage() {
        return shouldPrintUsage;
    }

    /**
     * @see Options#CLI_PROPERTIES
     */
    public void setShouldPrintProperties(boolean shouldPrintProperties) {
        this.shouldPrintProperties = shouldPrintProperties;
    }

    /**
     * @see Options#CLI_PROPERTIES
     */
    public boolean getShouldPrintProperties() {
        return shouldPrintProperties;
    }

    public boolean isInlineScript() {
        return hasInlineScript;
    }

    /**
     * True if we are only using source from stdin and not from a -e or file argument.
     */
    public boolean isForceStdin() {
        return forceStdin;
    }

    /**
     * Set whether we should only look at stdin for source.
     */
    public void setForceStdin(boolean forceStdin) {
        this.forceStdin = forceStdin;
    }

    public void setScriptFileName(String scriptFileName) {
        this.hasScriptArgv = true;
        this.scriptFileName = scriptFileName;
    }

    public String getScriptFileName() {
        return scriptFileName;
    }

    /**
     * @see Options#CLI_ASSUME_LOOP
     */
    public void setAssumeLoop(boolean assumeLoop) {
        this.assumeLoop = assumeLoop;
    }

    /**
     * @see Options#CLI_ASSUME_PRINT
     */
    public void setAssumePrinting(boolean assumePrinting) {
        this.assumePrinting = assumePrinting;
    }

    /**
     * @see Options#CLI_PROCESS_LINE_ENDS
     */
    public void setProcessLineEnds(boolean processLineEnds) {
        this.processLineEnds = processLineEnds;
    }

    /**
     * @see Options#CLI_AUTOSPLIT
     */
    public void setSplit(boolean split) {
        this.split = split;
    }

    /**
     * @see Options#CLI_AUTOSPLIT
     */
    public boolean isSplit() {
        return split;
    }

    /**
     * @see Options#CLI_WARNING_LEVEL
     */
    public void setVerbosity(Verbosity verbosity) {
        this.verbosity = verbosity;
    }

    /**
     * @see Options#CLI_VERBOSE
     */
    public boolean isVerbose() {
        return verbosity == Verbosity.TRUE;
    }

    /**
     * @see Options#CLI_DEBUG
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @see Options#CLI_DEBUG
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @see Options#CLI_VERSION
     */
    public void setShowVersion(boolean showVersion) {
        this.showVersion = showVersion;
    }

    /**
     * @see Options#CLI_VERSION
     */
    public boolean isShowVersion() {
        return showVersion;
    }

    /**
     * @see Options#CLI_BYTECODE
     */
    public void setShowBytecode(boolean showBytecode) {
        this.showBytecode = showBytecode;
    }

    /**
     * @see Options#CLI_COPYRIGHT
     */
    public void setShowCopyright(boolean showCopyright) {
        this.showCopyright = showCopyright;
    }

    /**
     * @see Options#CLI_COPYRIGHT
     */
    public boolean isShowCopyright() {
        return showCopyright;
    }

    public void setShouldRunInterpreter(boolean shouldRunInterpreter) {
        this.shouldRunInterpreter = shouldRunInterpreter;
    }

    public boolean getShouldRunInterpreter() {
        return shouldRunInterpreter && (hasScriptArgv || !showVersion);
    }

    /**
     * @see Options#CLI_CHECK_SYNTAX
     */
    public void setShouldCheckSyntax(boolean shouldSetSyntax) {
        this.shouldCheckSyntax = shouldSetSyntax;
    }

    /**
     * @see Options#CLI_CHECK_SYNTAX
     */
    public boolean getShouldCheckSyntax() {
        return shouldCheckSyntax;
    }

    /**
     * @see Options#CLI_AUTOSPLIT_SEPARATOR
     */
    public void setInputFieldSeparator(String inputFieldSeparator) {
        this.inputFieldSeparator = inputFieldSeparator;
    }

    /**
     * @see Options#CLI_ENCODING_INTERNAL
     */
    public void setInternalEncoding(String internalEncoding) {
        this.internalEncoding = internalEncoding;
    }

    /**
     * @see Options#CLI_ENCODING_INTERNAL
     */
    public String getInternalEncoding() {
        return internalEncoding;
    }

    /**
     * @see Options#CLI_ENCODING_EXTERNAL
     */
    public void setExternalEncoding(String externalEncoding) {
        this.externalEncoding = externalEncoding;
    }

    /**
     * @see Options#CLI_ENCODING_EXTERNAL
     */
    public String getExternalEncoding() {
        return externalEncoding;
    }

    /**
     * @see Options#CLI_BACKUP_EXTENSION
     */
    public void setInPlaceBackupExtension(String inPlaceBackupExtension) {
        this.inPlaceBackupExtension = inPlaceBackupExtension;
    }

    /**
     * @see Options#CLI_BACKUP_EXTENSION
     */
    public String getInPlaceBackupExtension() {
        return inPlaceBackupExtension;
    }

    public Map<String, String> getOptionGlobals() {
        return optionGlobals;
    }

    public boolean isArgvGlobalsOn() {
        return argvGlobalsOn;
    }

    public void setArgvGlobalsOn(boolean argvGlobalsOn) {
        this.argvGlobalsOn = argvGlobalsOn;
    }

    /**
     * @see Options#CLI_RUBYGEMS_ENABLE
     */
    public boolean isDisableGems() {
        return disableGems;
    }

    /**
     * @see Options#CLI_RUBYGEMS_ENABLE
     */
    public void setDisableGems(boolean dg) {
        this.disableGems = dg;
    }

    /**
     * @see Options#CLI_STRIP_HEADER
     */
    public void setXFlag(boolean xFlag) {
        this.xFlag = xFlag;
    }

    /**
     * @see Options#CLI_STRIP_HEADER
     */
    public boolean isXFlag() {
        return xFlag;
    }

    public boolean isFrozenStringLiteral() {
        return frozenStringLiteral;
    }

    public void setFrozenStringLiteral(boolean frozenStringLiteral) {
        this.frozenStringLiteral = frozenStringLiteral;
    }


    public static ClassLoader defaultClassLoader() {
        ClassLoader loader = RubyInstanceConfig.class.getClassLoader();

        // loader can be null for example when jruby comes from the boot-classLoader

        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }

        return loader;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Configuration fields.
    ////////////////////////////////////////////////////////////////////////////

    private final boolean isSecurityRestricted;

    /**
     * Indicates whether the script must be extracted from script source
     */
    private boolean xFlag = Options.CLI_STRIP_HEADER.load();

    /**
     * Indicates whether the script has a shebang line or not
     */
    private InputStream input          = System.in;
    private PrintStream output         = System.out;
    private PrintStream error          = System.err;

    private org.jruby.RubyInstanceConfig.CompileMode compileMode = org.jruby.RubyInstanceConfig.CompileMode.OFF;
    private String currentDirectory;

    /** Environment variables; defaults to System.getenv() in constructor */
    private Map<String, String> environment;
    private String[] argv = {};

    private String internalEncoding = Options.CLI_ENCODING_INTERNAL.load();
    private String externalEncoding = Options.CLI_ENCODING_EXTERNAL.load();

    private ClassLoader loader = defaultClassLoader();

    // from CommandlineParser
    private List<String> loadPaths = new ArrayList<String>();
    private Set<String> excludedMethods = new HashSet<String>();
    private StringBuffer inlineScript = new StringBuffer();
    private boolean hasInlineScript = false;
    private String scriptFileName = null;
    private Collection<String> requiredLibraries = new LinkedHashSet<String>();
    private boolean argvGlobalsOn = false;
    private boolean assumeLoop = Options.CLI_ASSUME_LOOP.load();
    private boolean assumePrinting = Options.CLI_ASSUME_PRINT.load();
    private Map<String, String> optionGlobals = new HashMap<String, String>();
    private boolean processLineEnds = Options.CLI_PROCESS_LINE_ENDS.load();
    private boolean split = Options.CLI_AUTOSPLIT.load();
    private Verbosity verbosity = Verbosity.NIL;
    private boolean debug = Options.CLI_DEBUG.load();
    private boolean showVersion = Options.CLI_VERSION.load();
    private boolean showBytecode = Options.CLI_BYTECODE.load();
    private boolean showCopyright = Options.CLI_COPYRIGHT.load();
    private boolean shouldRunInterpreter = true;
    private boolean shouldPrintUsage = Options.CLI_HELP.load();
    private boolean shouldPrintProperties=Options.CLI_PROPERTIES.load();
    private boolean shouldCheckSyntax = Options.CLI_CHECK_SYNTAX.load();
    private String inputFieldSeparator = Options.CLI_AUTOSPLIT_SEPARATOR.load();
    private String inPlaceBackupExtension = Options.CLI_BACKUP_EXTENSION.load();
    private boolean disableGems = !Options.CLI_RUBYGEMS_ENABLE.load();
    private boolean hasScriptArgv = false;
    private boolean frozenStringLiteral = false;
    private String jrubyHome;

    private boolean forceStdin = false;

    ////////////////////////////////////////////////////////////////////////////
    // Static configuration fields, used as defaults for new JRuby instances.
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Comma-separated list of methods to exclude from JIT compilation.
     * Specify as "Module", "Module#method" or "method".
     *
     * Set with the <tt>jruby.jit.exclude</tt> system property.
     */
    public static final String COMPILE_EXCLUDE = Options.JIT_EXCLUDE.load();

    /**
     * Indicates the global default for whether native code is enabled. Default
     * is true. This value is used to default new runtime configurations.
     *
     * Set with the <tt>jruby.native.enabled</tt> system property.
     */
    public static final boolean NATIVE_ENABLED = Options.NATIVE_ENABLED.load();

    @Deprecated
    public final static boolean CEXT_ENABLED = false;

    /**
     * Turn on debugging of script resolution with "-S".
     *
     * Set with the <tt>jruby.debug.scriptResolution</tt> system property.
     */
    public static final boolean DEBUG_SCRIPT_RESOLUTION = Options.DEBUG_SCRIPTRESOLUTION.load();

    @Deprecated
    public static final boolean JIT_CACHE_ENABLED = Options.JIT_CACHE.load();

    @Deprecated
    public void setSafeLevel(int safeLevel) {
    }

    @Deprecated
    public String getInPlaceBackupExtention() {
        return inPlaceBackupExtension;
    }

    @Deprecated
    public String getBasicUsageHelp() {
        return OutputStrings.getBasicUsageHelp();
    }

    @Deprecated
    public String getExtendedHelp() {
        return OutputStrings.getExtendedHelp();
    }

    @Deprecated
    public String getPropertyHelp() {
        return OutputStrings.getPropertyHelp();
    }

    @Deprecated
    public String getVersionString() {
        return OutputStrings.getVersionString();
    }

    @Deprecated
    public String getCopyrightString() {
        return OutputStrings.getCopyrightString();
    }

    @Deprecated
    public Collection<String> requiredLibraries() {
        return requiredLibraries;
    }

    @Deprecated
    public List<String> loadPaths() {
        return loadPaths;
    }

    @Deprecated
    public boolean shouldPrintUsage() {
        return shouldPrintUsage;
    }

    @Deprecated
    public boolean shouldPrintProperties() {
        return shouldPrintProperties;
    }

    @Deprecated
    public Boolean getVerbose() {
        return isVerbose();
    }

    public Verbosity getVerbosity() {
        return verbosity;
    }

    @Deprecated
    public boolean shouldRunInterpreter() {
        return isShouldRunInterpreter();
    }

    @Deprecated
    public boolean isShouldRunInterpreter() {
        return shouldRunInterpreter;
    }

    @Deprecated
    public boolean isxFlag() {
        return xFlag;
    }

    @Deprecated
    public static final boolean nativeEnabled = NATIVE_ENABLED;

    @Deprecated
    public boolean isSamplingEnabled() {
        return false;
    }

    @Deprecated
    public void setBenchmarking(boolean benchmarking) {
    }

    @Deprecated
    public boolean isBenchmarking() {
        return false;
    }

    @Deprecated
    public void setCextEnabled(boolean b) {
    }

    @Deprecated
    public boolean isCextEnabled() {
        return false;
    }

    @Deprecated public static final String JIT_CODE_CACHE = "";
}
