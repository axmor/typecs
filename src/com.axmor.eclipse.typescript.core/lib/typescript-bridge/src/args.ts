/// <reference path="../node/node.d.ts"/>

var args = new Array();
process.argv.splice(2).map(function(s) { var p = s.split('='); args[p[0]] = p[1]; return s;});

export var src = args['src'] ? args['src'] : './';
export var serv = args['serv'] != undefined;
export var logLevel = args['log'] ? args['log'] : 'error';