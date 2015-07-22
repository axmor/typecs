/// <reference path="../node/node.d.ts"/>
/// <reference path="./args.ts" />
/// <reference path="./log.ts" />
/// <reference path="./service.ts" />

import log = require('./log')
import host = require('./host')
import args = require('./args')

// activity indicator
var activity = false
import service = require('./service')

var thrift = require('./thrift');
var tsService: service.TSService = new service.TSService()

var TSBridgeService = require('./gen-nodejs/TSBridgeService.js'),
    ttypes = require("./gen-nodejs/bridge_types");

var server = thrift.createServer(TSBridgeService, {
    invoke: function(method: string, file: string, position: number, params: string, result: any): void {
        log.debug("server invoke: %s", method);
        try {
            switch (method) {
                case 'exit':
                    result(null, JSON.stringify({ 'status': 0 }));
                    process.exit();
                    break;
                case 'setFileContent':
                    tsService.setFileContent(file, params);
                    result(null, JSON.stringify({ 'status': 0 }));
                    break;
                case 'addFile':
                    if (file !== 'std-lib/lib.d.ts') {
                        tsService.addFile(file, params);
                    }
                    result(null, JSON.stringify({ 'status': 0 }));
                    break;
                case 'getScriptFileNames':
                    result(null, JSON.stringify({ 'names': tsService.getScriptFileNames() }));
                    break;
                case 'getScriptLexicalStructure':
                    result(null, JSON.stringify({ 'model': tsService.getScriptLexicalStructure(file) }));
                    break;
                case 'getCompletions':
                    result(null, JSON.stringify(tsService.getCompletionsAtPosition(file, position)));
                    break;
                case 'getCompletionDetails':
                    result(null, JSON.stringify(tsService.getCompletionEntryDetails(file, position, params)));
                    break;
                case 'getSignature':
                    result(null, JSON.stringify({ 'model': tsService.getSignatureAtPosition(file, position) }));
                    break;
                case 'getSignatureHelpItems':
                    result(null, JSON.stringify({ 'model': tsService.getSignatureHelpItems(file, position) }));
                    break;
                case 'getTypeDefinition':
                    result(null, JSON.stringify({ 'model': tsService.getDefinitionAtPosition(file, position) }));
                    break;
                case 'getFormattingCode': {
                    let jsonParams = JSON.parse(params)
                    result(null, JSON.stringify({ 'model': tsService.getFormattingEditsForDocument(file, jsonParams.start, jsonParams.end, jsonParams.settings) }));
                    break;
                }
                case 'getReferencesAtPosition':
                    result(null, JSON.stringify({ 'model': tsService.getReferencesAtPosition(file, position) }));
                    break;
                case 'getOccurrencesAtPosition':
                    result(null, JSON.stringify({ 'model': tsService.getOccurrencesAtPosition(file, position) }));
                    break;
                case 'compile':
                    result(null, JSON.stringify(tsService.compile(file, JSON.parse(params))));
                    break;
                case 'getSemanticDiagnostics':
                    result(null, JSON.stringify({ 'model': tsService.getSemanticDiagnostics(file) },
                        function(key, value) {
                            if (key == 'file') return value.filename;
                            return value;
                        }
                        ));
                    break;
                case 'getSyntaxTree':
                    result(null, JSON.stringify({ 'model': tsService.getSyntaxTree(file) },
                        function(key, value) {
                            if (key == 'members') {
                                return undefined;
                            }
                            if (value && (key == 'parent' || key == 'name')) {
                                if (key == 'name' && value) {
                                    return { text: value.text ? value.text : "", filename: value.filename };
                                }
                                if (key == 'parent' && value) {
                                    if (value.name) {
                                        return { name: value.name.text, filename: value.filename };
                                    } else {
                                        return { filename: value.filename };
                                    }
                                }
                                return value;
                            }
                            return value;
                        })
                        );
                    break;
                case 'getReferences':
                    result(null, JSON.stringify({ 'model': tsService.getReferences(file) }));
                    break;
                case 'getIdentifiers':
                    result(null, JSON.stringify({ 'model': tsService.getIdentifiers(file) }));
                    break;
                default:
                    result(null, JSON.stringify({ 'version': tsService.getVersion() }));
                    break;
            }
        } catch (e) {
            log.error('Command error', e.stack);
            result(null, JSON.stringify({ "error": e.message }));
        }
    },
}, {});

server.listen(args.port);