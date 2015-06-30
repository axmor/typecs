/// <reference path="../node/node.d.ts"/>
var args = require('./args');
var level = args.logLevel === 'error' ? 2 : (args.logLevel === 'info' ? 1 : 0);
var level = 0;
exports.error = console.error;
exports.info = level > 0 ? console.log : empty;
exports.debug = level == 0 ? console.log : empty;
function empty(s) { }
