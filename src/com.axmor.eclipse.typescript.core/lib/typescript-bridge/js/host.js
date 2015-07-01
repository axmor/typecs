/// <reference path="../ts/typescriptServices.d.ts" />
/// <reference path="../node/node.d.ts"/>
/// <reference path="./log.ts" />
var log = require('./log');
var fs = require('fs');
var _ts = require('../ts/typescriptServices.js');
var TSFile = (function () {
    function TSFile() {
        this.version = 1;
        this.snapshot = null;
        this.path = '';
    }
    return TSFile;
})();
var BridgeServiceHost = (function () {
    function BridgeServiceHost(baseDir) {
        this.files = {
            'std-lib/lib.d.ts': {
                version: 1,
                snapshot: _ts.ScriptSnapshot.fromString(fs.readFileSync(__dirname + '/../ts/lib.d.ts').toString()),
                path: ''
            }
        };
        this.baseDir = '';
        this.baseDir = baseDir;
        if (fs.statSync(baseDir).isDirectory()) {
            this.readDir('');
        }
    }
    BridgeServiceHost.prototype.getCompilationSettings = function () {
        log.debug('host.getCompilationSettings');
        return {
            target: 1 /* ES5 */,
            module: 0 /* None */
        };
    };
    BridgeServiceHost.prototype.getScriptFileNames = function () {
        log.debug('host.getScriptFileNames');
        return Object.keys(this.files);
    };
    BridgeServiceHost.prototype.getScriptVersion = function (fileName) {
        log.debug('host.getScriptVersion: %s', fileName);
        if (this.files[fileName] && this.files[fileName].version) {
            return this.files[fileName].version.toString();
        }
        return '0';
    };
    BridgeServiceHost.prototype.getScriptSnapshot = function (fileName) {
        log.debug('host.getScriptSnapshot: %s', fileName);
        if (this.files[fileName] == null) {
            this.files[fileName] = new TSFile();
        }
        // we added white space to end of content to avoid code completion error
        if (this.files[fileName].snapshot == null) {
            var content = new Buffer('');
            try {
                content = fs.readFileSync(this.baseDir + '/' + fileName);
            }
            catch (e) {
                if (e.code !== "ENOENT") {
                    throw e;
                }
                else {
                    try {
                        content = fs.readFileSync(this.files[fileName].path);
                    }
                    catch (e) {
                    }
                }
            }
            this.files[fileName].snapshot = _ts.ScriptSnapshot.fromString(content.toString() + ' ');
        }
        return this.files[fileName].snapshot;
    };
    BridgeServiceHost.prototype.getLocalizedDiagnosticMessages = function () {
        log.debug('host.getLocalizedDiagnosticMessages');
        return null;
    };
    //getCancellationToken?(): CancellationToken;
    BridgeServiceHost.prototype.getCurrentDirectory = function () {
        log.debug('host.getCurrentDirectory(): %s', this.baseDir);
        return this.baseDir;
    };
    BridgeServiceHost.prototype.getDefaultLibFileName = function (options) {
        log.debug('host.getDefaultLibFileName(): std-lib/lib.d.ts');
        return 'std-lib/lib.d.ts';
    };
    BridgeServiceHost.prototype.setFileContent = function (file, content) {
        log.debug('host.setFileContent: %s: %s', file, content);
        // we added white space to end of content to avoid code completion error
        var c = content ? (content + ' ') : ' ';
        var snapshot = _ts.ScriptSnapshot.fromString(c);
        var old = null;
        if (this.files[file]) {
            var f = this.files[file];
            if (f.snapshot) {
                old = f.snapshot.getText(0, f.snapshot.getLength());
                if (old != c) {
                    f.version++;
                }
            }
        }
        else {
            this.files[file] = new TSFile();
        }
        this.files[file].snapshot = _ts.ScriptSnapshot.fromString(c);
        this.files[file].snapshot.getChangeRange = function (oldSnapshot) { return null; };
    };
    BridgeServiceHost.prototype.addFile = function (fileName, path) {
        log.debug('host.addFile: %s: %s', fileName, path);
        if (this.files[fileName]) {
            this.files[fileName].version++;
            this.files[fileName].snapshot = null;
        }
        else {
            this.files[fileName] = new TSFile();
            this.files[fileName].path = path;
        }
    };
    BridgeServiceHost.prototype.log = function (s) {
        //log.info(s);	
    };
    BridgeServiceHost.prototype.trace = function (s) {
        //log.debug(s);	
    };
    BridgeServiceHost.prototype.error = function (s) {
        //log.error(s);	
    };
    BridgeServiceHost.prototype.readDir = function (dir) {
        var _this = this;
        log.debug('host.readDir: %s', dir);
        var items = fs.readdirSync(this.baseDir + '/' + dir);
        items.forEach(function (item) {
            var stat = fs.statSync(_this.baseDir + '/' + dir + '/' + item);
            if (stat.isDirectory()) {
                _this.readDir((dir.length > 0 ? (dir + '/') : '') + item);
            }
            else if (stat.isFile() && item.indexOf('.ts') == item.length - 3) {
                var name = (dir.length > 0 ? (dir + '/') : '') + item;
                if (_this.files[name]) {
                    _this.files[name].snapshot = null;
                }
                else {
                    _this.files[name] = new TSFile();
                }
            }
        });
    };
    return BridgeServiceHost;
})();
exports.BridgeServiceHost = BridgeServiceHost;
//# sourceMappingURL=host.js.map