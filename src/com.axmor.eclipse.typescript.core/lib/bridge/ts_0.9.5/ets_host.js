var fs = require('fs');
var log = require('../log.js');
var args = require('../args.js');
var files = new Array();
var baseDir = '';
var TypeScript = null;

exports.init = function(ts) {
  TypeScript = ts;
  files['lib.d.ts'] = TypeScript.ScriptSnapshot.fromString(fs.readFileSync('./ts_' + args.version + '/lib.d.ts').toString());
}

// logger
exports.information = function() { return true; };
exports.debug = function() { return true; };
exports.warning = function() { return true; };
exports.error = function() { return true; };
exports.fatal = function() { return true; };
exports.log = function(s) {
  log.debug(s);
}

exports.getLocalizedDiagnosticMessages = function() {
  log.debug('tss.getLocalizedDiagnosticMessages');
  return "";
};

exports.getCompilationSettings = function() {
  log.debug("tss.getCompilationSettings");
  return new TypeScript.CompilationSettings();
}

exports.getScriptFileNames = function() {
  log.debug('tss.getScriptFileNames');
  return Object.keys(files);
}

exports.getScriptVersion = function(fileName) {
  //log.debug('tss.getScriptVersion: ' + fileName);
  return 1;
}

exports.getScriptIsOpen = function(fileName) {
  //log.debug('tss.getScriptIsOpen: ' + fileName);
  return true;
}

exports.getScriptByteOrderMark = function(fileName) {
  return 0;
}

exports.getScriptSnapshot = function(fileName) {
  log.debug('tss.getScriptSnapshot: ' + fileName);
//  return TypeScript.SimpleText.fromString(fs.readFileSync(fileName));
  if (files[fileName] == null) {
    files[fileName] = TypeScript.ScriptSnapshot.fromString(fs.readFileSync(baseDir + '/' + fileName).toString());
  }
  return files[fileName]; 
};

exports.getDiagnosticsObject = function() {
  log.debug('tss.getDiagnosticsObject');
  return null;
};

// file related
exports.addDefaultLibrary = function() {
  log.debug('tss.addDefaultLibrary');
  //this.addScript("lib.d.ts", Harness.Compiler.libText);
};

exports.addFile = function(fileName) {
  log.debug('tss.addFile:' + fileName);
//  var code = readFile(fileName).contents;
//  this.addScript(fileName, code);
};

exports.resolveRelativePath = function(path, directory) {
  log.debug('tss.resolveRelativePath: ' + path + '; ' + directory);
  return '';
};

exports.fileExists = function(path) {
  log.debug('tss.fileExists');
  return true;
};

exports.directoryExists = function(path) {
  log.debug('tss.directoryExists');
  return true;
};

exports.getParentDirectory = function(path) {
  log.debug('tss.getParentDirectory');
  return "";
}

exports.setBaseSourceDirectory = function(src) {
  log.debug('tss.setBaseSourceDirectory: ' + src);
  baseDir = src;
  if (fs.statSync(src).isDirectory()) {
    readDir(src, '', files);
  }  
}

exports.setFileContent = function(file, content) {
  log.debug('tss.setFileContent: ' + file);
  files[file] = TypeScript.ScriptSnapshot.fromString(content);
}

function readDir(base, dir, files) {
  log.debug('read dir: ' + dir)       
  var items = fs.readdirSync(base + '/' + dir);
  for (var i in items) {
    var item = items[i];
    var stat = fs.statSync(base + '/' + dir + '/' + item); 
    if (stat.isDirectory()) {
      readDir(base, (dir.length > 0 ? (dir + '/') : '') + item, files);
    } else if (stat.isFile() && item.indexOf('.ts') == item.length - 3) {
      files[(dir.length > 0 ? (dir + '/') : '') + item] = null;
    }
  };
}

//    // use this to test "clean" re-typecheck speed
//    public reTypeCheck() {
//        var settings = new TypeScript.CompilationSettings();
//
//        if (!this.compiler) {
//            this.compiler = new TypeScript.TypeScriptCompiler(new DiagnosticsLogger(), settings);
//
//            this.compiler.addSourceUnit("lib.d.ts", this.libScriptSnapshot, ByteOrderMark.None, 0, false, []);
//            this.compiler.addSourceUnit("compiler.ts", this.compilerScriptSnapshot, ByteOrderMark.None, 0, false, []);
//            this.compiler.pullTypeCheck();
//        }
//
//        this.compiler.semanticInfoChain.update();
//        this.compiler.semanticInfoChain.forceTypeCheck("compiler.ts");
//        this.compiler.getSemanticDiagnostics("compiler.ts");
//    }
//
//    public newParse(): TypeScript.SyntaxTree {
//        return TypeScript.Parser.parse(compilerFileName, this.simpleText, false,
//            TypeScript.getParseOptions(new TypeScript.CompilationSettings()));
//    }
//
//    public newIncrementalParse(tree: TypeScript.SyntaxTree): TypeScript.SyntaxTree {
//        var width = 100;
//        var span = new TypeScript.TextSpan(TypeScript.IntegerUtilities.integerDivide(compilerString.length - width, 2), width);
//        var range = new TypeScript.TextChangeRange(span, width);
//        return TypeScript.Parser.incrementalParse(tree, range, this.simpleText);
//    }


//    public compiler: TypeScript.TypeScriptCompiler;
//    private simpleText = TypeScript.SimpleText.fromString(compilerString);
//    private libScriptSnapshot = TypeScript.ScriptSnapshot.fromString(libString);
//    private compilerScriptSnapshot = TypeScript.ScriptSnapshot.fromString(compilerString);

//    public compile() {
//        var settings = new TypeScript.CompilationSettings();
//        settings.generateDeclarationFiles = true;
//        settings.outFileOption = "Output.ts";
//
//        this.compiler = new TypeScript.TypeScriptCompiler(new DiagnosticsLogger(), settings);
//
//        this.compiler.addSourceUnit("lib.d.ts", this.libScriptSnapshot, ByteOrderMark.None, 0, false, []);
//        this.compiler.addSourceUnit("compiler.ts", this.compilerScriptSnapshot, ByteOrderMark.None, 0, false, []);
//
//        this.compiler.pullTypeCheck();
//
//        var emitterIOHost = {
//            writeFile: (fileName: string, contents: string, writeByteOrderMark: boolean) => { },
//            directoryExists: (a: string) => false,
//            fileExists: (a: string) => true,
//            resolvePath: (a: string) => a,
//        };
//
//        var mapInputToOutput = (inputFile: string, outputFile: string): void => { };
//
//        // TODO: if there are any emit diagnostics.  Don't proceed.
//        var emitDiagnostics = this.compiler.emitAll(emitterIOHost, mapInputToOutput);
//
//        var emitDeclarationsDiagnostics = this.compiler.emitAllDeclarations();
//    }

