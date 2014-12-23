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

//tss.getScriptLexicalStructure('module1.ts');
//tss.setFileContent('module1.ts', 'class TestM {}');
//tss.getScriptLexicalStructure('module1.ts');
//tss.getScriptLexicalStructure('test_ts2.ts');
//tss.addFile('module1.ts');
//log.debug(tss.getCompletionsAtPosition('module2.ts', 100));
//log.debug(tss.getDefinitionAtPosition('module2.ts', 100));
//log.info(tss.getFormattingEditsForDocument('module2.ts', 0, 500, { "ConvertTabsToSpaces":true, "IndentSize":4, "InsertSpaceAfterCommaDelimiter":true, "InsertSpaceAfterFunctionKeywordForAnonymousFunctions":false, "InsertSpaceAfterKeywordsInControlFlowStatements":true, "InsertSpaceAfterOpeningAndBeforeClosingNonemptyParenthesis":false, "InsertSpaceAfterSemicolonInForStatements":true, "InsertSpaceBeforeAndAfterBinaryOperators":true, "NewLineCharacter":"\r\n", "PlaceOpenBraceOnNewLineForControlBlocks":false,"PlaceOpenBraceOnNewLineForFunctions":false,"TabSize":4}));
//tss.getScriptLexicalStructure('module2.ts');
//tss.setFileContent('module2.ts', 'class 1TestM {}');
//log.debug(tss.getSignatureAtPosition('module2.ts', 107));
/*
log.debug(JSON.stringify({ 'model' : tss.getSyntaxTree('module2.ts') }, 
              	function(key, value) {
              		//log.debug(key);
              		//log.debug(value);
  					//if (key == '_sourceUnit') return null;
  					//if (key == 'syntaxTree') return null;
  					if (key == 'members') {
  						return undefined;
  					}
  					if (value && (key == 'parent' || key == 'name')) {
						if (key == 'name' && value) {
							return {text: value.text ? value.text : "", filename: value.filename};
						}
						if (key == 'parent' && value) {
							if (value.name) {
	  							return {name: value.name.text, filename: value.filename};
	  						} else {
	  							return {filename: value.filename};
	  						}
						}
						return value;
  					}
  					return value;
				})
);
*/
/*
log.error(tsc.compile('module2.ts', 
    {
      "allowAutomaticSemicolonInsertion":true,
      "allowBool":false,
      "allowModuleKeywordInExternalModuleReference":false,
      "codeGenTarget":1,
      "gatherDiagnostics":false,
      "generateDeclarationFiles":false,
      "mapRoot":"",
      "mapSourceFiles":true,
      "moduleGenTarget":0,
      "noImplicitAny":false,
      "noLib":false,
      "noResolve":false,
      "outDirOption":"target",
      "outFileOption":"",
      "propagateEnumConstants":false,
      "removeComments":false,
      "sourceRoot":"",
      "updateTC":false,
      "useCaseSensitiveFileResolution":false,
      "watch":false}));
*/
/*********************************************************************
Service functions
  cleanupSemanticCache: [Function: cleanupSemanticCache],
  getSyntacticDiagnostics: [Function: getSyntacticDiagnostics],
  getSemanticDiagnostics: [Function: getSemanticDiagnostics],
  getCompilerOptionsDiagnostics: [Function: getCompilerOptionsDiagnostics],
  getCompletionsAtPosition: [Function: getCompletionsAtPosition],
  getCompletionEntryDetails: [Function: getCompletionEntryDetails],
  getTypeAtPosition: [Function: getTypeAtPosition],
  getSignatureHelpItems: [Function],
  getSignatureHelpCurrentArgumentState: [Function],
  getDefinitionAtPosition: [Function: getDefinitionAtPosition],
  getReferencesAtPosition: [Function: getReferencesAtPosition],
  getOccurrencesAtPosition: [Function: getOccurrencesAtPosition],
  getImplementorsAtPosition: [Function],
  getNameOrDottedNameSpan: [Function: getNameOrDottedNameSpan],
  getBreakpointStatementAtPosition: [Function: getBreakpointStatementAtPosition],
  getNavigateToItems: [Function],
  getRenameInfo: [Function],
  getNavigationBarItems: [Function: getNavigationBarItems],
  getOutliningSpans: [Function: getOutliningSpans],
  getTodoComments: [Function: getTodoComments],
  getBraceMatchingAtPosition: [Function: getBraceMatchingAtPosition],
  getIndentationAtPosition: [Function: getIndentationAtPosition],
  getFormattingEditsForRange: [Function: getFormattingEditsForRange],
  getFormattingEditsForDocument: [Function: getFormattingEditsForDocument],
  getFormattingEditsAfterKeystroke: [Function: getFormattingEditsAfterKeystroke],
  getEmitOutput: [Function] }
************************************************************************/
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
            socket.end(JSON.stringify({ 'model' : tss.getSignatureAtPosition(o.file, o.params)}));
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
          case 'getSemanticDiagnostics':
              log.debug('bridge.getSemanticDiagnostics: ' + o.file);
              socket.end(JSON.stringify({ 'model' : tss.getSemanticDiagnostics(o.file) }, 
              	function(key, value) {
  					if (key == 'file') return value.filename;
  					return value;
				}
			  ));
              break;
          case 'getSyntaxTree':
              log.debug('bridge.getSyntaxTree: ' + o.file);
              socket.end(JSON.stringify({ 'model' : tss.getSyntaxTree(o.file) }, 
              	function(key, value) {
  					if (key == 'members') {
  						return undefined;
  					}
  					if (value && (key == 'parent' || key == 'name')) {
						if (key == 'name' && value) {
							return {text: value.text ? value.text : "", filename: value.filename};
						}
						if (key == 'parent' && value) {
							if (value.name) {
	  							return {name: value.name.text, filename: value.filename};
	  						} else {
	  							return {filename: value.filename};
	  						}
						}
						return value;
  					}
  					return value;
				})
			  );
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