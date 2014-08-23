#!/usr/bin/env node

// command line arguments
var args = require('./args.js');
// logging
var log = require('./log.js');

// activity indicator
var activity = false;

log.debug(args);

// init service
var tsc = require('./ts_' + args.version + '/ets_io.js');
var tss = require('./ts_' + args.version + '/ets_service.js');

//tss.getScriptLexicalStructure('test_ts1.ts');
//tss.setFileContent('test_ts1.ts', 'asdasd');
//tss.getScriptLexicalStructure('test_ts1.ts');
//tss.getScriptLexicalStructure('test_ts2.ts');

if (args.serv) {
  var net = require('net');

  server = net.createServer({allowHalfOpen : true}, function (socket) {
    var data = '';
    socket.allowHalfOpen = true; 

    socket.on('data', function (d) {
      activity = true;
      data += d.toString();
    });        
    
    socket.on('end', function() {
      try {      
        var o = JSON.parse(data);   
        switch(o.method)
        {
          case 'exit':
            socket.end(JSON.stringify({'status': 0}));
            process.exit();
            break;
          case 'setFileContent':                            
            log.debug('bridge.setFileContent: ' + o.file);
            tss.setFileContent(o.file, o.params);
            socket.end(JSON.stringify({ 'status': 0}));
            break;
          case 'addFile':
            if (o.file !== 'std-lib/lib.d.ts') {                            
              log.debug('bridge.addFile: ' + o.file);
              tss.addFile(o.file);
            }
            socket.end(JSON.stringify({ 'status': 0}));
            break;
          case 'getScriptLexicalStructure':                            
            log.debug('bridge.getScriptLexicalStructure: ' + o.file);
            socket.end(JSON.stringify({ 'model' : tss.getScriptLexicalStructure(o.file)}));
            break;    
          case 'getCompletions':                            
            log.debug('bridge.getCompletions: ' + o.file + ', pos: ' + o.params);
            socket.end(JSON.stringify(tss.getCompletionsAtPosition(o.file, o.params, true)));
            break;    
          case 'getCompletionDetails':                            
            log.debug('bridge.getCompletionDetails: ' + o.file + ', param: ' + o.params.position + ' ' + o.params.entryName);
            socket.end(JSON.stringify(tss.getCompletionEntryDetails(o.file, o.params.position, o.params.entryName)));
            break;    
          case 'getSignature':                            
            log.debug('bridge.getSignature: ' + o.file + ', pos: ' + o.params);
            socket.end(JSON.stringify(tss.getSignatureAtPosition(o.file, o.params)));
            break;    
          case 'getTypeDefinition':                            
            log.debug('bridge.getTypeDefinition: ' + o.file + ', param: ' + o.params);
            socket.end(JSON.stringify({ 'model' : tss.getDefinitionAtPosition(o.file, o.params) }));
            break;    
          case 'getFormattingCode':                            
            log.debug('bridge.getFormattingCode: ' + o.file + ', param: start-' + o.params.start + ', end-' + o.params.end + ', settings - ' + o.params.settings);
            socket.end(JSON.stringify({ 'model' : tss.getFormattingEditsForDocument(o.file, o.params.start, o.params.end, o.params.settings) }));
            break;    
          case 'getReferencesAtPosition':                            
            log.debug('bridge.getReferencesAtPosition: ' + o.file + ', param: ' + o.params);
            socket.end(JSON.stringify({ 'model' : tss.getReferencesAtPosition(o.file, o.params) }));
            break;    
          case 'getOccurrencesAtPosition':
            log.debug('bridge.getOccurrencesAtPosition: ' + o.file + ', param: ' + o.params);
            socket.end(JSON.stringify({ 'model' : tss.getOccurrencesAtPosition(o.file, o.params) }));
            break;    
          case 'compile':                            
            log.debug('bridge.compile: ' + o.file + ', param: ' + o.params);
            socket.end(JSON.stringify(tsc.compile(o.file, o.params)));
            break;    
          default:
            socket.end(JSON.stringify({ 'version' : args.version }));
            break;
        }
      } catch (e) {
        log.error('Command error', e.stack);
        socket.end(JSON.stringify({"error": e.message}));
      } 
    });
  });           
      
  server.listen(function() {
    address = server.address();
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
}