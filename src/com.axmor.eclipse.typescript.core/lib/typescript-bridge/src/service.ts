/// <reference path="../ts/typescript.d.ts" />
/// <reference path="../ts/typescriptServices.d.ts" />
/// <reference path="../node/node.d.ts"/>
/// <reference path="./host.ts" />
/// <reference path="./log.ts" />
 

var _ts: typeof ts = require('../ts/typescriptServices.js')

import log = require('./log')
import host = require('./host')
import args = require('./args')
import path = require('./path-util')

export class TSService {
    host: host.BridgeServiceHost
    langService: ts.LanguageService

    constructor() {
        log.debug('service.init')
        this.host = new host.BridgeServiceHost(args.src);
        this.langService = _ts.createLanguageService(this.host, _ts.createDocumentRegistry());
    }

    public setFileContent(fileName: string, content: string): void {
        log.debug('service.setFileContent: %s', fileName)
        this.host.setFileContent(fileName, content)
    }

    public addFile(fileName: string, path: string) {
        log.debug('service.addFile: %s: %s', fileName, path)
        this.host.addFile(fileName, path)
    }

    public getScriptFileNames(): string[] {
        log.debug('service.getScriptFileNames')
        return this.host.getScriptFileNames()
    }

    public getScriptLexicalStructure(fileName: string): ts.NavigationBarItem[] {
        log.debug('service.getScriptLexicalStructure: %s', fileName)
        return this.langService.getNavigationBarItems(fileName)
    }

    public getCompletionsAtPosition(fileName: string, position: number): ts.CompletionInfo {
        log.debug('service.getCompletionsAtPosition: %s: %d', fileName, position)
        return this.langService.getCompletionsAtPosition(fileName, position)
    }

    public getCompletionEntryDetails(fileName: string, position: number, entryName: string): ts.CompletionEntryDetails {
        log.debug('service.getCompletionEntryDetails: %s: %d: %s', fileName, position, entryName)
        return this.langService.getCompletionEntryDetails(fileName, position, entryName)
    }

    public getSignatureAtPosition(fileName: string, position: number): ts.QuickInfo {
        log.debug('service.getSignatureAtPosition: %s: %d', fileName, position)
        //  return ts.getSignatureHelpItems(file, params);
        return this.langService.getQuickInfoAtPosition(fileName, position)
    }

    public getDefinitionAtPosition(fileName: string, position: number): ts.DefinitionInfo[] {
        log.debug('service.getDefinitionAtPosition: %s: %d', fileName, position)
        return this.langService.getDefinitionAtPosition(fileName, position)
    }

    public getFormattingEditsForDocument(fileName: string, start: number, end: number, options: ts.FormatCodeOptions): ts.TextChange[] {
        log.debug('service.getFormattingEditsForDocument: %s: [%d, %d] - %j', fileName, start, end, options)
        return this.langService.getFormattingEditsForRange(fileName, start, end, options)
    }

    public getReferencesAtPosition(fileName: string, position: number): ts.ReferenceEntry[] {
        log.debug('service.getFormattingEditsForDocument: %s: %d', fileName, position)
        return this.langService.getReferencesAtPosition(fileName, position)
    }

    public getOccurrencesAtPosition(fileName: string, position: number): ts.ReferenceEntry[] {
        log.debug('service.getOccurrencesAtPosition: %s: %d', fileName, position)
        return this.langService.getOccurrencesAtPosition(fileName, position)
    }

    public getSemanticDiagnostics(fileName: string): ts.Diagnostic[] {
        log.debug('service.getSemanticDiagnostics: %s', fileName)
        return this.langService.getSemanticDiagnostics(fileName)
    }

    public getSyntaxTree(fileName: string) {
        log.error('service.getSyntaxTree: %s', fileName)
        //var t = this.langService.getSyntaxTree(file);
        //return {statements: t.statements, imports: t.referencedFiles};
    }

    public getReferences(fileName: string): ts.FileReference[] {
        log.debug('service.getReferences: %s', fileName)
        return this.langService.getSourceFile(fileName).referencedFiles
    }

    public getSignatureHelpItems(fileName: string, position: number): ts.SignatureHelpItems {
        log.debug('service.getSignatureHelpItems: %s: %d', fileName, position)
        return this.langService.getSignatureHelpItems(fileName, position)
    }

    public getIdentifiers(fileName: string): any[] {
        log.debug('service.getIdentifiers: %s', fileName)
        var nodes: any[] = []
        var tNodes: ts.Node[] = []
        var sourceFile: ts.SourceFile = this.langService.getSourceFile(fileName)
        var that = this
        var getNodes = function(_node: ts.Node) {
            _ts.forEachChild(_node, (child: ts.Node) => {
                if (child.kind === _ts.SyntaxKind.Identifier) {
                    tNodes.push(child)
                }
                getNodes(child)
            },
                (childs: ts.Node[]) => {
                    for (var i = 0; i < childs.length; i++) {
                        if (childs[i].kind === _ts.SyntaxKind.Identifier) {
                            tNodes.push(childs[i])
                        }
                        getNodes(childs[i])
                    }
                })
        }
        getNodes(sourceFile)

        for (var i = 0; i < tNodes.length; i++) {
            var child = tNodes[i]
            //console.log("!" + child.getText(sourceFile) + ": " + child.kind)
            try {
                var nodeType = that.langService.getQuickInfoAtPosition(fileName, child.pos + 1)
                if (nodeType) {
                    nodes.push({
                        text: child.getText(sourceFile),
                        length: child.end - child.pos,
                        offset: child.pos,
                        type: nodeType ? nodeType.kind : ""
                    });
                }
            } catch (e) {
                log.error(e)
            }
        }

        return nodes;
    }


    public getVersion() {
        log.debug('service.getVersion - %s', _ts.version)
        return _ts.version;
    }

    public compile(file: string, _settings: any): any {
        log.debug('service.compile - %s: %j', file, _settings)
        var compilerHost
        var files
        var settings
        var _errors: any = []

        if (_settings == undefined) {
            let result = _ts.readConfigFile(file, function(path) {
                return _ts.sys.readFile(path);
            });
            var baseDir = path.getDirectoryPath(file)
            let configParseResult = _ts.parseJsonConfigFileContent(result.config, _ts.sys, baseDir)
            if (configParseResult.errors.length > 0) {
                this.reportError(_errors, configParseResult.errors, file)
                return {
                    errors: _errors
                }
            }
            files = configParseResult.fileNames
            settings = configParseResult.options
            compilerHost = _ts.createCompilerHost(settings)
        } else {
            files = [file];
            settings = this.getCompilationSettings(args.src, _settings)
            compilerHost = _ts.createCompilerHost(settings)
            compilerHost.getCurrentDirectory = function() {
                return args.src
            }

            compilerHost.getDefaultLibFileName = function(s) {
                return __dirname + "/../ts/lib.d.ts"
            }
        }

        var program: ts.Program = _ts.createProgram(files, settings, compilerHost);
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
        }
    }

    private reportError(errors: any[], diags: ts.Diagnostic[], _file?: string) {
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
    }

    private getCompilationSettings(basedir: string, settings: any): ts.CompilerOptions {
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
        }
    }
}
