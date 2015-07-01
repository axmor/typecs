/// <reference path="../node/node.d.ts"/>


import args = require('./args');
var level = args.logLevel === 'error' ? 2 : (args.logLevel === 'info' ? 1 : 0);

export var error:(message?: any, ...optionalParams: any[]) => void = console.error;
export var info:(message?: any, ...optionalParams: any[]) => void  = level > 0 ? console.log : empty;
export var debug:(message?: any, ...optionalParams: any[]) => void = level == 0 ? console.log : empty;

function empty(s) {} 
