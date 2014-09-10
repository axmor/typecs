var fs = require('fs');
var log = require('../log.js');
var args = require('../args.js');
var files = new Array();
var baseDir = '';
var TypeScript = null;

exports.init = function(ts) {
  TypeScript = ts;
  files['std-lib/lib.d.ts'] = TypeScript.ScriptSnapshot.fromString(fs.readFileSync('./ts_' + args.version + '/lib.d.ts').toString());
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
  // we added white space to end of content to avoid code completion error 
    files[fileName] = TypeScript.ScriptSnapshot.fromString(fs.readFileSync(baseDir + '/' + fileName).toString() + ' ');
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
  files[fileName] = null;
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
  // we added white space to end of content to avoid code completion error 
  files[file] = TypeScript.ScriptSnapshot.fromString(content ? (content + ' ') : ' ');
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
