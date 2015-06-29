/// <reference path="../ts/typescriptServices.d.ts" />
/// <reference path="../node/node.d.ts"/>
/// <reference path="./log.ts" />


import log = require('./log')
import fs = require('fs')

var _ts: typeof ts = require('../ts/typescriptServices.js')

class TSFile {
	public version: number = 1;
	public snapshot: ts.IScriptSnapshot = null;
	public path: string = '';
}

export class BridgeServiceHost implements ts.LanguageServiceHost {
	private files: {[name: string]: TSFile} = {'std-lib/lib.d.ts' : {
    	version: 1,
    	snapshot: _ts.ScriptSnapshot.fromString(fs.readFileSync('../ts/lib.d.ts').toString()),
     	path: ''
	}};
	
	private baseDir:string = '';
	
	constructor(baseDir: string) {
		this.baseDir = baseDir
		if (fs.statSync(baseDir).isDirectory()) {
    		this.readDir('');
  		}
	} 
	
	public getCompilationSettings(): ts.CompilerOptions {
		log.debug('host.getCompilationSettings');
		return {
	   		target: ts.ScriptTarget.ES5,
	   		module: ts.ModuleKind.None
		}
	}
	
	public getScriptFileNames(): string[] {
	   	log.debug('host.getScriptFileNames')
	   	return Object.keys(this.files)
	}
	     
	public getScriptVersion(fileName: string): string {
		log.debug('host.getScriptVersion: %s', fileName)
		if (this.files[fileName] && this.files[fileName].version ) {
   			return this.files[fileName].version.toString()
  		}
	  	return '0'
	}
	
	public getScriptSnapshot(fileName: string): ts.IScriptSnapshot {
		log.debug('host.getScriptSnapshot: %s', fileName)
		if (this.files[fileName] == null) { 
    		this.files[fileName] = new TSFile()
  		}
  		
  		// we added white space to end of content to avoid code completion error
  		if (this.files[fileName].snapshot == null) {
			var content: Buffer = new Buffer('')
			try {
				content = fs.readFileSync(this.baseDir + '/' + fileName);
			} catch (e) {
				if (e.code !== "ENOENT") {
					throw e;
				} else {
					content = fs.readFileSync(this.files[fileName].path);
				}		      
			}			
			this.files[fileName].snapshot = _ts.ScriptSnapshot.fromString(content.toString() + ' ');
		}
		return this.files[fileName].snapshot; 
	}
	
	public getLocalizedDiagnosticMessages(): any {
		log.debug('host.getLocalizedDiagnosticMessages')
		return null
	}
	
		 //getCancellationToken?(): CancellationToken;
	     	
	public getCurrentDirectory(): string {
		log.debug('host.getCurrentDirectory(): %s', this.baseDir)
		return this.baseDir
	}
	     
    public getDefaultLibFileName(options: ts.CompilerOptions): string {
    	log.debug('host.getDefaultLibFileName(): std-lib/lib.d.ts')
     	return 'std-lib/lib.d.ts'
    }
	
	public setFileContent(file: string, content: string) {
		log.debug('host.setFileContent: %s: %s', file, content)
  		// we added white space to end of content to avoid code completion error
  		var c = content ? (content + ' ') : ' '
  		var snapshot = _ts.ScriptSnapshot.fromString(c)
  		var old = null;
  		if (this.files[file]) {
    		var f = this.files[file]
    		if (f.snapshot) {
      			old = f.snapshot.getText(0, f.snapshot.getLength())
      			if (old != c) {
        			f.version ++
      			}
    		}
  		} else { 
    		this.files[file] = new TSFile()
		}
 		
 		this.files[file].snapshot = _ts.ScriptSnapshot.fromString(c)
  		this.files[file].snapshot.getChangeRange = (oldSnapshot:any) => null
	}

	public addFile(fileName: string, path: string) {
  		log.debug('host.addFile: %s: %s', fileName, path)
  		
  		if (this.files[fileName]) {
			this.files[fileName].version++
			this.files[fileName].snapshot = null
		} else {
			this.files[fileName] = new TSFile()
			this.files[fileName].path = path
		}  
	}
	
    public log(s: string): void {
    	log.info(s);	
    }
	     
    public trace(s: string): void {
     	log.debug(s);	
    }
	     
    public error(s: string): void {
    	log.error(s);	
    }
	     
	private readDir(dir: string) {
		log.debug('host.readDir: %s', dir)       
		var items = fs.readdirSync(this.baseDir + '/' + dir);
	  	items.forEach((item) => {
	    	var stat = fs.statSync(this.baseDir + '/' + dir + '/' + item); 
    		if (stat.isDirectory()) {
       			this.readDir((dir.length > 0 ? (dir + '/') : '') + item);
    		} else if (stat.isFile() && item.indexOf('.ts') == item.length - 3) {
      			var name = (dir.length > 0 ? (dir + '/') : '') + item;
      			if (this.files[name]) {
        			this.files[name].snapshot = null;
      			} else {
        			this.files[name] = new TSFile();
      			}
    		}
  		})
	}
}
