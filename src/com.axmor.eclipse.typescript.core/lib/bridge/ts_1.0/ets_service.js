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

var ts = new Services.LanguageService(tsh);

exports.setFileContent = function(file, params) {
  tsh.setFileContent(file, params);
  ts.currentFileSyntaxTree = null;
  if (ts.compiler.compiler != null) {
    ts.compiler.compiler.updateFile(file, tsh.getScriptSnapshot(file), /*version:*/ 1, /*isOpen:*/ true, /* textChangeRange: */ null);
  }
}

exports.addFile = function(file) {
  tsh.addFile(file);
}

exports.getScriptLexicalStructure = function(file) {
  return ts.getScriptLexicalStructure(file);
}

exports.getCompletionsAtPosition = function(file, params, member) {
  return ts.getCompletionsAtPosition(file, params, member);
}

exports.getCompletionEntryDetails = function(file, position, entryName) {
  return ts.getCompletionEntryDetails(file, position, entryName);
}

exports.getSignatureAtPosition = function(file, params) {
  return ts.getSignatureAtPosition(file, params);
}

exports.getDefinitionAtPosition = function(file, params) {
  return ts.getDefinitionAtPosition(file, params);
}

exports.getFormattingEditsForDocument = function(file, start, end, options) {
  return ts.getFormattingEditsForDocument(file, start, end, options);
}

exports.getReferencesAtPosition = function(file, params) {
  return ts.getReferencesAtPosition(file, params);        
}