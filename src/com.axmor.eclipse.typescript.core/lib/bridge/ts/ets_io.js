var _fs = require('fs');
var _path = require('path');
var _module = require('module');
var log = require('../log.js');
var args = require('../args.js');
var TypeScript = require('./tsc.js');

TypeScript.Debug.assert = function(expression, message, verboseDebugInfo) {
  if (!expression) {
//    throw new Error("Debug Failure. False expression: " + (message || ""));
  }
};

exports.compile = function(file, _settings) {
  var settings = getCompilationSettings(args.src, _settings);
  var compilerHost= TypeScript.createCompilerHost(settings);
  compilerHost.getCurrentDirectory = function() {
    return args.src;
  };
  compilerHost.getDefaultLibFilename = function() {
    return "./ts/lib.d.ts";
  }

  var program = TypeScript.createProgram([file], settings, compilerHost);

  var bindStart = new Date().getTime();
  var errors = program.getDiagnostics();
  if (!errors.length) {
    var checker = program.getTypeChecker(/*fullTypeCheckMode*/ true);
    var semanticErrors = checker.getDiagnostics();
    var emitErrors = checker.emitFiles().errors;
    errors = TypeScript.concatenate(semanticErrors, emitErrors);
  }
  
  var result = {
    errors : []
  };
  
  for (var i = 0; i < errors.length; i++) {
    var e = errors[i];
    result.errors.push({
      file: e.file ? e.file.filename : "",
      text : e.messageText,
      severity: e.category,
      code: e.code,
      start: e.start,
      length: e.length
    });
  }

  return result;
}

function getCompilationSettings(basedir, settings) {
  var s = settings;
  return {
    removeComments: s.removeComments,
    noResolve: s.noResolve,
    noImplicitAny: s.noImplicitAny,
    noLib: s.noLib,
    target: s.codeGenTarget,
    module: s.moduleGenTarget,
    out: s.outFileOption === "" ? "" : basedir + "/" + s.outFileOption,
    outDir: s.outDirOption === "" ? "" : basedir + "/" + s.outDirOption,
    sourceMap: s.mapSourceFiles,
    mapRoot: s.mapRoot,
    sourceRoot: s.sourceRoot,
    declaration: s.generateDeclarationFiles,
    useCaseSensitiveFileResolution: s.useCaseSensitiveFileResolution,
    codepage: "UTF-8" /*s.codepage;*/,
    emitBOM: false,
    createFileLog: false 
  };
};