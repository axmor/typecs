/// <reference path="../ts/typescript.d.ts" />
/// <reference path="../ts/typescriptServices.d.ts" />
/// <reference path="../node/node.d.ts"/>
/// <reference path="./host.ts" />
/// <reference path="./log.ts" />
var _ts = require('../ts/typescriptServices.js');
var log = require('./log');
var host = require('./host');
var args = require('./args');
var path = require('./path-util');
var TSService = (function () {
    function TSService() {
        log.debug('service.init');
        this.host = new host.BridgeServiceHost(args.src);
        this.langService = _ts.createLanguageService(this.host, _ts.createDocumentRegistry());
    }
    TSService.prototype.setFileContent = function (fileName, content) {
        log.debug('service.setFileContent: %s', fileName);
        this.host.setFileContent(fileName, content);
    };
    TSService.prototype.addFile = function (fileName, path) {
        log.debug('service.addFile: %s: %s', fileName, path);
        this.host.addFile(fileName, path);
    };
    TSService.prototype.getScriptFileNames = function () {
        log.debug('service.getScriptFileNames');
        return this.host.getScriptFileNames();
    };
    TSService.prototype.getScriptLexicalStructure = function (fileName) {
        log.debug('service.getScriptLexicalStructure: %s', fileName);
        return this.langService.getNavigationBarItems(fileName);
    };
    TSService.prototype.getCompletionsAtPosition = function (fileName, position) {
        log.debug('service.getCompletionsAtPosition: %s: %d', fileName, position);
        return this.langService.getCompletionsAtPosition(fileName, position);
    };
    TSService.prototype.getCompletionEntryDetails = function (fileName, position, entryName) {
        log.debug('service.getCompletionEntryDetails: %s: %d: %s', fileName, position, entryName);
        return this.langService.getCompletionEntryDetails(fileName, position, entryName);
    };
    TSService.prototype.getSignatureAtPosition = function (fileName, position) {
        log.debug('service.getSignatureAtPosition: %s: %d', fileName, position);
        //  return ts.getSignatureHelpItems(file, params);
        return this.langService.getQuickInfoAtPosition(fileName, position);
    };
    TSService.prototype.getDefinitionAtPosition = function (fileName, position) {
        log.debug('service.getDefinitionAtPosition: %s: %d', fileName, position);
        return this.langService.getDefinitionAtPosition(fileName, position);
    };
    TSService.prototype.getFormattingEditsForDocument = function (fileName, start, end, options) {
        log.debug('service.getFormattingEditsForDocument: %s: [%d, %d] - %j', fileName, start, end, options);
        return this.langService.getFormattingEditsForRange(fileName, start, end, options);
    };
    TSService.prototype.getReferencesAtPosition = function (fileName, position) {
        log.debug('service.getFormattingEditsForDocument: %s: %d', fileName, position);
        return this.langService.getReferencesAtPosition(fileName, position);
    };
    TSService.prototype.getOccurrencesAtPosition = function (fileName, position) {
        log.debug('service.getOccurrencesAtPosition: %s: %d', fileName, position);
        return this.langService.getOccurrencesAtPosition(fileName, position);
    };
    TSService.prototype.getSemanticDiagnostics = function (fileName) {
        log.debug('service.getSemanticDiagnostics: %s', fileName);
        return this.langService.getSemanticDiagnostics(fileName);
    };
    TSService.prototype.getSyntaxTree = function (fileName) {
        log.error('service.getSyntaxTree: %s', fileName);
        //var t = this.langService.getSyntaxTree(file);
        //return {statements: t.statements, imports: t.referencedFiles};
    };
    TSService.prototype.getReferences = function (fileName) {
        log.debug('service.getReferences: %s', fileName);
        return this.langService.getSourceFile(fileName).referencedFiles;
    };
    TSService.prototype.getSignatureHelpItems = function (fileName, position) {
        log.debug('service.getSignatureHelpItems: %s: %d', fileName, position);
        return this.langService.getSignatureHelpItems(fileName, position);
    };
    TSService.prototype.getIdentifiers = function (fileName) {
        log.debug('service.getIdentifiers: %s', fileName);
        var nodes = [];
        var tNodes = [];
        var sourceFile = this.langService.getSourceFile(fileName);
        var that = this;
        var getNodes = function (_node) {
            _ts.forEachChild(_node, function (child) {
                if (child.kind === 65 /* Identifier */) {
                    tNodes.push(child);
                }
                getNodes(child);
            }, function (childs) {
                for (var i = 0; i < childs.length; i++) {
                    if (childs[i].kind === 65 /* Identifier */) {
                        tNodes.push(childs[i]);
                    }
                    getNodes(childs[i]);
                }
            });
        };
        getNodes(sourceFile);
        for (var i = 0; i < tNodes.length; i++) {
            var child = tNodes[i];
            //console.log("!" + child.getText(sourceFile) + ": " + child.kind)
            try {
                var nodeType = that.langService.getQuickInfoAtPosition(fileName, child.pos + 1);
                if (nodeType) {
                    nodes.push({
                        text: child.getText(sourceFile),
                        length: child.end - child.pos,
                        offset: child.pos,
                        type: nodeType ? nodeType.kind : ""
                    });
                }
            }
            catch (e) {
                log.error(e);
            }
        }
        return nodes;
    };
    TSService.prototype.getVersion = function () {
        log.debug('service.getVersion - %s', _ts.version);
        return _ts.version;
    };
    TSService.prototype.compile = function (file, _settings) {
        log.debug('service.compile - %s: %j', file, _settings);
        var compilerHost;
        var files;
        var settings;
        var _errors = [];
        if (_settings == undefined) {
        	var result = _ts.readConfigFile(file, function (path) { 
           		return _ts.sys.readFile(path);
            });
            var baseDir = path.getDirectoryPath(file);
            var configParseResult = _ts.parseJsonConfigFileContent(result.config, _ts.sys, baseDir);
            if (configParseResult.errors.length > 0) {
                this.reportError(_errors, configParseResult.errors, file);
                return {
                    errors: _errors
                };
            }
            files = configParseResult.fileNames;
            settings = configParseResult.options;
            compilerHost = _ts.createCompilerHost(settings);
        }
        else {
            files = [file];
            settings = this.getCompilationSettings(args.src, _settings);
            compilerHost = _ts.createCompilerHost(settings);
            compilerHost.getCurrentDirectory = function () {
                return args.src;
            };
            compilerHost.getDefaultLibFileName = function (s) {
                return __dirname + "/../ts/lib.d.ts";
            };
        }
        var program = _ts.createProgram(files, settings, compilerHost);
        /*
        if (program.getCompilerOptions().outDir && program.getCurrentDirectory()) {
            var commonPathComponents = path.getNormalizedPathComponents(program.getCurrentDirectory(), args.src);
            var outDirPathComponents = path.getNormalizedPathComponents(program.getCompilerOptions().outDir, args.src);
            var resultPathComponents = [];
            log.debug(commonPathComponents)
            log.debug(outDirPathComponents)
            commonPathComponents.pop();
            for (var i = 0; i < Math.min(commonPathComponents.length, outDirPathComponents.length); i++) {
                if (commonPathComponents[i] != outDirPathComponents[i]) {
                    resultPathComponents = outDirPathComponents;
                    break;
                }
                resultPathComponents.push(commonPathComponents[i]);
            }
            program.getCompilerOptions().outDir = path.getNormalizedPathFromPathComponents(resultPathComponents);
        }
        */
        var bindStart = new Date().getTime();
        var diagnostics = program.getSyntacticDiagnostics();
        this.reportError(_errors, diagnostics);
        if (diagnostics.length === 0) {
            var diagnostics = program.getGlobalDiagnostics();
            this.reportError(_errors, diagnostics);
            if (diagnostics.length === 0) {
                var diagnostics = program.getSemanticDiagnostics();
                this.reportError(_errors, diagnostics);
            }
        }
        var emitOutput = program.emit();
        this.reportError(_errors, emitOutput.diagnostics);
        return {
            errors: _errors
        };
    };
    TSService.prototype.reportError = function (errors, diags, _file) {
        for (var i = 0; i < diags.length; i++) {
            var e = diags[i];
            errors.push({
                file: e.file ? e.file.fileName : (_file ? _file : ""),
                text: e.messageText,
                severity: e.category,
                code: e.code,
                start: e.start,
                length: e.length
            });
        }
    };
    TSService.prototype.getCompilationSettings = function (basedir, settings) {
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
            rootDir: s.rootDir,
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
    return TSService;
})();
exports.TSService = TSService;
//# sourceMappingURL=service.js.map