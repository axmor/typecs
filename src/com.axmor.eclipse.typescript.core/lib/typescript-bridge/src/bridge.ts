/// <reference path="../node/node.d.ts"/>
/// <reference path="./log.ts" />
/// <reference path="./service.ts" />

import log = require('./log')
import host = require('./host')

// activity indicator
var activity = false
import service = require('./service')

import net = require('net')
var tsService: service.TSService = new service.TSService()

var server: net.Server = net.createServer({ allowHalfOpen: true }, (socket: any) => {
    var data = '';

    socket.allowHalfOpen = true;

    socket.on('data', (d: any) => {
        activity = true;
        data += d.toString();
    });

    socket.on('end', () => {
        try {
            var o = JSON.parse(data);
            switch (o.method) {
                case 'exit':
                    socket.end(JSON.stringify({ 'status': 0 }));
                    process.exit();
                    break;
                case 'setFileContent':
                    tsService.setFileContent(o.file, o.params);
                    socket.end(JSON.stringify({ 'status': 0 }));
                    break;
                case 'addFile':
                    if (o.file !== 'std-lib/lib.d.ts') {
                        tsService.addFile(o.file, o.params);
                    }
                    socket.end(JSON.stringify({ 'status': 0 }));
                    break;
                case 'getScriptFileNames':
                    socket.end(JSON.stringify({ 'names': tsService.getScriptFileNames() }));
                    break;
                case 'getScriptLexicalStructure':
                    socket.end(JSON.stringify({ 'model': tsService.getScriptLexicalStructure(o.file) }));
                    break;
                case 'getCompletions':
                    socket.end(JSON.stringify(tsService.getCompletionsAtPosition(o.file, o.params)));
                    break;
                case 'getCompletionDetails':
                    socket.end(JSON.stringify(tsService.getCompletionEntryDetails(o.file, o.params.position, o.params.entryName)));
                    break;
                case 'getSignature':
                    socket.end(JSON.stringify({ 'model': tsService.getSignatureAtPosition(o.file, o.params) }));
                    break;
                case 'getSignatureHelpItems':
                    log.debug('bridge.getSignatureHelpItems: ' + o.file + ', pos: ' + o.params);
                    socket.end(JSON.stringify({ 'model': tsService.getSignatureHelpItems(o.file, o.params) }));
                    break;
                case 'getTypeDefinition':
                    socket.end(JSON.stringify({ 'model': tsService.getDefinitionAtPosition(o.file, o.params) }));
                    break;
                case 'getFormattingCode':
                    socket.end(JSON.stringify({ 'model': tsService.getFormattingEditsForDocument(o.file, o.params.start, o.params.end, o.params.settings) }));
                    break;
                case 'getReferencesAtPosition':
                    socket.end(JSON.stringify({ 'model': tsService.getReferencesAtPosition(o.file, o.params) }));
                    break;
                case 'getOccurrencesAtPosition':
                    socket.end(JSON.stringify({ 'model': tsService.getOccurrencesAtPosition(o.file, o.params) }));
                    break;
                case 'compile':
                    socket.end(JSON.stringify(tsService.compile(o.file, o.params)));
                    break;
                case 'getSemanticDiagnostics':
                    socket.end(JSON.stringify({ 'model': tsService.getSemanticDiagnostics(o.file) },
                        function(key, value) {
                            if (key == 'file') return value.filename;
                            return value;
                        }
                        ));
                    break;
                case 'getSyntaxTree':
                    socket.end(JSON.stringify({ 'model': tsService.getSyntaxTree(o.file) },
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
                    socket.end(JSON.stringify({ 'model': tsService.getReferences(o.file) }));
                    break;
                case 'getIdentifiers':
                    socket.end(JSON.stringify({ 'model': tsService.getIdentifiers(o.file) }));
                    break;
                default:
                    socket.end(JSON.stringify({ 'version': tsService.getVersion() }));
                    break;
            }
        } catch (e) {
            log.error('Command error', e.stack);
            socket.end(JSON.stringify({ "error": e.message }));
        }
    });
});

server.listen(() => {
    var address = server.address();
    console.error("@%s@", address.port);
});

server.on('error', (e) => {
    if (e.code == 'EADDRINUSE') {
        log.error('Address in use, close server.');
        setTimeout(function() {
            server.close();
        }, 1000);
    }
});

setInterval(inactivityCheck, 1000 * 60 * 10); // 10 munites check inactivity
  
function inactivityCheck() {
    log.debug('inactivityCheck: ' + activity);
    if (!activity) {
        log.info('Exit by inactivity period exceed.');
        process.exit();
    }
    activity = false;
}
