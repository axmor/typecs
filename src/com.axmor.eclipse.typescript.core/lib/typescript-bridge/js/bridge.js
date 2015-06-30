/// <reference path="../node/node.d.ts"/>
/// <reference path="./log.ts" />
/// <reference path="./service.ts" />
var log = require('./log');
// activity indicator
var activity = false;
var service = require('./service');
var net = require('net');
var tsService = new service.TSService();
var server = net.createServer({ allowHalfOpen: true }, function (socket) {
    var data = '';
    socket.allowHalfOpen = true;
    socket.on('data', function (d) {
        activity = true;
        data += d.toString();
    });
    socket.on('end', function () {
        try {
            var o = JSON.parse(data);
            switch (o.method) {
                case 'exit':
                    socket.end(JSON.stringify({ 'status': 0 }));
                    process.exit();
                    break;
                case 'setFileContent':
                    log.debug('bridge.setFileContent: ' + o.file);
                    tsService.setFileContent(o.file, o.params);
                    socket.end(JSON.stringify({ 'status': 0 }));
                    break;
                case 'addFile':
                    if (o.file !== 'std-lib/lib.d.ts') {
                        log.debug('bridge.addFile: ' + o.file);
                        tsService.addFile(o.file, o.params);
                    }
                    socket.end(JSON.stringify({ 'status': 0 }));
                    break;
                case 'getScriptFileNames':
                    log.debug('bridge.getScriptFileNames ');
                    socket.end(JSON.stringify({ 'names': tsService.getScriptFileNames() }));
                    break;
                case 'getScriptLexicalStructure':
                    log.debug('bridge.getScriptLexicalStructure: ' + o.file);
                    socket.end(JSON.stringify({ 'model': tsService.getScriptLexicalStructure(o.file) }));
                    break;
                case 'getCompletions':
                    log.debug('bridge.getCompletions: ' + o.file + ', pos: ' + o.params);
                    socket.end(JSON.stringify(tsService.getCompletionsAtPosition(o.file, o.params)));
                    break;
                case 'getCompletionDetails':
                    log.debug('bridge.getCompletionDetails: ' + o.file + ', param: ' + o.params.position + ' ' + o.params.entryName);
                    socket.end(JSON.stringify(tsService.getCompletionEntryDetails(o.file, o.params.position, o.params.entryName)));
                    break;
                case 'getSignature':
                    log.debug('bridge.getSignature: ' + o.file + ', pos: ' + o.params);
                    socket.end(JSON.stringify({ 'model': tsService.getSignatureAtPosition(o.file, o.params) }));
                    break;
                case 'getSignatureHelpItems':
                    log.debug('bridge.getSignatureHelpItems: ' + o.file + ', pos: ' + o.params);
                    socket.end(JSON.stringify({ 'model': tsService.getSignatureHelpItems(o.file, o.params) }));
                    break;
                case 'getTypeDefinition':
                    log.debug('bridge.getTypeDefinition: ' + o.file + ', param: ' + o.params);
                    socket.end(JSON.stringify({ 'model': tsService.getDefinitionAtPosition(o.file, o.params) }));
                    break;
                case 'getFormattingCode':
                    log.debug('bridge.getFormattingCode: ' + o.file + ', param: start-' + o.params.start + ', end-' + o.params.end + ', settings - ' + o.params.settings);
                    socket.end(JSON.stringify({ 'model': tsService.getFormattingEditsForDocument(o.file, o.params.start, o.params.end, o.params.settings) }));
                    break;
                case 'getReferencesAtPosition':
                    log.debug('bridge.getReferencesAtPosition: ' + o.file + ', param: ' + o.params);
                    socket.end(JSON.stringify({ 'model': tsService.getReferencesAtPosition(o.file, o.params) }));
                    break;
                case 'getOccurrencesAtPosition':
                    log.debug('bridge.getOccurrencesAtPosition: ' + o.file + ', param: ' + o.params);
                    socket.end(JSON.stringify({ 'model': tsService.getOccurrencesAtPosition(o.file, o.params) }));
                    break;
                case 'compile':
                    log.debug('bridge.compile: ' + o.file + ', param: ' + o.params);
                    socket.end(JSON.stringify(tsService.compile(o.file, o.params)));
                    break;
                case 'getSemanticDiagnostics':
                    log.debug('bridge.getSemanticDiagnostics: ' + o.file);
                    socket.end(JSON.stringify({ 'model': tsService.getSemanticDiagnostics(o.file) }, function (key, value) {
                        if (key == 'file')
                            return value.filename;
                        return value;
                    }));
                    break;
                case 'getSyntaxTree':
                    log.debug('bridge.getSyntaxTree: ' + o.file);
                    socket.end(JSON.stringify({ 'model': tsService.getSyntaxTree(o.file) }, function (key, value) {
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
                                }
                                else {
                                    return { filename: value.filename };
                                }
                            }
                            return value;
                        }
                        return value;
                    }));
                    break;
                case 'getReferences':
                    log.debug('bridge.getReferences: ' + o.file);
                    socket.end(JSON.stringify({ 'model': tsService.getReferences(o.file) }));
                    break;
                case 'getIdentifiers':
                    log.debug('bridge.getIdentifiers: ' + o.file);
                    socket.end(JSON.stringify({ 'model': tsService.getIdentifiers(o.file) }));
                    break;
                default:
                    socket.end(JSON.stringify({ 'version': tsService.getVersion() }));
                    break;
            }
        }
        catch (e) {
            log.error('Command error', e.stack);
            socket.end(JSON.stringify({ "error": e.message }));
        }
    });
});
server.listen(function () {
    var address = server.address();
    console.error("@%s@", address.port);
});
server.on('error', function (e) {
    if (e.code == 'EADDRINUSE') {
        log.error('Address in use, close server.');
        setTimeout(function () {
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
