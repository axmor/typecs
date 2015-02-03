// init type script compiler script
var fs = require('fs');
var log = require('../log.js');
var args = require('../args.js');
var ts = require('./typescriptServices.js');

var tsh = require('./ets_host.js');
var TypeScript = ts.TypeScript;
var Services = ts.Services;

tsh.init(TypeScript);
tsh.setBaseSourceDirectory(args.src);

var ts = Services.createLanguageService(tsh, Services.createDocumentRegistry());
log.debug(ts);

exports.setFileContent = function(file, params) {
  tsh.setFileContent(file, params);
}

exports.addFile = function(file) {
  tsh.addFile(file);
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
  return ts.getSignatureHelpItems(file, params);
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
