var _fs = require('fs');
var _path = require('path');
var _module = require('module');
var log = require('../log.js');
var args = require('../args.js');
var TypeScript = require('./tsc.js');

var io = getIO(args.src);
var batch = new TypeScript.BatchCompiler(io);

exports.compile = function(file, settings) {
  io.init();                                         
  
  batch.hasErrors = false;
  batch.fileNameToSourceFile = new TypeScript.StringHashTable();
  
  var result = {
    errors : []
  };
  batch.inputFiles = [ file ];
  // skip parse options
  batch.parseOptions = function() {
    return true;
  };
  
  batch.compilationSettings = getCompilationSettings(settings);
  batch.addDiagnostic = function(diag) {
    hasErrors = true;  
    var info = diag.info();
    result.errors.push({
      file: diag.fileName(),
      text : diag.text(),
      severity: info.category,
      code: info.code,
      start: diag.start(),
      length: diag.length()
    });
  };
  batch.batchCompile();
  result.files = io.getChangedFiles();
  return result;
}

function getCompilationSettings(settings) {
  var s = settings;
  return {
    propagateEnumConstants: function() { return s.propagateEnumConstants; },
    removeComments: function() { return s.removeComments; },
    watch: function() { return s.watch; },
    noResolve: function() { return s.noResolve; },
    allowBool: function() { return s.allowBool; },
    allowAutomaticSemicolonInsertion: function() { return s.allowAutomaticSemicolonInsertion; },
    allowModuleKeywordInExternalModuleReference: function() { return s.allowModuleKeywordInExternalModuleReference; },
    noImplicitAny: function() { return s.noImplicitAny; },
    noLib: function() { return s.noLib; },
    codeGenTarget: function() { return s.codeGenTarget; },
    moduleGenTarget: function() { return s.moduleGenTarget; },
    outFileOption: function() { return s.outFileOption; },
    outDirOption: function() { return s.outDirOption; },
    mapSourceFiles: function() { return s.mapSourceFiles; },
    mapRoot: function() { return s.mapRoot; },
    sourceRoot: function() { return s.sourceRoot; },
    generateDeclarationFiles: function() { return s.generateDeclarationFiles; },
    useCaseSensitiveFileResolution: function() { return s.useCaseSensitiveFileResolution; },
    gatherDiagnostics: function() { return s.gatherDiagnostics; },
    updateTC: function() { return s.updateTC; },
    codepage: function() { return null; /*s.codepage;*/ },
    createFileLog: function() {return false;} 
  };
};

function getIO(_baseDir) {
  var baseDir = _baseDir;
  var files = [];  
  return {
    init: function() {
      files = [];
    },
    
    getChangedFiles: function() {
      return files;
    },              
    
    readFile: function (file, codepage) {
      log.debug('io.readFile:' + file);
      return TypeScript.Environment.readFile(file, codepage);
    },     
    
    writeFile: function (path, contents, writeByteOrderMark) {
      log.debug('io.writeFile:' + path);
      TypeScript.Environment.writeFile(path, contents, writeByteOrderMark);
      files.push(path);
    },
    
    deleteFile: function (path) {
      log.debug('io.deleteFile:' + path);
      try  {
          _fs.unlinkSync(path);
      } catch (e) {
          IOUtils.throwIOError(TypeScript.getDiagnosticMessage(TypeScript.DiagnosticCode.Could_not_delete_file_0, [path]), e);
      }
    },      
    
    fileExists: function (path) {
      log.debug('io.fileExists:' + path);
      return _fs.existsSync(path);
    },
    
    dir: function dir(path, spec, options) {
      log.debug('io.dir:' + path);
      options = options || {};

      function filesInFolder(folder) {
          var paths = [];

          try  {
              var files = _fs.readdirSync(folder);
              for (var i = 0; i < files.length; i++) {
                  var stat = _fs.statSync(folder + "/" + files[i]);
                  if (options.recursive && stat.isDirectory()) {
                      paths = paths.concat(filesInFolder(folder + "/" + files[i]));
                  } else if (stat.isFile() && (!spec || files[i].match(spec))) {
                      paths.push(folder + "/" + files[i]);
                  }
              }
          } catch (err) {
          }

          return paths;
      }
      return filesInFolder(path);
    },        
    
    createDirectory: function (path) {
      log.debug('io.createDirectory:' + path);
      try  {
          if (!this.directoryExists(path)) {
              _fs.mkdirSync(path);
          }
      } catch (e) {
          IOUtils.throwIOError(TypeScript.getDiagnosticMessage(TypeScript.DiagnosticCode.Could_not_create_directory_0, [path]), e);
      }
    },
    
    directoryExists: function (path) {
      log.debug('io.directoryExits:' + path);
      return _fs.existsSync(path) && _fs.statSync(path).isDirectory();
    },
    
    resolvePath: function (path) {
      //log.debug('io.resolvePath:' + path);
      return _path.resolve(baseDir, path);
    },

    dirName: function (path) {
      // log.debug('io.dirName:' + path);
      if (path.indexOf('bridge.js') != -1) {
        return _path.dirname(path) + '/ts_' + args.version;
      }
      var dirPath = _path.dirname(path);

      if (dirPath === path) {
          dirPath = null;
      }

      return dirPath;
    },
    
    findFile: function (rootPath, partialFilePath) {
      log.debug('io.findFile:' + rootPath);
      var path = rootPath + "/" + partialFilePath;

      while (true) {
          if (_fs.existsSync(path)) {
              return { fileInformation: this.readFile(path), path: path };
          } else {
              var parentPath = _path.resolve(rootPath, "..");

              if (rootPath === parentPath) {
                  return null;
              } else {
                  rootPath = parentPath;
                  path = _path.resolve(rootPath, partialFilePath);
              }
          }
      }
    },      
    
    print: function (str) {
      log.debug('io.print:' + str);
      //process.stdout.write(str);
    },  
    
    printLine: function (str) {
      log.debug('io.printLine:' + str);
//      process.stdout.write(str + '\n');
    },   
    
    arguments: process.argv.slice(2),

    stderr: {
      Write: function (str) {
        log.debug('io.stderr.Write:' + str);
        //process.stderr.write(str);
      },
      WriteLine: function (str) {
        log.debug('io.stderr.WriteLine:' + str);
        //process.stderr.write(str + '\n');
      },
      Close: function () {
      }
    },
    stdout: {
        Write: function (str) {
            //process.stdout.write(str);
        },
        WriteLine: function (str) {
            //process.stdout.write(str + '\n');
        },
        Close: function () {
        }
    },

    watchFile: function (fileName, callback) {
    },

    run: function (source, fileName) {
      log.debug('io.run:' + fileName);
      require.main.fileName = fileName;
      require.main.paths = _module._nodeModulePaths(_path.dirname(_fs.realpathSync(fileName)));
      require.main._compile(source, fileName);
    },
    
    getExecutingFilePath: function () {
      //log.debug('io.getExecutingFilePath');
      return process.mainModule.filename;
    },
    
    quit: function (code) { 
      //log.debug('io.quit:' + code);
    }
  };
};