// init type script compiler script
var fs = require('fs');
var log = require('../log.js');
var args = require('../args.js');
var ts = require('./typescriptServices.js');

var tsh = require('./ets_host.js');
var TypeScript = ts;
var Services = ts;

tsh.init(TypeScript);
tsh.setBaseSourceDirectory(args.src);

var ts = Services.createLanguageService(tsh, Services.createDocumentRegistry());
log.debug(ts);

exports.setFileContent = function(file, params) {
  tsh.setFileContent(file, params);
}

exports.addFile = function(file, params) {
  tsh.addFile(file, params);
}

exports.getScriptFileNames = function() {
  return tsh.getScriptFileNames();
}

exports.getScriptLexicalStructure = function(file) {
  return ts.getNavigationBarItems(file);
}

exports.getCompletionsAtPosition = function(file, params, member) {
  return ts.getCompletionsAtPosition(file, params, member);
}

exports.getCompletionEntryDetails = function(file, position, entryName) {
  return ts.getCompletionEntryDetails(file, position, entryName);
}

exports.getSignatureAtPosition = function(file, params) {
//  return ts.getSignatureHelpItems(file, params);
  return ts.getQuickInfoAtPosition(file, params);
}

exports.getDefinitionAtPosition = function(file, params) {
  return ts.getDefinitionAtPosition(file, params);
}

exports.getFormattingEditsForDocument = function(file, start, end, options) {
  return ts.getFormattingEditsForRange(file, start, end, options);
}

exports.getReferencesAtPosition = function(file, params) {
  return ts.getReferencesAtPosition(file, params);        
}

exports.getOccurrencesAtPosition = function (file, position) {
  return ts.getOccurrencesAtPosition(file, position); 
}

exports.getSemanticDiagnostics = function (file) {
  return ts.getSemanticDiagnostics(file);
}

exports.getSyntaxTree = function (file) {
  var t = ts.getSyntaxTree(file);
  return {statements: t.statements, imports: t.referencedFiles};
}

exports.getReferences = function (file) {
  return ts.getSourceFile(file).referencedFiles;
}

exports.getSignatureHelpItems = function(file, params) {
  return ts.getSignatureHelpItems(file, params);
}

exports.getIdentifiers = function (file) {  
  var nodes = [];  
  var sourceFile = ts.getSourceFile(file);
  var getNodes = function (node) {
	  TypeScript.forEachChild(node, function(child) {
		  if (child.kind == 63) {
			  var nodePos = TypeScript.getTokenPosOfNode(child),
			  	  nodeType = ts.getQuickInfoAtPosition(file, nodePos);
			  var node = {
					  text: child.text,
					  length: child.end - nodePos,
					  offset: nodePos,
					  type: nodeType ? nodeType.kind : ""
			  };		      
			  nodes.push(node);
		  }		  
		  getNodes(child);
	  });
  }; 
	  
  getNodes(sourceFile);
  return nodes;
}

exports.getVersion = function () {
  return '1.4.1.0';
}
