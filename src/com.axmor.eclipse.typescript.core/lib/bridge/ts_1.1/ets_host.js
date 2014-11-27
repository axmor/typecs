var fs = require('fs');
var log = require('../log.js');
var args = require('../args.js');
var files = new Array();
var baseDir = '';
var TypeScript = null;

/************************************************************************* 
    Service Host interface from services.ts
    //
    // Public interface of the host of a language service instance.
    //
    export interface LanguageServiceHost extends Logger {
        getCompilationSettings(): CompilerOptions;
        getScriptFileNames(): string[];
        getScriptVersion(fileName: string): string;
        getScriptIsOpen(fileName: string): boolean;
        getScriptSnapshot(fileName: string): TypeScript.IScriptSnapshot;
        getLocalizedDiagnosticMessages(): any;
        getCancellationToken(): CancellationToken;
    }
***************************************************************************/

exports.init = function(ts) {
  TypeScript = ts;
  files['std-lib/lib.d.ts'] = {
     version: 1,
     snapshot: TypeScript.ScriptSnapshot.fromString(fs.readFileSync('./ts_' + args.version + '/lib.d.ts').toString())
  };
}

exports.getCompilationSettings = function() {
  log.debug('host.getCompilationSettings');
  return {
    target: "ES5",
    module: "None"
  };
}//(): CompilerOptions;

exports.getScriptFileNames = function() {
  log.debug('host.getScriptFileNames');
  return Object.keys(files);
}//(): string[];

exports.getScriptVersion = function(fileName) {
  log.debug('host.getScriptVersion: ' + fileName + " - " + (files[fileName] && files[fileName].version ? files[fileName].version : "N/A"));
  if (files[fileName] && files[fileName].version ) {
    return files[fileName].version;
  }
  return 0;
}//(fileName: string): string;

exports.getScriptIsOpen = function(fileName) {
  log.debug('host.getScriptIsOpen');
  return true;
}//(fileName: string): boolean;

exports.getScriptSnapshot = function(fileName) {
  log.debug('host.getScriptSnapshot: ' + fileName + " " + (files[fileName] && files[fileName].version));
//  return TypeScript.SimpleText.fromString(fs.readFileSync(fileName));
  if (files[fileName] == null) { 
    files[fileName] = emptyEntry();
  }
  // we added white space to end of content to avoid code completion error
  if (files[fileName].snapshot == null) {
    files[fileName].snapshot = TypeScript.ScriptSnapshot.fromString(fs.readFileSync(baseDir + '/' + fileName).toString() + ' ')
  }
  return files[fileName].snapshot; 
}//(fileName: string): TypeScript.IScriptSnapshot;

exports.getLocalizedDiagnosticMessages = function() {
  log.debug('host.getLocalizedDiagnosticMessages');
  return "";
}//(): any;

exports.getCancellationToken = function() {
  log.debug('host.getCancellationToken');
  return {
    isCancellationRequested: function() {
      return false;
    }
  }; 
}//(): CancellationToken;

/// inner our methods 
exports.setBaseSourceDirectory = function(src) {
  log.debug('host.setBaseSourceDirectory: ' + src);
  baseDir = src;
  if (fs.statSync(src).isDirectory()) {
    readDir(src, '', files);
  }  
}

function readDir(base, dir, files) {
  log.debug('read dir: ' + dir)       
  var items = fs.readdirSync(base + '/' + dir);
  items.forEach(function(item) {
    var stat = fs.statSync(base + '/' + dir + '/' + item); 
    if (stat.isDirectory()) {
       readDir(base, (dir.length > 0 ? (dir + '/') : '') + item, files);
    } else if (stat.isFile() && item.indexOf('.ts') == item.length - 3) {
      var name = (dir.length > 0 ? (dir + '/') : '') + item;
      if (files[name]) {
        files[name].snapshot = null;
      } else {
        files[name] = emptyEntry();
      }
    }
  });
}

function emptyEntry() {
  return {
    version: 1,
    snapshot: null
  };
}

exports.setFileContent = function(file, content) {
  log.debug('host.setFileContent: ' + file +":" + content);
  // we added white space to end of content to avoid code completion error
  var c = content ? (content + ' ') : ' ';
  var snapshot = TypeScript.ScriptSnapshot.fromString(c);
  var old = null;
  if (files[file]) {
    var f = files[file];
    old = f.snapshot.getText(0, f.snapshot.getLength())
    if (old != c) {
      f.version ++;
    }
  } else { 
    files[fileName] = emptyEntry();
  }
  files[file].snapshot = TypeScript.ScriptSnapshot.fromString(c);
  files[file].snapshot.getChangeRange = function(oldSnapshot) {
     return null;
  }
}

exports.addFile = function(fileName) {
  log.debug('host.addFile:' + fileName);
  if (files[fileName]) {
    files[fileName].version++;
    files[fileName].snapshot = null;
  } else {
  files[fileName] = emptyEntry();
  }
};

exports.log = function(msg) {
  log.debug("[TS]:" + msg);
};

/*
exports.init = function(ts) {
  TypeScript = ts;
  files['std-lib/lib.d.ts'] = {
    version: 1,
    snapshot: TypeScript.ScriptSnapshot.fromString(fs.readFileSync('./ts_' + args.version + '/lib.d.ts').toString())
  };
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

// file related
exports.addDefaultLibrary = function() {
  log.debug('tss.addDefaultLibrary');
  //this.addScript("lib.d.ts", Harness.Compiler.libText);
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
  log.debug('tss.setFileContent: ' + file +":" + content);
  // we added white space to end of content to avoid code completion error
  var c = content ? (content + ' ') : ' ';
  var snapshot = TypeScript.ScriptSnapshot.fromString(c);
  var old = null;
  if (files[file]) {
    var f = files[file];
    old = f.snapshot.getText(0, f.snapshot.getLength())
    if (old != c) {
      f.version ++;
    }
  } else { 
    files[fileName] = emptyEntry();
  }
  files[file].snapshot = TypeScript.ScriptSnapshot.fromString(c);
  files[file].snapshot.getTextChangeRangeSinceVersion = function(scriptVersion) {
    return null;
  }
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
      var name = (dir.length > 0 ? (dir + '/') : '') + item;
      if (files[name]) {
        files[name].snapshot = null;
      } else {
        files[name] = emptyEntry();
      }
    }
  };
}

function emptyEntry() {
  return {
    version: 1,
    snapshot: null
  };
}
*/