/* Simple JavaScript Inheritance
 * By John Resig http://ejohn.org/
 * MIT Licensed.
 */
// Inspired by base2 and Prototype
(function() {
	var initializing = false, fnTest = /xyz/.test(function() {
		xyz;
	}) ? /\b_super\b/ : /.*/;

	// The base Class implementation (does nothing)
	this.Class = function() {
	};

	// Create a new Class that inherits from this class
	Class.extend = function(prop) {
		var _super = this.prototype;

		// Instantiate a base class (but only create the instance,
		// don't run the init constructor)
		initializing = true;
		var prototype = new this();
		initializing = false;

		// Copy the properties over onto the new prototype
		for ( var name in prop) {
			// Check if we're overwriting an existing function
			prototype[name] = typeof prop[name] == "function"
					&& typeof _super[name] == "function"
					&& fnTest.test(prop[name]) ? (function(name, fn) {
				return function() {
					var tmp = this._super;

					// Add a new ._super() method that is the same method
					// but on the super-class
					this._super = _super[name];

					// The method only need to be bound temporarily, so we
					// remove it when we're done executing
					var ret = fn.apply(this, arguments);
					this._super = tmp;

					return ret;
				};
			})(name, prop[name]) : prop[name];
		}

		// The dummy class constructor
		function Class() {
			// All construction is actually done in the init method
			if (!initializing && this.init)
				this.init.apply(this, arguments);
		}

		// Populate our constructed prototype object
		Class.prototype = prototype;

		// Enforce the constructor to be what we expect
		Class.prototype.constructor = Class;

		// And make this class extendable
		Class.extend = arguments.callee;

		return Class;
	};
})();
/*
 * Purl (A JavaScript URL parser) v2.3.1
 * Developed and maintanined by Mark Perkins, mark@allmarkedup.com
 * Source repository: https://github.com/allmarkedup/jQuery-URL-Parser
 * Licensed under an MIT-style license. See https://github.com/allmarkedup/jQuery-URL-Parser/blob/master/LICENSE for details.
 */

;(function(factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else {
        window.purl = factory();
    }
})(function() {

    var tag2attr = {
            a       : 'href',
            img     : 'src',
            form    : 'action',
            base    : 'href',
            script  : 'src',
            iframe  : 'src',
            link    : 'href'
        },

        key = ['source', 'protocol', 'authority', 'userInfo', 'user', 'password', 'host', 'port', 'relative', 'path', 'directory', 'file', 'query', 'fragment'], // keys available to query

        aliases = { 'anchor' : 'fragment' }, // aliases for backwards compatability

        parser = {
            strict : /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,  //less intuitive, more accurate to the specs
            loose :  /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*):?([^:@]*))?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/ // more intuitive, fails on relative paths and deviates from specs
        },

        isint = /^[0-9]+$/;

    function parseUri( url, strictMode ) {
        var str = decodeURI( url ),
        res   = parser[ strictMode || false ? 'strict' : 'loose' ].exec( str ),
        uri = { attr : {}, param : {}, seg : {} },
        i   = 14;

        while ( i-- ) {
            uri.attr[ key[i] ] = res[i] || '';
        }

        // build query and fragment parameters
        uri.param['query'] = parseString(uri.attr['query']);
        uri.param['fragment'] = parseString(uri.attr['fragment']);

        // split path and fragement into segments
        uri.seg['path'] = uri.attr.path.replace(/^\/+|\/+$/g,'').split('/');
        uri.seg['fragment'] = uri.attr.fragment.replace(/^\/+|\/+$/g,'').split('/');

        // compile a 'base' domain attribute
        uri.attr['base'] = uri.attr.host ? (uri.attr.protocol ?  uri.attr.protocol+'://'+uri.attr.host : uri.attr.host) + (uri.attr.port ? ':'+uri.attr.port : '') : '';

        return uri;
    }

    function getAttrName( elm ) {
        var tn = elm.tagName;
        if ( typeof tn !== 'undefined' ) return tag2attr[tn.toLowerCase()];
        return tn;
    }

    function promote(parent, key) {
        if (parent[key].length === 0) return parent[key] = {};
        var t = {};
        for (var i in parent[key]) t[i] = parent[key][i];
        parent[key] = t;
        return t;
    }

    function parse(parts, parent, key, val) {
        var part = parts.shift();
        if (!part) {
            if (isArray(parent[key])) {
                parent[key].push(val);
            } else if ('object' == typeof parent[key]) {
                parent[key] = val;
            } else if ('undefined' == typeof parent[key]) {
                parent[key] = val;
            } else {
                parent[key] = [parent[key], val];
            }
        } else {
            var obj = parent[key] = parent[key] || [];
            if (']' == part) {
                if (isArray(obj)) {
                    if ('' !== val) obj.push(val);
                } else if ('object' == typeof obj) {
                    obj[keys(obj).length] = val;
                } else {
                    obj = parent[key] = [parent[key], val];
                }
            } else if (~part.indexOf(']')) {
                part = part.substr(0, part.length - 1);
                if (!isint.test(part) && isArray(obj)) obj = promote(parent, key);
                parse(parts, obj, part, val);
                // key
            } else {
                if (!isint.test(part) && isArray(obj)) obj = promote(parent, key);
                parse(parts, obj, part, val);
            }
        }
    }

    function merge(parent, key, val) {
        if (~key.indexOf(']')) {
            var parts = key.split('[');
            parse(parts, parent, 'base', val);
        } else {
            if (!isint.test(key) && isArray(parent.base)) {
                var t = {};
                for (var k in parent.base) t[k] = parent.base[k];
                parent.base = t;
            }
            if (key !== '') {
                set(parent.base, key, val);
            }
        }
        return parent;
    }

    function parseString(str) {
        return reduce(String(str).split(/&|;/), function(ret, pair) {
            try {
                pair = decodeURIComponent(pair.replace(/\+/g, ' '));
            } catch(e) {
                // ignore
            }
            var eql = pair.indexOf('='),
                brace = lastBraceInKey(pair),
                key = pair.substr(0, brace || eql),
                val = pair.substr(brace || eql, pair.length);

            val = val.substr(val.indexOf('=') + 1, val.length);

            if (key === '') {
                key = pair;
                val = '';
            }

            return merge(ret, key, val);
        }, { base: {} }).base;
    }

    function set(obj, key, val) {
        var v = obj[key];
        if (typeof v === 'undefined') {
            obj[key] = val;
        } else if (isArray(v)) {
            v.push(val);
        } else {
            obj[key] = [v, val];
        }
    }

    function lastBraceInKey(str) {
        var len = str.length,
            brace,
            c;
        for (var i = 0; i < len; ++i) {
            c = str[i];
            if (']' == c) brace = false;
            if ('[' == c) brace = true;
            if ('=' == c && !brace) return i;
        }
    }

    function reduce(obj, accumulator){
        var i = 0,
            l = obj.length >> 0,
            curr = arguments[2];
        while (i < l) {
            if (i in obj) curr = accumulator.call(undefined, curr, obj[i], i, obj);
            ++i;
        }
        return curr;
    }

    function isArray(vArg) {
        return Object.prototype.toString.call(vArg) === "[object Array]";
    }

    function keys(obj) {
        var key_array = [];
        for ( var prop in obj ) {
            if ( obj.hasOwnProperty(prop) ) key_array.push(prop);
        }
        return key_array;
    }

    function purl( url, strictMode ) {
        if ( arguments.length === 1 && url === true ) {
            strictMode = true;
            url = undefined;
        }
        strictMode = strictMode || false;
        url = url || window.location.toString();

        return {

            data : parseUri(url, strictMode),

            // get various attributes from the URI
            attr : function( attr ) {
                attr = aliases[attr] || attr;
                return typeof attr !== 'undefined' ? this.data.attr[attr] : this.data.attr;
            },

            // return query string parameters
            param : function( param ) {
                return typeof param !== 'undefined' ? this.data.param.query[param] : this.data.param.query;
            },

            // return fragment parameters
            fparam : function( param ) {
                return typeof param !== 'undefined' ? this.data.param.fragment[param] : this.data.param.fragment;
            },

            // return path segments
            segment : function( seg ) {
                if ( typeof seg === 'undefined' ) {
                    return this.data.seg.path;
                } else {
                    seg = seg < 0 ? this.data.seg.path.length + seg : seg - 1; // negative segments count from the end
                    return this.data.seg.path[seg];
                }
            },

            // return fragment segments
            fsegment : function( seg ) {
                if ( typeof seg === 'undefined' ) {
                    return this.data.seg.fragment;
                } else {
                    seg = seg < 0 ? this.data.seg.fragment.length + seg : seg - 1; // negative segments count from the end
                    return this.data.seg.fragment[seg];
                }
            }

        };

    }
    
    purl.jQuery = function($){
        if ($ != null) {
            $.fn.url = function( strictMode ) {
                var url = '';
                if ( this.length ) {
                    url = $(this).attr( getAttrName(this[0]) ) || '';
                }
                return purl( url, strictMode );
            };

            $.url = purl;
        }
    };

    purl.jQuery(window.jQuery);

    return purl;

});
/*!
 * jQuery Templates Plugin 1.0.0pre
 * http://github.com/jquery/jquery-tmpl
 * Requires jQuery 1.4.2
 *
 * Copyright 2011, Software Freedom Conservancy, Inc.
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 */
(function( jQuery, undefined ){
	var oldManip = jQuery.fn.domManip, tmplItmAtt = "_tmplitem", htmlExpr = /^[^<]*(<[\w\W]+>)[^>]*$|\{\{\! /,
		newTmplItems = {}, wrappedItems = {}, appendToTmplItems, topTmplItem = { key: 0, data: {} }, itemKey = 0, cloneIndex = 0, stack = [];

	function newTmplItem( options, parentItem, fn, data ) {
		// Returns a template item data structure for a new rendered instance of a template (a 'template item').
		// The content field is a hierarchical array of strings and nested items (to be
		// removed and replaced by nodes field of dom elements, once inserted in DOM).
		var newItem = {
			data: data || (data === 0 || data === false) ? data : (parentItem ? parentItem.data : {}),
			_wrap: parentItem ? parentItem._wrap : null,
			tmpl: null,
			parent: parentItem || null,
			nodes: [],
			calls: tiCalls,
			nest: tiNest,
			wrap: tiWrap,
			html: tiHtml,
			update: tiUpdate
		};
		if ( options ) {
			jQuery.extend( newItem, options, { nodes: [], parent: parentItem });
		}
		if ( fn ) {
			// Build the hierarchical content to be used during insertion into DOM
			newItem.tmpl = fn;
			newItem._ctnt = newItem._ctnt || newItem.tmpl( jQuery, newItem );
			newItem.key = ++itemKey;
			// Keep track of new template item, until it is stored as jQuery Data on DOM element
			(stack.length ? wrappedItems : newTmplItems)[itemKey] = newItem;
		}
		return newItem;
	}

	// Override appendTo etc., in order to provide support for targeting multiple elements. (This code would disappear if integrated in jquery core).
	jQuery.each({
		appendTo: "append",
		prependTo: "prepend",
		insertBefore: "before",
		insertAfter: "after",
		replaceAll: "replaceWith"
	}, function( name, original ) {
		jQuery.fn[ name ] = function( selector ) {
			var ret = [], insert = jQuery( selector ), elems, i, l, tmplItems,
				parent = this.length === 1 && this[0].parentNode;

			appendToTmplItems = newTmplItems || {};
			if ( parent && parent.nodeType === 11 && parent.childNodes.length === 1 && insert.length === 1 ) {
				insert[ original ]( this[0] );
				ret = this;
			} else {
				for ( i = 0, l = insert.length; i < l; i++ ) {
					cloneIndex = i;
					elems = (i > 0 ? this.clone(true) : this).get();
					jQuery( insert[i] )[ original ]( elems );
					ret = ret.concat( elems );
				}
				cloneIndex = 0;
				ret = this.pushStack( ret, name, insert.selector );
			}
			tmplItems = appendToTmplItems;
			appendToTmplItems = null;
			jQuery.tmpl.complete( tmplItems );
			return ret;
		};
	});

	jQuery.fn.extend({
		// Use first wrapped element as template markup.
		// Return wrapped set of template items, obtained by rendering template against data.
		tmpl: function( data, options, parentItem ) {
			return jQuery.tmpl( this[0], data, options, parentItem );
		},

		// Find which rendered template item the first wrapped DOM element belongs to
		tmplItem: function() {
			return jQuery.tmplItem( this[0] );
		},

		// Consider the first wrapped element as a template declaration, and get the compiled template or store it as a named template.
		template: function( name ) {
			return jQuery.template( name, this[0] );
		},

		domManip: function( args, table, callback, options ) {
			if ( args[0] && jQuery.isArray( args[0] )) {
				var dmArgs = jQuery.makeArray( arguments ), elems = args[0], elemsLength = elems.length, i = 0, tmplItem;
				while ( i < elemsLength && !(tmplItem = jQuery.data( elems[i++], "tmplItem" ))) {}
				if ( tmplItem && cloneIndex ) {
					dmArgs[2] = function( fragClone ) {
						// Handler called by oldManip when rendered template has been inserted into DOM.
						jQuery.tmpl.afterManip( this, fragClone, callback );
					};
				}
				oldManip.apply( this, dmArgs );
			} else {
				oldManip.apply( this, arguments );
			}
			cloneIndex = 0;
			if ( !appendToTmplItems ) {
				jQuery.tmpl.complete( newTmplItems );
			}
			return this;
		}
	});

	jQuery.extend({
		// Return wrapped set of template items, obtained by rendering template against data.
		tmpl: function( tmpl, data, options, parentItem ) {
			var ret, topLevel = !parentItem;
			if ( topLevel ) {
				// This is a top-level tmpl call (not from a nested template using {{tmpl}})
				parentItem = topTmplItem;
				tmpl = jQuery.template[tmpl] || jQuery.template( null, tmpl );
				wrappedItems = {}; // Any wrapped items will be rebuilt, since this is top level
			} else if ( !tmpl ) {
				// The template item is already associated with DOM - this is a refresh.
				// Re-evaluate rendered template for the parentItem
				tmpl = parentItem.tmpl;
				newTmplItems[parentItem.key] = parentItem;
				parentItem.nodes = [];
				if ( parentItem.wrapped ) {
					updateWrapped( parentItem, parentItem.wrapped );
				}
				// Rebuild, without creating a new template item
				return jQuery( build( parentItem, null, parentItem.tmpl( jQuery, parentItem ) ));
			}
			if ( !tmpl ) {
				return []; // Could throw...
			}
			if ( typeof data === "function" ) {
				data = data.call( parentItem || {} );
			}
			if ( options && options.wrapped ) {
				updateWrapped( options, options.wrapped );
			}
			ret = jQuery.isArray( data ) ?
				jQuery.map( data, function( dataItem ) {
					return dataItem ? newTmplItem( options, parentItem, tmpl, dataItem ) : null;
				}) :
				[ newTmplItem( options, parentItem, tmpl, data ) ];
			return topLevel ? jQuery( build( parentItem, null, ret ) ) : ret;
		},

		// Return rendered template item for an element.
		tmplItem: function( elem ) {
			var tmplItem;
			if ( elem instanceof jQuery ) {
				elem = elem[0];
			}
			while ( elem && elem.nodeType === 1 && !(tmplItem = jQuery.data( elem, "tmplItem" )) && (elem = elem.parentNode) ) {}
			return tmplItem || topTmplItem;
		},

		// Set:
		// Use $.template( name, tmpl ) to cache a named template,
		// where tmpl is a template string, a script element or a jQuery instance wrapping a script element, etc.
		// Use $( "selector" ).template( name ) to provide access by name to a script block template declaration.

		// Get:
		// Use $.template( name ) to access a cached template.
		// Also $( selectorToScriptBlock ).template(), or $.template( null, templateString )
		// will return the compiled template, without adding a name reference.
		// If templateString includes at least one HTML tag, $.template( templateString ) is equivalent
		// to $.template( null, templateString )
		template: function( name, tmpl ) {
			if (tmpl) {
				// Compile template and associate with name
				if ( typeof tmpl === "string" ) {
					// This is an HTML string being passed directly in.
					tmpl = buildTmplFn( tmpl );
				} else if ( tmpl instanceof jQuery ) {
					tmpl = tmpl[0] || {};
				}
				if ( tmpl.nodeType ) {
					// If this is a template block, use cached copy, or generate tmpl function and cache.
					tmpl = jQuery.data( tmpl, "tmpl" ) || jQuery.data( tmpl, "tmpl", buildTmplFn( tmpl.innerHTML ));
					// Issue: In IE, if the container element is not a script block, the innerHTML will remove quotes from attribute values whenever the value does not include white space.
					// This means that foo="${x}" will not work if the value of x includes white space: foo="${x}" -> foo=value of x.
					// To correct this, include space in tag: foo="${ x }" -> foo="value of x"
				}
				return typeof name === "string" ? (jQuery.template[name] = tmpl) : tmpl;
			}
			// Return named compiled template
			return name ? (typeof name !== "string" ? jQuery.template( null, name ):
				(jQuery.template[name] ||
					// If not in map, and not containing at least on HTML tag, treat as a selector.
					// (If integrated with core, use quickExpr.exec)
					jQuery.template( null, htmlExpr.test( name ) ? name : jQuery( name )))) : null;
		},

		encode: function( text ) {
			// Do HTML encoding replacing < > & and ' and " by corresponding entities.
			return ("" + text).split("<").join("&lt;").split(">").join("&gt;").split('"').join("&#34;").split("'").join("&#39;");
		}
	});

	jQuery.extend( jQuery.tmpl, {
		tag: {
			"tmpl": {
				_default: { $2: "null" },
				open: "if($notnull_1){__=__.concat($item.nest($1,$2));}"
				// tmpl target parameter can be of type function, so use $1, not $1a (so not auto detection of functions)
				// This means that {{tmpl foo}} treats foo as a template (which IS a function).
				// Explicit parens can be used if foo is a function that returns a template: {{tmpl foo()}}.
			},
			"wrap": {
				_default: { $2: "null" },
				open: "$item.calls(__,$1,$2);__=[];",
				close: "call=$item.calls();__=call._.concat($item.wrap(call,__));"
			},
			"each": {
				_default: { $2: "$index, $value" },
				open: "if($notnull_1){$.each($1a,function($2){with(this){",
				close: "}});}"
			},
			"if": {
				open: "if(($notnull_1) && $1a){",
				close: "}"
			},
			"else": {
				_default: { $1: "true" },
				open: "}else if(($notnull_1) && $1a){"
			},
			"html": {
				// Unecoded expression evaluation.
				open: "if($notnull_1){__.push($1a);}"
			},
			"=": {
				// Encoded expression evaluation. Abbreviated form is ${}.
				_default: { $1: "$data" },
				open: "if($notnull_1){__.push($.encode($1a));}"
			},
			"!": {
				// Comment tag. Skipped by parser
				open: ""
			}
		},

		// This stub can be overridden, e.g. in jquery.tmplPlus for providing rendered events
		complete: function( items ) {
			newTmplItems = {};
		},

		// Call this from code which overrides domManip, or equivalent
		// Manage cloning/storing template items etc.
		afterManip: function afterManip( elem, fragClone, callback ) {
			// Provides cloned fragment ready for fixup prior to and after insertion into DOM
			var content = fragClone.nodeType === 11 ?
				jQuery.makeArray(fragClone.childNodes) :
				fragClone.nodeType === 1 ? [fragClone] : [];

			// Return fragment to original caller (e.g. append) for DOM insertion
			callback.call( elem, fragClone );

			// Fragment has been inserted:- Add inserted nodes to tmplItem data structure. Replace inserted element annotations by jQuery.data.
			storeTmplItems( content );
			cloneIndex++;
		}
	});

	//========================== Private helper functions, used by code above ==========================

	function build( tmplItem, nested, content ) {
		// Convert hierarchical content into flat string array
		// and finally return array of fragments ready for DOM insertion
		var frag, ret = content ? jQuery.map( content, function( item ) {
			return (typeof item === "string") ?
				// Insert template item annotations, to be converted to jQuery.data( "tmplItem" ) when elems are inserted into DOM.
				(tmplItem.key ? item.replace( /(<\w+)(?=[\s>])(?![^>]*_tmplitem)([^>]*)/g, "$1 " + tmplItmAtt + "=\"" + tmplItem.key + "\" $2" ) : item) :
				// This is a child template item. Build nested template.
				build( item, tmplItem, item._ctnt );
		}) :
		// If content is not defined, insert tmplItem directly. Not a template item. May be a string, or a string array, e.g. from {{html $item.html()}}.
		tmplItem;
		if ( nested ) {
			return ret;
		}

		// top-level template
		ret = ret.join("");

		// Support templates which have initial or final text nodes, or consist only of text
		// Also support HTML entities within the HTML markup.
		ret.replace( /^\s*([^<\s][^<]*)?(<[\w\W]+>)([^>]*[^>\s])?\s*$/, function( all, before, middle, after) {
			frag = jQuery( middle ).get();

			storeTmplItems( frag );
			if ( before ) {
				frag = unencode( before ).concat(frag);
			}
			if ( after ) {
				frag = frag.concat(unencode( after ));
			}
		});
		return frag ? frag : unencode( ret );
	}

	function unencode( text ) {
		// Use createElement, since createTextNode will not render HTML entities correctly
		var el = document.createElement( "div" );
		el.innerHTML = text;
		return jQuery.makeArray(el.childNodes);
	}

	// Generate a reusable function that will serve to render a template against data
	function buildTmplFn( markup ) {
		return new Function("jQuery","$item",
			// Use the variable __ to hold a string array while building the compiled template. (See https://github.com/jquery/jquery-tmpl/issues#issue/10).
			"var $=jQuery,call,__=[],$data=$item.data;" +

			// Introduce the data as local variables using with(){}
			"with($data){__.push('" +

			// Convert the template into pure JavaScript
			jQuery.trim(markup)
				.replace( /([\\'])/g, "\\$1" )
				.replace( /[\r\t\n]/g, " " )
				.replace( /\$\{([^\}]*)\}/g, "{{= $1}}" )
				.replace( /\{\{(\/?)(\w+|.)(?:\(((?:[^\}]|\}(?!\}))*?)?\))?(?:\s+(.*?)?)?(\(((?:[^\}]|\}(?!\}))*?)\))?\s*\}\}/g,
				function( all, slash, type, fnargs, target, parens, args ) {
					var tag = jQuery.tmpl.tag[ type ], def, expr, exprAutoFnDetect;
					if ( !tag ) {
						throw "Unknown template tag: " + type;
					}
					def = tag._default || [];
					if ( parens && !/\w$/.test(target)) {
						target += parens;
						parens = "";
					}
					if ( target ) {
						target = unescape( target );
						args = args ? ("," + unescape( args ) + ")") : (parens ? ")" : "");
						// Support for target being things like a.toLowerCase();
						// In that case don't call with template item as 'this' pointer. Just evaluate...
						expr = parens ? (target.indexOf(".") > -1 ? target + unescape( parens ) : ("(" + target + ").call($item" + args)) : target;
						exprAutoFnDetect = parens ? expr : "(typeof(" + target + ")==='function'?(" + target + ").call($item):(" + target + "))";
					} else {
						exprAutoFnDetect = expr = def.$1 || "null";
					}
					fnargs = unescape( fnargs );
					return "');" +
						tag[ slash ? "close" : "open" ]
							.split( "$notnull_1" ).join( target ? "typeof(" + target + ")!=='undefined' && (" + target + ")!=null" : "true" )
							.split( "$1a" ).join( exprAutoFnDetect )
							.split( "$1" ).join( expr )
							.split( "$2" ).join( fnargs || def.$2 || "" ) +
						"__.push('";
				}) +
			"');}return __;"
		);
	}
	function updateWrapped( options, wrapped ) {
		// Build the wrapped content.
		options._wrap = build( options, true,
			// Suport imperative scenario in which options.wrapped can be set to a selector or an HTML string.
			jQuery.isArray( wrapped ) ? wrapped : [htmlExpr.test( wrapped ) ? wrapped : jQuery( wrapped ).html()]
		).join("");
	}

	function unescape( args ) {
		return args ? args.replace( /\\'/g, "'").replace(/\\\\/g, "\\" ) : null;
	}
	function outerHtml( elem ) {
		var div = document.createElement("div");
		div.appendChild( elem.cloneNode(true) );
		return div.innerHTML;
	}

	// Store template items in jQuery.data(), ensuring a unique tmplItem data data structure for each rendered template instance.
	function storeTmplItems( content ) {
		var keySuffix = "_" + cloneIndex, elem, elems, newClonedItems = {}, i, l, m;
		for ( i = 0, l = content.length; i < l; i++ ) {
			if ( (elem = content[i]).nodeType !== 1 ) {
				continue;
			}
			elems = elem.getElementsByTagName("*");
			for ( m = elems.length - 1; m >= 0; m-- ) {
				processItemKey( elems[m] );
			}
			processItemKey( elem );
		}
		function processItemKey( el ) {
			var pntKey, pntNode = el, pntItem, tmplItem, key;
			// Ensure that each rendered template inserted into the DOM has its own template item,
			if ( (key = el.getAttribute( tmplItmAtt ))) {
				while ( pntNode.parentNode && (pntNode = pntNode.parentNode).nodeType === 1 && !(pntKey = pntNode.getAttribute( tmplItmAtt ))) { }
				if ( pntKey !== key ) {
					// The next ancestor with a _tmplitem expando is on a different key than this one.
					// So this is a top-level element within this template item
					// Set pntNode to the key of the parentNode, or to 0 if pntNode.parentNode is null, or pntNode is a fragment.
					pntNode = pntNode.parentNode ? (pntNode.nodeType === 11 ? 0 : (pntNode.getAttribute( tmplItmAtt ) || 0)) : 0;
					if ( !(tmplItem = newTmplItems[key]) ) {
						// The item is for wrapped content, and was copied from the temporary parent wrappedItem.
						tmplItem = wrappedItems[key];
						tmplItem = newTmplItem( tmplItem, newTmplItems[pntNode]||wrappedItems[pntNode] );
						tmplItem.key = ++itemKey;
						newTmplItems[itemKey] = tmplItem;
					}
					if ( cloneIndex ) {
						cloneTmplItem( key );
					}
				}
				el.removeAttribute( tmplItmAtt );
			} else if ( cloneIndex && (tmplItem = jQuery.data( el, "tmplItem" )) ) {
				// This was a rendered element, cloned during append or appendTo etc.
				// TmplItem stored in jQuery data has already been cloned in cloneCopyEvent. We must replace it with a fresh cloned tmplItem.
				cloneTmplItem( tmplItem.key );
				newTmplItems[tmplItem.key] = tmplItem;
				pntNode = jQuery.data( el.parentNode, "tmplItem" );
				pntNode = pntNode ? pntNode.key : 0;
			}
			if ( tmplItem ) {
				pntItem = tmplItem;
				// Find the template item of the parent element.
				// (Using !=, not !==, since pntItem.key is number, and pntNode may be a string)
				while ( pntItem && pntItem.key != pntNode ) {
					// Add this element as a top-level node for this rendered template item, as well as for any
					// ancestor items between this item and the item of its parent element
					pntItem.nodes.push( el );
					pntItem = pntItem.parent;
				}
				// Delete content built during rendering - reduce API surface area and memory use, and avoid exposing of stale data after rendering...
				delete tmplItem._ctnt;
				delete tmplItem._wrap;
				// Store template item as jQuery data on the element
				jQuery.data( el, "tmplItem", tmplItem );
			}
			function cloneTmplItem( key ) {
				key = key + keySuffix;
				tmplItem = newClonedItems[key] =
					(newClonedItems[key] || newTmplItem( tmplItem, newTmplItems[tmplItem.parent.key + keySuffix] || tmplItem.parent ));
			}
		}
	}

	//---- Helper functions for template item ----

	function tiCalls( content, tmpl, data, options ) {
		if ( !content ) {
			return stack.pop();
		}
		stack.push({ _: content, tmpl: tmpl, item:this, data: data, options: options });
	}

	function tiNest( tmpl, data, options ) {
		// nested template, using {{tmpl}} tag
		return jQuery.tmpl( jQuery.template( tmpl ), data, options, this );
	}

	function tiWrap( call, wrapped ) {
		// nested template, using {{wrap}} tag
		var options = call.options || {};
		options.wrapped = wrapped;
		// Apply the template, which may incorporate wrapped content,
		return jQuery.tmpl( jQuery.template( call.tmpl ), call.data, options, call.item );
	}

	function tiHtml( filter, textOnly ) {
		var wrapped = this._wrap;
		return jQuery.map(
			jQuery( jQuery.isArray( wrapped ) ? wrapped.join("") : wrapped ).filter( filter || "*" ),
			function(e) {
				return textOnly ?
					e.innerText || e.textContent :
					e.outerHTML || outerHtml(e);
			});
	}

	function tiUpdate() {
		var coll = this.nodes;
		jQuery.tmpl( null, null, null, this).insertBefore( coll[0] );
		jQuery( coll ).remove();
	}
})( jQuery );

var GET_CAPABILITIES_TYPE = "GetCapabilities"; 
var DESCRIBE_PROCESS_TYPE = "DescribeProcess";
var EXECUTE_TYPE = "Execute";

var OWS_11_NAMESPACE = "http://www.opengis.net/ows/1.1";
var WPS_100_NAMESPACE = "http://www.opengis.net/wps/1.0.0";

var METHOD_POST = "POST";
var METHOD_GET = "GET";
var PARAM_WPS_REQUEST_URL = "wpsRequestUrl";
var PARAM_WPS_REQUEST_TYPE = "wpsRequestType";

var USE_PROXY = false;
var PROXY_URL = "";
var PROXY_TYPE = "";

var USER_TEMPLATE_CAPABILITIES_MARKUP = null;
var USER_TEMPLATE_PROCESS_DESCRIPTION_MARKUP = null;
var USER_TEMPLATE_EXECUTE_RESPONSE_MARKUP = null;

function wpsResetSetup() {
	USE_PROXY = false;
	PROXY_URL = "";
	PROXY_TYPE = "";

	USER_TEMPLATE_CAPABILITIES_MARKUP = null;
	USER_TEMPLATE_PROCESS_DESCRIPTION_MARKUP = null;
	USER_TEMPLATE_EXECUTE_RESPONSE_MARKUP = null;
}

function equalsString(a, b) {
	if (!a) {
		return false;
	}
	
	if (!b) {
		return false;
	}
	
	return jQuery.trim(a).localeCompare(jQuery.trim(b)) == 0;
}

function stringStartsWith(target, sub) {
	return target.indexOf(sub) == 0;
}

function fillXMLTemplate(template, properties) {
	var result = template;
	
	for (var key in properties) {
		if (properties.hasOwnProperty(key)) {
			result = result.replace("${"+key+"}", properties[key]);	
		}
	}
	
	return result;
}
var WPSConfiguration = Class.extend({
	
	init : function(settings) {
		this.settings = settings;
	},
	
	getServiceUrl : function() {
		return this.settings.url;
	}

});

var BaseResponse = Class.extend({
	
	init : function(xmlResponse, originalRequest) {
		this.xmlResponse = xmlResponse;
		this.originalRequest = originalRequest;
	},
	
	createMarkup : function() {
		return '<div class="wps-success"><div class="wps-generic-success"></div></div>';
	}

});

var TEMPLATE_CAPABILITIES_MARKUP = '\
	<div class="wps-capabilities"> \
		<div class="wps-capabilities-serviceidentification"> \
			<ul class="wps-capabilities-list"> \
				<li class="wps-capabilities-list-entry"> \
					<label class="wps-item-label">Title</label><span class="wps-item-value">${serviceTitle}</span></li> \
				<li class="wps-capabilities-list-entry"> \
					<label class="wps-item-label">Abstract</label><span class="wps-item-value">${serviceAbstract}</li> \
				<li class="wps-capabilities-list-entry"> \
					<label class="wps-item-label">Keywords</label><span class="wps-item-value">${serviceKeywords}</li> \
				<li class="wps-capabilities-list-entry"> \
					<label class="wps-item-label">Type</label><span class="wps-item-value">${serviceType}</li> \
				<li class="wps-capabilities-list-entry"> \
					<label class="wps-item-label">Version</label><span class="wps-item-value">${serviceVersion}</li> \
				<li class="wps-capabilities-list-entry"> \
					<label class="wps-item-label">Fees</label><span class="wps-item-value">${serviceFees}</li> \
				<li class="wps-capabilities-list-entry"> \
					<label class="wps-item-label">Access constraints</label><span class="wps-item-value">${serviceAccessConstraints}</li> \
			</ul> \
		</div> \
		<div class="wps-capabilities-serviceprovider"> \
			<ul class="wps-capabilities-list"> \
				<li class="wps-capabilities-list-entry"> \
					<label class="wps-item-label">Provider name</label><span class="wps-item-value">${providerName}</li> \
				<li class="wps-capabilities-list-entry"> \
					<label class="wps-item-label">Provider site</label><span class="wps-item-value"><a href="${providerSite}" target="_blank" title="Open link in new window">${providerSite}</a></li> \
			</ul> \
			<div class="wps-capabilities-serviceprovider-contact"> \
		</div> \
		<div class="wps-capabilities-full-link"> \
			<a href="${capabilitiesFullLink}" title="Show original capabilities document in a new window" target="_blank">Full capabilities</a> \
		</div> \
	</div>';

var CapabilitiesResponse = BaseResponse.extend({

	createMarkup : function() {
		var ident = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "ServiceIdentification");
		var identTitle = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "Title")[0];
		var identAbstract = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "Abstract");
		var identKeywords = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "Keyword");
		var keywords = "";
		jQuery.each(identKeywords, function(index, value) {
			keywords += jQuery(value).text() + " ";
		});
		
		var identType = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "ServiceType");
		var identVersion = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "ServiceTypeVersion");
		var identFees = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "Fees");
		var identAccessConstr = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "AccessConstraints");
		
		var provName = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "ProviderName");
		var provSite = this.xmlResponse.getElementsByTagNameNS(OWS_11_NAMESPACE, "ProviderSite");
		var site = jQuery(provSite).attr("xlink:href");
		
		capabilitiesProperties = {
				serviceTitle: jQuery(identTitle).text(),
				serviceAbstract: jQuery(identAbstract).text(),
				serviceKeywords: keywords,
				serviceType: jQuery(identType).text(),
				serviceVersion: jQuery(identVersion).text(),
				serviceFees: jQuery(identFees).text(),
				serviceAccessConstraints: jQuery(identAccessConstr).text(),
				providerName: jQuery(provName).text(),
				providerSite: site,
				capabilitiesFullLink: this.originalRequest.settings.url
		};
		
		var content = jQuery.tmpl(TEMPLATE_CAPABILITIES_MARKUP, capabilitiesProperties);
		
		return content;
	}

});

var TEMPLATE_PROCESS_DESCRIPTION_MARKUP = '\
	<div class="wps-process-description"> \
		<div class="wps-process-description-general"> \
			<ul class="process-description-list"> \
				<li class="wps-execute-response-list-entry>${process-identifier}<li> \
				<li class="wps-execute-response-list-entry>${process-title}<li> \
			</ul> \
		</div> \
		<div class="wps-process-description-inputs"> \
			<ul class="process-description-list"> \
				<li class="wps-execute-response-list-entry>${process-identifier}<li> \
				<li class="wps-execute-response-list-entry>${process-title}<li> \
			</ul> \
		</div> \
		<div class="wps-process-description-outputs"> \
			<ul class="process-description-list"> \
				<li class="wps-execute-response-list-entry>${process-identifier}<li> \
				<li class="wps-execute-response-list-entry>${process-title}<li> \
			</ul> \
		</div> \
	</div>';

var DescribeProcessResponse = BaseResponse.extend({

	createMarkup : function() {
		var provName = xml.getElementsByTagNameNS(OWS_11_NAMESPACE, "ProviderName");
		var content = "<div>"+jQuery(provName).text()+"</div>";
		return this._super(content);
	}

});
var TEMPLATE_EXECUTE_RESPONSE_MARKUP = '\
	<div class="wps-execute-response"> \
		<div class="wps-execute-autoUpdate" id="wps-execute-autoUpdate" style="${updateSwitchEnabled}"></div> \
		<div class="wps-execute-response-process"> \
			<ul class="wps-execute-response-list"> \
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-item-label">Identifier</label><span class="wps-item-value">${identifier}</span></li> \
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-item-label">Title</label><span class="wps-item-value">${title}</span></li> \
			</ul> \
		</div> \
		<div class="wps-execute-response-status" id="wps-execute-response-status"> \
			<ul class="wps-execute-response-list" id="wps-execute-response-list"> \
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-item-label">Created on </label><span class="wps-item-value">${creationTime}</span></li> \
			</ul> \
		</div> \
		<div id="wps-execute-response-extension"></div> \
	</div>';

var TEMPLATE_EXECUTE_RESPONSE_EXTENSION_MARKUP_DOWNLOAD = '\
	<div class="wps-execute-response-result"> \
			<label class="wps-extension-item-label">${key}</label><span class="wps-item-value"><a href="${value}" title="${title}">download</a></span></li> \
	</div>';

var TEMPLATE_EXECUTE_RESPONSE_EXTENSION_MARKUP_VALUE = '\
	<div class="wps-execute-response-result"> \
			<label class="wps-extension-item-label">${key}</label><span class="wps-item-value" title="${title} | ${valueType}">${value}</span></li> \
	</div>';

var TEMPLATE_EXECUTE_RESPONSE_STATUS_NORMAL_MARKUP = '\
	<li class="wps-execute-response-list-entry"> \
			<label class="wps-item-label">Status</label><span class="wps-item-value">${status}</span> \
	</li>';

var TEMPLATE_EXECUTE_RESPONSE_STATUS_FAILED_MARKUP = '\
	<li class="wps-execute-response-list-entry"> \
			<label class="wps-item-label">Status</label><span class="wps-item-error-value">${status}</span> \
	</li> \
	<li class="wps-execute-response-list-entry"> \
			<label class="wps-item-label">Message</label><span class="wps-item-error-message-value">${message}</span> \
	</li>';

var ExecuteResponse = BaseResponse.extend({
	
	resolveProcessOutputs : function(processOutputs) {
		var outputs = processOutputs.getElementsByTagNameNS(WPS_100_NAMESPACE, "Output");
		
		var array = new Array(outputs.length);
		for (var i = 0; i < outputs.length; i++) {
			var element = outputs[i];
			var identifier = element.getElementsByTagNameNS(OWS_11_NAMESPACE, "Identifier");
			var title = element.getElementsByTagNameNS(OWS_11_NAMESPACE, "Title");
			var reference = element.getElementsByTagNameNS(WPS_100_NAMESPACE, "Reference");
			var data = element.getElementsByTagNameNS(WPS_100_NAMESPACE, "Data");
			var value;
			var valueType = null;
			if (reference && reference.length > 0) { // create link from reference
				value = reference[0].getAttribute("href");
				array[i] = {
						key : jQuery(identifier).text(),
						title: jQuery(title).text(),
						value : value,
						ref: true
				};
			}
			else {
				if(data && data.length > 0) { // show inline values
					value = "", valueType = "";
					// each data child element
					jQuery(data).children().each(function(key, val) {
						var $val = jQuery(val);
						value += $val.text();
						valueType += $val.attr("dataType");
					});
				}
				else {
					value = "n/a";
				}
				
				array[i] = {
						key : jQuery(identifier).text(),
						title: jQuery(title).text(),
						value : value,
						valueType: valueType,
						ref: false
				};
			}
		}
		
		var result = {outputs : array};
		
		return result;
	},
	
	createMarkup : function() {
		var process = this.xmlResponse.getElementsByTagNameNS(WPS_100_NAMESPACE, "Process");
		var status = this.xmlResponse.getElementsByTagNameNS(WPS_100_NAMESPACE, "Status");
		
		var properties = null;
		var extensions = {};
		var statusText = null;
		var statusMessage = null;
		var processFailed = false;
		
		if (process && process[0] && status && status[0]) {
			var identifier = jQuery(process[0].getElementsByTagNameNS(OWS_11_NAMESPACE, "Identifier")).text();
			var title = jQuery(process[0].getElementsByTagNameNS(OWS_11_NAMESPACE, "Title")).text();	
			
			var accepted = status[0].getElementsByTagNameNS(WPS_100_NAMESPACE, "ProcessAccepted");
			if (accepted && accepted.length > 0) {
				statusText = jQuery(accepted).text();
			}
			if (!statusText) {
				var started = status[0].getElementsByTagNameNS(WPS_100_NAMESPACE, "ProcessStarted");
				if (started && started.length > 0) {
					var percent = started[0].getAttribute("percentCompleted");
					statusText = "Process started (" + percent + " % complete)";	
				}
			}
			//process paused
			if (!statusText) {
				var paused = status[0].getElementsByTagNameNS(WPS_100_NAMESPACE, "ProcessPaused");
				if (paused && paused.length > 0) {
					statusText = "Process paused";
				}
			}
			if (!statusText) {
				var succeeded = status[0].getElementsByTagNameNS(WPS_100_NAMESPACE, "ProcessSucceeded");
				if (succeeded && succeeded.length > 0) {
					statusText = "Process succeeded";
					var processOutputs = this.xmlResponse.getElementsByTagNameNS(WPS_100_NAMESPACE, "ProcessOutputs");
					if (processOutputs && processOutputs.length > 0) {
						extensions = jQuery.extend(this.resolveProcessOutputs(processOutputs[0]), extensions);	
					}
				}
			}
			//process failed
			if (!statusText) {
				var failed = status[0].getElementsByTagNameNS(WPS_100_NAMESPACE, "ProcessFailed");
				if (failed && failed.length > 0) {
					statusText = "Process failed";
					
					exceptionText = status[0].getElementsByTagNameNS(OWS_11_NAMESPACE, "ExceptionText");
					if(exceptionText) {
						statusMessage = exceptionText.item(0).innerHTML;
						//TODO display more than one exception text
					}
					processFailed = true;
				}
			}
			
			var time = status[0].getAttribute("creationTime");	
			if (time) {
			    var d = new Date(time);
			    time = d.toLocaleString();
			}
			
			var updateSwitch;
			if (this.originalRequest.updateSwitch) {
				updateSwitch = '';
			}
			else {
				updateSwitch = 'display:none';
			}
			
			properties = {
					identifier : identifier,
					title : title,
					creationTime : time,
					updateSwitchEnabled : updateSwitch
			};
		}
		
		var statusLocation = this.xmlResponse.documentElement.getAttribute("statusLocation");
		/*
		 * Make this updateable and NOT execute two times.
		 */
		if (statusLocation) {
			this.originalRequest.settings.url = statusLocation;
			//this.originalRequest = new GetRequest(this.originalRequest.settings);
			
			var getSettings = {
				url : this.originalRequest.settings.url,
				requestType : this.originalRequest.settings.requestType,
				type : "GET" 
			};
			
			this.originalRequest = new GetRequest(getSettings);
		}
		
		var template;
		if (USER_TEMPLATE_EXECUTE_RESPONSE_MARKUP != null) {
			template = USER_TEMPLATE_EXECUTE_RESPONSE_MARKUP;
		}
		else {
			template = TEMPLATE_EXECUTE_RESPONSE_MARKUP;
		}
		
		var result = jQuery.tmpl(template, properties);
		
		//insert status entry depending on normal status (started, accepted, paused, succeeded) or failed status
		var statusDiv = result.children('#wps-execute-response-status');
		var statusList = statusDiv.children('#wps-execute-response-list');
		
		statusProperties = {
			status: statusText,
			message: statusMessage
		};
		
		if(!processFailed){
			jQuery.tmpl(TEMPLATE_EXECUTE_RESPONSE_STATUS_NORMAL_MARKUP, statusProperties).appendTo(statusList);			
		}else{
			jQuery.tmpl(TEMPLATE_EXECUTE_RESPONSE_STATUS_FAILED_MARKUP, statusProperties).appendTo(statusList);
		}
		
		if (extensions && !jQuery.isEmptyObject(extensions)) {
			var extensionDiv = result.children('#wps-execute-response-extension');
			if (extensions.outputs) {
				jQuery(extensions.outputs).each(function(key, value) {
						if(value.ref == true) {
							jQuery.tmpl(TEMPLATE_EXECUTE_RESPONSE_EXTENSION_MARKUP_DOWNLOAD, value).appendTo(extensionDiv);
						}
						else {
							jQuery.tmpl(TEMPLATE_EXECUTE_RESPONSE_EXTENSION_MARKUP_VALUE, value).appendTo(extensionDiv);
						}
					});
			}
		}
		
		return result;
	}

});

var ResponseFactory = Class.extend({
	
	init : function(settings) {
		this.settings = settings;
	},
	
	resolveResponseHandler : function(xmlResponse, originalRequest) {
		var rootElement = xmlResponse.documentElement;
		
		if (rootElement.localName == "Capabilities") {
			return new CapabilitiesResponse(xmlResponse, originalRequest);
		}
		else if (rootElement.localName == "ProcessDescriptions") {
			return new DescribeProcessResponse(xmlResponse, originalRequest);
		}
		else if (rootElement.localName == "ExecuteResponse") {
			return new ExecuteResponse(xmlResponse, originalRequest);
		}
		else if (rootElement.localName == "ExceptionReport") {
			return new ExceptionReportResponse(xmlResponse, originalRequest);
		}
		
		return null;
	}

});
var TEMPLATE_EXCEPTION_REPORT_RESPONSE_MARKUP = '\
	<div class="wps-exception-report-response"> \
		<div class="wps-exception-report-response-exceptions"> \
			<ul class="wps-exception-list" id="wps-exception-list"> \
			</ul> \
		</div> \
		<div id="wps-exception-report-response-extension"></div> \
	</div>';

var TEMPLATE_EXCEPTION_MARKUP = '\
	<li class="wps-execute-response-list-entry"> \
			<label class="wps-item-label">Code</label><span class="wps-item-error-value">${code}</span> \
	</li> \
	<li class="wps-execute-response-list-entry"> \
			<label class="wps-item-label">Text</label><span class="wps-item-error-message-value">${text}</span> \
	</li>';

var ExceptionReportResponse = BaseResponse
		.extend({

			createMarkup : function() {
				var exceptionsFromResponse = this.xmlResponse
						.getElementsByTagNameNS(OWS_11_NAMESPACE, "Exception");

				console.log("Got exception response!");
				exceptions = jQuery(exceptionsFromResponse);
				console.log(exceptions);

				var parsedExceptions = [];
				for ( var i = 0; i < exceptions.length; i++) {
					var exc = jQuery(exceptions[i]);

					var parsedExc = {
						"code" : exc.attr("exceptionCode"),
						"text" : exc.text().trim()
					};
					parsedExceptions.push(parsedExc);
				}

				var properties = {};
				var result = jQuery.tmpl(
						TEMPLATE_EXCEPTION_REPORT_RESPONSE_MARKUP, properties);
				var exceptionList = result.children('#wps-exception-list');

				// var extensionDiv = result
				// .children('#wps-exception-report-response-extension');

				// TODO FIXME display exceptions
				if (parsedExceptions && !jQuery.isEmptyObject(parsedExceptions)) {
					jQuery(parsedExceptions)
							.each(
									function(key, value) {
										alert(key + " - " + value.code + ": "
												+ value.text);
											jQuery
													.tmpl(
															TEMPLATE_EXCEPTION_MARKUP,
 															value).appendTo(
															exceptionList);
									});
				}

				return result;
			}

		});

var BaseRequest = Class.extend({
	init : function(settings) {
		this.settings = settings;
	},

	getSettings : function() {
		return this.settings;
	},

	execute : function(callback, updateSwitch) {
		/*
		 * define a callback which gets called after finishing the request
		 */
		this.callback = callback;
		
		this.updateSwitch = updateSwitch;
		
		this.preRequestExecution();

		this.executeHTTPRequest(this.prepareHTTPRequest());

		this.postRequestExecution();
	},

	preRequestExecution : function() {
		this.changeElementContent('<div class="wps-waiting"></div>');
	},

	postRequestExecution : function() {

	},

	processResponse : function(xml) {
		return '<div class="wps-success"><div class="wps-generic-success"></div></div>';
	},

	changeElementContent : function(htmlContent) {
		if (this.settings.domElement) {
			this.settings.domElement.html(htmlContent);
		}
	},
	
	prepareHTTPRequest : function() {
		return null;
	},

	executeHTTPRequest : function(requestSettings) {
		/*
		 * we need 'self' as 'this' is different in the
		 * anonymous callbacks
		 */
		var self = this;
		
		var combinedRequestSettings = jQuery.extend({
			success : function(data) {
				if (self.callback) {
					self.callback(data, self.settings.domElement, self, self.updateSwitch);
				} else {
					htmlContent = self.processResponse(data);
					self.changeElementContent(htmlContent);	
				}
			},
			error: function() {
				self.changeElementContent('<div class="wps-error"><div class="wps-generic-error"></div>'+
						'<div>An error occured while connecting to '+
						self.settings.url +'</div></div>');
			}
		}, requestSettings);
		
		var targetUrl;
		if (USE_PROXY) {
			if (PROXY_TYPE == "parameter") {
				targetUrl = PROXY_URL + encodeURIComponent(this.settings.url);
			}
			else {
				//TODO split URL into host-base + query and create new
				targetUrl = this.settings.url;
			} 
		} else {
			targetUrl = this.settings.url;
		}
		jQuery.ajax(targetUrl, combinedRequestSettings);
	}
});

var GetRequest = BaseRequest.extend({

	prepareHTTPRequest : function() {
		var targetUrl = this.settings.url;
		var targetUrlQuery = this.settings.urlQuery;
		
		//check for a query part
		if (!targetUrlQuery) {
			targetUrlQuery = this.createTargetUrlQuery();
		}
		
		if (targetUrlQuery) {
			this.settings.url = this.buildTargetUrl(targetUrl, targetUrlQuery);
		}
		
		return {
			type : "GET"
		};
	},
	
	/*
	 * overwrite this method to define specific behavior
	 */
	createTargetUrlQuery : function() {
		return null;
	},
	
	buildTargetUrl : function(targetUrl, targetUrlQuery) {
		if (targetUrl.indexOf("?") == -1) {
			targetUrl += "?";
		}
		
		if (targetUrl.indexOf("service=") == -1) {
			targetUrl += "service=WPS";
		}
		
		if (targetUrl.indexOf("version=") == -1) {
			targetUrl += "&version=1.0.0";
		}
		
		if (targetUrlQuery) {
			targetUrl += "&" + targetUrlQuery;
		}
		
		return targetUrl;
	}

});

var PostRequest = BaseRequest.extend({

	prepareHTTPRequest : function() {
		var payload = this.settings.data;
		if (!payload) {
			payload = this.createPostPayload();
		}
		
		return {
			type : "POST",
			data : payload,
			contentType : "text/xml"
		};
	},
	
	/*
	 * overwrite this method to create specific payload
	 */
	createPostPayload : function() {
		return null;
	},
	
	fillTemplate : function(template, properties) {
		return fillXMLTemplate(template, properties);
	}

});
var EXECUTE_REQUEST_XML_START = '<wps:Execute service="WPS" version="1.0.0" \
	xmlns:wps="http://www.opengis.net/wps/1.0.0" \
	xmlns:ows="http://www.opengis.net/ows/1.1" \
	xmlns:xlink="http://www.w3.org/1999/xlink" \
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
	xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 \
	  http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd"> \
	  <ows:Identifier>${processIdentifier}</ows:Identifier>\
	  <wps:DataInputs>\
		${dataInputs}\
      </wps:DataInputs>\
	  ${responseForm}\
	</wps:Execute>';

var EXECUTE_REQUEST_XML_COMPLEX_DATA_ALL_INPUT = '<wps:Input>\
	      <ows:Identifier>${identifier}</ows:Identifier>\
	      <wps:Data>\
			<wps:ComplexData schema="${schema}" mimeType="${mimeType}" encoding="${encoding}">\
			${complexPayload}\
			</wps:ComplexData>\
	      </wps:Data>\
	</wps:Input>';

var EXECUTE_REQUEST_XML_COMPLEX_DATA_MIME_TYPE_INPUT = '<wps:Input>\
    <ows:Identifier>${identifier}</ows:Identifier>\
    <wps:Data>\
		<wps:ComplexData mimeType="${mimeType}">\
		${complexPayload}\
		</wps:ComplexData>\
    </wps:Data>\
</wps:Input>';

var EXECUTE_REQUEST_XML_COMPLEX_DATA_SCHEMA_INPUT = '<wps:Input>\
    <ows:Identifier>${identifier}</ows:Identifier>\
    <wps:Data>\
		<wps:ComplexData schema="${schema}" mimeType="${mimeType}">\
		${complexPayload}\
		</wps:ComplexData>\
    </wps:Data>\
</wps:Input>';

var EXECUTE_REQUEST_XML_COMPLEX_DATA_ENCODING_INPUT = '<wps:Input>\
    <ows:Identifier>${identifier}</ows:Identifier>\
    <wps:Data>\
		<wps:ComplexData mimeType="${mimeType}" encoding="${encoding}">\
		${complexPayload}\
		</wps:ComplexData>\
    </wps:Data>\
</wps:Input>';

var EXECUTE_REQUEST_XML_COMPLEX_DATA_BY_REFERENCE_INPUT = '<wps:Input>\
    <ows:Identifier>${identifier}</ows:Identifier>\
    <wps:Reference schema="${schema}" \
	xlink:href="${href}" method="${method}"/>\
  </wps:Input>';

var EXECUTE_REQUEST_XML_LITERAL_DATA_INPUT = '<wps:Input>\
    <ows:Identifier>${identifier}</ows:Identifier>\
    <wps:Data>\
      <wps:LiteralData dataType="${dataType}">${value}</wps:LiteralData>\
    </wps:Data>\
  </wps:Input>';

var EXECUTE_REQUEST_XML_LITERAL_DATA_NO_TYPE_INPUT = '<wps:Input>\
    <ows:Identifier>${identifier}</ows:Identifier>\
    <wps:Data>\
      <wps:LiteralData>${value}</wps:LiteralData>\
    </wps:Data>\
  </wps:Input>';

var EXECUTE_REQUEST_XML_BOUNDING_BOX_INPUT = '<wps:Input>\
    <ows:Identifier>${identifier}</ows:Identifier>\
    <wps:Data>\
       <wps:BoundingBoxData ows:crs="${crs}" ows:dimensions="${dimension}">\
          <ows:LowerCorner>${lowerCorner}</ows:LowerCorner>\
          <ows:UpperCorner>${upperCorner}</ows:UpperCorner>\
       </wps:BoundingBoxData>\
    </wps:Data>\
 </wps:Input>';

var EXECUTE_REQUEST_XML_RESPONSE_FORM_RAW = '<wps:ResponseForm>\
	    <wps:RawDataOutput mimeType="${mimeType}">\
	      <ows:Identifier>${identifier}</ows:Identifier>\
	    </wps:RawDataOutput>\
	  </wps:ResponseForm>';

var EXECUTE_REQUEST_XML_RESPONSE_FORM_DOCUMENT = '<wps:ResponseForm>\
    <wps:ResponseDocument storeExecuteResponse="${storeExecuteResponse}" \
	lineage="${lineage}" status="${status}">\
	${outputs}\
    </wps:ResponseDocument>\
  </wps:ResponseForm>';

var EXECUTE_REQUEST_XML_COMPLEX_ALL_OUTPUT = '<wps:Output \
	asReference="${asReference}" schema="${schema}" mimeType="${mimeType}" encoding="${encoding}">\
        <ows:Identifier>${identifier}</ows:Identifier>\
      </wps:Output>';

var EXECUTE_REQUEST_XML_COMPLEX_MIME_TYPE_OUTPUT = '<wps:Output \
	asReference="${asReference}" mimeType="${mimeType}">\
        <ows:Identifier>${identifier}</ows:Identifier>\
      </wps:Output>';

var EXECUTE_REQUEST_XML_COMPLEX_SCHEMA_OUTPUT = '<wps:Output \
	asReference="${asReference}" schema="${schema}" mimeType="${mimeType}">\
        <ows:Identifier>${identifier}</ows:Identifier>\
      </wps:Output>';

var EXECUTE_REQUEST_XML_COMPLEX_ENCODING_OUTPUT = '<wps:Output \
	asReference="${asReference}" mimeType="${mimeType}" encoding="${encoding}">\
        <ows:Identifier>${identifier}</ows:Identifier>\
      </wps:Output>';
	
var EXECUTE_REQUEST_XML_LITERAL_OUTPUT = '<wps:Output>\
    <ows:Identifier>${identifier}</ows:Identifier>\
  </wps:Output>';

var ExecuteRequest = PostRequest.extend({

	createPostPayload : function() {
		var inputs = this.settings.inputs;
		var outputs = this.settings.outputs;
		
		var dataInputsMarkup = "";
		if (inputs) {
			dataInputsMarkup = this.createDataInputsMarkup(inputs);
		}
		
		var responseFormMarkup = "";
		if (outputs) {
			responseFormMarkup = this.createResponseFormMarkup(outputs, this.settings.outputStyle);
		}
		
		var templateProperties = {
				processIdentifier: this.settings.processIdentifier,
				dataInputs: dataInputsMarkup,
				responseForm: responseFormMarkup
		};
		
		var result = this.fillTemplate(EXECUTE_REQUEST_XML_START, templateProperties);
		
		return result;
	},
	
	createDataInputsMarkup : function(inputs) {
		var result = "";
		for (var i = 0; i < inputs.length; i++) {
			var markup = "";
			if (equalsString("literal", inputs[i].type)) {
				markup = this.createLiteralDataInput(inputs[i]);
			}
			else if (equalsString("complex", inputs[i].type)) {
				markup = this.createComplexDataInput(inputs[i]);
			}
			else if (equalsString("bbox", inputs[i].type)) {
				markup = this.createBoundingBoxDataInput(inputs[i]);
			}
			result += markup;
		}
		
		return result;
	},
	
	/*
	 * example 'input' objects:
	 * 
	 * {
	 * identifier: "theInputId",
	 * value: "10.0",
	 * dataType: "xs:double"
	 * }
	 * 
	 * {
	 * identifier: "theInputId",
	 * value: "myStringValue"
	 * }
	 * 
	 */
	createLiteralDataInput : function(input) {
		var markup;
		if (input.dataType) {
			markup = this.fillTemplate(EXECUTE_REQUEST_XML_LITERAL_DATA_INPUT, input);
		}
		else {
			markup = this.fillTemplate(EXECUTE_REQUEST_XML_LITERAL_DATA_NO_TYPE_INPUT, input);
		}
		
		return markup;
	},
	
	/*
	 * example 'input' objects:
	 * 
	 * {
	 * identifier: "theProcessId",
	 * schema: "http://schema.xsd.url",
	 * complexPayload: "<heavy><xml><markup/></xml></heavy>"
	 * }
	 * 
	 * {
	 * identifier: "theProcessId",
	 * schema: "http://schema.xsd.url",
	 * href: "http://the.online.resource",
	 * method: "GET"
	 * }
	 * 
	 */
	createComplexDataInput : function(input) {
		var markup;
		if (input.href) {
			markup = this.fillTemplate(EXECUTE_REQUEST_XML_COMPLEX_DATA_BY_REFERENCE_INPUT, input);
		}
		else {
			if (input.schema && input.encoding) {
				markup = this.fillTemplate(EXECUTE_REQUEST_XML_COMPLEX_DATA_ALL_INPUT, input);
			}
			
			else if (input.schema && !input.encoding) {
				markup = this.fillTemplate(EXECUTE_REQUEST_XML_COMPLEX_DATA_SCHEMA_INPUT, input);
			}
			
			else if (!input.schema && input.encoding) {
				markup = this.fillTemplate(EXECUTE_REQUEST_XML_COMPLEX_DATA_ENCODING_INPUT, input);
			}
			
			else {
				markup = this.fillTemplate(EXECUTE_REQUEST_XML_COMPLEX_DATA_MIME_TYPE_INPUT, input);
			}
		}
		
		return markup;
	},
	
	/*
	 * example 'input' objects:
	 * 
	 * {
	 * identifier: "theInputId",
	 * crs: "EPSG:4236",
	 * dimension: 2,
	 * lowerCorner: "-10.0 40.5",
	 * upperCorner: "20.4 65.3",
	 * }
	 * 
	 * {
	 * identifier: "theInputId",
	 * value: "myStringValue"
	 * }
	 * 
	 */
	createBoundingBoxDataInput : function(input) {
		/*
		 * set some default values
		 */
		if (!input.crs) {
			input.crs = "EPSG:4326";
		}
		
		if (!input.dimension) {
			input.dimension = 2;
		}
		
		var markup = this.fillTemplate(EXECUTE_REQUEST_XML_BOUNDING_BOX_INPUT, input);
		
		return markup;
	},
	
	/*
	 * example 'outputStyle' objects:
	 * 
	 * {
	 *     storeExecuteResponse: true,
	 *     lineage: false,
	 *     status: true
	 * }
	 * 
	 * example 'outputs' objects:
	 * 
	 * [
	 * 	  {
	 * 		  identifier: "myComplexOutput1",
	 * 		  type: "complex",
	 * 		  asReference:false,
	 * 		  mimeType: "text/xml",
	 * 		  schema:"http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
	 *        encoding: "UTF-8"
	 * 	  },
	 * 	  {
	 * 		  identifier: "myLiteralOutput1",
	 * 		  type: "literal"
	 * 	  }
	 * ]
	 * 
	 */
	createResponseFormMarkup : function(outputs, outputStyle) {
		var outputString = "";
		for (var i = 0; i < outputs.length; i++) {
			if (equalsString("literal", outputs[i].type)) {
				outputString += this.fillTemplate(EXECUTE_REQUEST_XML_LITERAL_OUTPUT, outputs[i]);
			}
			else {
				if (outputs[i].encoding && outputs[i].schema) {
					outputString += this.fillTemplate(EXECUTE_REQUEST_XML_COMPLEX_ALL_OUTPUT, outputs[i]);
				}
				
				else if (outputs[i].encoding && !outputs[i].schema) {
					outputString += this.fillTemplate(EXECUTE_REQUEST_XML_COMPLEX_ENCODING_OUTPUT, outputs[i]);
				}
				
				else if (!outputs[i].encoding && outputs[i].schema) {
					outputString += this.fillTemplate(EXECUTE_REQUEST_XML_COMPLEX_SCHEMA_OUTPUT, outputs[i]);
				}
				
				else {
					outputString += this.fillTemplate(EXECUTE_REQUEST_XML_COMPLEX_MIME_TYPE_OUTPUT, outputs[i]);
				}
			}
		}
		
		outputStyle.outputs = outputString;
		
		var result = this.fillTemplate(EXECUTE_REQUEST_XML_RESPONSE_FORM_DOCUMENT, outputStyle);
		
		return result;
	}
	
});

var DescribeProcessGetRequest = GetRequest.extend({

	createTargetUrlQuery : function() {
		var result = "request=DescribeProcess&identifier="+this.settings.processIdentifier;
		
		return result;
	}
	
});

var GetCapabilitiesGetRequest = GetRequest.extend({

	createTargetUrlQuery : function() {
		return "request=GetCapabilities";
	}
	
});

var DESCRIBE_PROCESS_POST = '<DescribeProcess \
	xmlns="http://www.opengis.net/wps/1.0.0" \
	xmlns:ows="http://www.opengis.net/ows/1.1" \
	xmlns:xlink="http://www.w3.org/1999/xlink" \
	service="WPS" version="1.0.0">\
	${identifierList}\
</DescribeProcess>';

var DESCRIBE_PROCESS_POST_IDENTIFIER = '<ows:Identifier>${identifier}</ows:Identifier>';


var DescribeProcessPostRequest = PostRequest.extend({

	createPostPayload : function() {
		
		var processIdentifier = this.settings.processIdentifier;
		
		var idList = "";
		if (processIdentifier) {
			if (jQuery.isArray(processIdentifier)) {
				for (var i = 0; i < processIdentifier.length; i++) {
					idList += this.fillTemplate(DESCRIBE_PROCESS_POST_IDENTIFIER, {identifier: processIdentifier[i]});
				}
			}
			else {
				idList = this.fillTemplate(DESCRIBE_PROCESS_POST_IDENTIFIER, {identifier: processIdentifier});
			}
		}
		
		return this.fillTemplate(DESCRIBE_PROCESS_POST, {identifierList: idList});
	}
	
});

var GET_CAPABILITIES_POST = '<wps:GetCapabilities \
	xmlns:ows="http://www.opengis.net/ows/1.1" \
	xmlns:wps="http://www.opengis.net/wps/1.0.0" \
	xmlns:xlink="http://www.w3.org/1999/xlink" \
	service="WPS">\
    <wps:AcceptVersions>\
		<ows:Version>1.0.0</ows:Version>\
	</wps:AcceptVersions>\
	</wps:GetCapabilities>';


var GetCapabilitiesPostRequest = PostRequest.extend({

	createPostPayload : function() {
		return GET_CAPABILITIES_POST;
	}
	
});

var TEMPLATE_EXECUTE_COMPLEX_INPUTS_MARKUP = '\
	<div class="wps-execute-complex-inputs" id="input_${identifier}"> \
		<div class="wps-execute-response-process"> \
			<ul class="wps-execute-response-list"> \
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-input-item-label">${identifier}${required}</label>{{html inputField}}{{html asReference}}</li> \
				<li class="wps-execute-response-list-entry"> \
					{{html formats}}{{html copyButton}}</li> \
			</ul> \
		</div> \
	</div>';

var TEMPLATE_EXECUTE_COMPLEX_INPUTS_COPY_MARKUP = '\
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-input-item-label">${identifier}${required}</label>{{html inputField}}{{html asReference}}</li> \
				<li class="wps-execute-response-list-entry"> \
					{{html formats}}</li>';

var TEMPLATE_EXECUTE_LITERAL_INPUTS_MARKUP = '\
	<div class="wps-execute-literal-inputs" id="input_${identifier}"> \
		<div class="wps-execute-response-process"> \
			<ul class="wps-execute-response-list"> \
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-input-item-label">${identifier}${required}</label>{{html inputField}}{{html copyButton}}</li> \
			</ul> \
		</div> \
	</div>';
	
var TEMPLATE_EXECUTE_LITERAL_INPUTS_COPY_MARKUP = '\
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-input-item-label">${identifier}${required}</label>{{html inputField}}</li>';


var TEMPLATE_EXECUTE_BBOX_INPUTS_MARKUP = '\
	<div class="wps-execute-bbox-inputs" id="input_${identifier}"> \
		<div class="wps-execute-response-process"> \
			<ul class="wps-execute-response-list"> \
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-input-item-label">${identifier}${required}</label>{{html inputField}}<label>${description}</label>{{html copyButton}}</li> \
			</ul> \
		</div> \
	</div>';

var TEMPLATE_EXECUTE_BBOX_INPUTS_COPY_MARKUP = '\
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-input-item-label">${identifier}${required}</label>{{html inputField}}<label>${description}</label></li>';

var TEMPLATE_EXECUTE_OUTPUTS_MARKUP = '\
	<div class="wps-execute-complex-inputs"> \
		<div class="wps-execute-response-process"> \
			<ul class="wps-execute-response-list" id="outputs"> \
			</ul> \
		</div> \
	</div>';

var TEMPLATE_EXECUTE_COMPLEX_OUTPUTS_MARKUP = '\
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-input-item-label">${identifier}</label>{{html settings}}</li> \
				<li class="wps-execute-response-list-entry"> \
					{{html formats}}</li>';


var TEMPLATE_EXECUTE_LITERAL_OUTPUTS_MARKUP = '\
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-input-item-label">${identifier}</label>{{html settings}}</li>';

var TEMPLATE_EXECUTE_BBOX_OUTPUTS_MARKUP = '\
				<li class="wps-execute-response-list-entry"> \
					<label class="wps-input-item-label">${identifier}</label>{{html settings}}</li>';

var FormBuilder = Class.extend({
	
	init : function(settings) {
		this.settings = settings;
	},
	
	clearForm : function(targetDiv) {
		targetDiv.html('');
	},
	
	buildExecuteForm : function(targetDiv, processDescription, executeCallback) {
	 	var formElement = jQuery('<form id="wps-execute-form"></form>');
	 	formElement.submit(function() {
	 			executeCallback("wps-execute-form");
	        	return false;
	        });
	 	if(processDescription["abstract"] != null && processDescription["abstract"] != "null") {
	 		formElement.append(jQuery('<span id="abstract">' + processDescription["abstract"] + "</span>"));
	 	}
	 	formElement.append(this.createFormInputs(processDescription.dataInputs));
		formElement.append(this.createFormOutputs(processDescription));
	 	formElement.append(jQuery('<input type="hidden" name="processIdentifier" value="'+processDescription.identifier+'" />'));
	        
        var executeButton = jQuery("<button id=\"btn_execute\">Execute</button>");
        formElement.append(executeButton);
        
        targetDiv.append(jQuery("<div>").append(formElement));
	},
	
	createFormInputs : function(inputs){
	    
	    var container = jQuery('<div id="input"></div>');
	    var complexContainer = jQuery('<div id="complex-inputs"/>');
	    var literalContainer = jQuery('<div id="literal-inputs"/>');
	    var bboxContainer = jQuery('<div id="bbox-inputs"/>');
	    container.append(complexContainer);
	    container.append(literalContainer);
	    container.append(bboxContainer);
	    
	    var input;
	    for (var i=0; i < inputs.length; i++) {
	        input = inputs[i];    
	                
	        if (input.complexData) {    		    		
	        	this.createInput(input, container, TEMPLATE_EXECUTE_COMPLEX_INPUTS_MARKUP, TEMPLATE_EXECUTE_COMPLEX_INPUTS_COPY_MARKUP, "complex-inputs", this.createComplexInput);       	   
	        } else if (input.boundingBoxData) {            
	        	this.createInput(input, container, TEMPLATE_EXECUTE_BBOX_INPUTS_MARKUP, TEMPLATE_EXECUTE_BBOX_INPUTS_COPY_MARKUP, "bbox-inputs", this.createBoundingBoxInput);               
	        } else if (input.literalData) {
	        	this.createInput(input, container, TEMPLATE_EXECUTE_LITERAL_INPUTS_MARKUP, TEMPLATE_EXECUTE_LITERAL_INPUTS_COPY_MARKUP, "literal-inputs", this.createLiteralInput);
	        }
	    }
	    
	    return container;	
	},

	createInput : function(input, container, template, copyTemplate, inputParentId, propertyCreationFunction){

	    var templateProperties = propertyCreationFunction(input, this);
	    
	    if (input.minOccurs > 0) {
	        templateProperties.required = "*";
	    }
	    	
	    var name = input.identifier;
	    
	    var button = null;
	    if (input.maxOccurs > 1) {
	    
	    	var copyButtonDiv = jQuery("<div></div>"); 
	    
	    	button = this.addInputCopyButton(name);    	
	    
			copyButtonDiv.append(button);
			templateProperties.copyButton = copyButtonDiv.html();
	    }
	    
	    var result = jQuery.tmpl(template, templateProperties);
	    result.appendTo(container);
	              
	    if (input.maxOccurs > 1) {
	    
	    	if (button) {
	    		button.onclick = function() { 
	    			var templateProperties = this.createCopy(input, propertyCreationFunction);
	    		
	    			if (templateProperties) {				
	    				var inputsUl = jQuery('#'+inputParentId);
	    				jQuery.tmpl(copyTemplate, templateProperties).appendTo(inputsUl);
	    			}
	    		};	
	    	}
		
		}
	    
	    if (input.hidden) {
	    	result.css("display", "none");
	    }

	},

	// helper function for xml input
	createComplexInput : function(input, self) {
	    
	    var complexInputElements = {};
	    
	    var name = input.identifier;        
	    
	    var fieldDiv = jQuery("<div/>"); 
	    
	    var field = jQuery('<textarea class="wps-complex-input-textarea" title="'+ input["abstract"] +'"/>');
	    
	    var number = "";
	    
	    if(input.maxOccurs > 1){
	    	number = (input.occurrence || 1);
	    } 

	    var inputType;
	    var fieldName;
	    if(input.maxOccurs > 1){
	    	fieldName = "input_"+ name + number;
	    }else {
	    	fieldName = "input_"+ name;
	    }
	    
	    field.attr("name", fieldName);
		inputType = self.createInputTypeElement("complex", fieldName);
	    
	    field.attr("title", input["abstract"]);
	    
	    if (input.predefinedValue) {
	    	field.html(input.predefinedValue);
	    }
	    
	    fieldDiv.append(field);
	    fieldDiv.append(inputType);
	    
	    var labelText = "";
	    
	    if(input.maxOccurs > 1){
	    	labelText = input.identifier + "(" + number + "/" + input.maxOccurs + ")";
	    }else{
	    	labelText = input.identifier;
	    }
	        
	    var formats = input.complexData.supported.formats;
	    var formatDropBox = self.createFormatDropdown("format_"+fieldName, formats, input); 
	    
	    var formatDropBoxDiv = jQuery("<div />"); 
	      
	    formatDropBoxDiv.append(formatDropBox);
	      
	    var checkBoxDiv = jQuery('<div />'); 
	    
	    var checkBox = jQuery('<input type="checkbox" name="checkbox_'+fieldName + '" value="asReference" title="This input is a reference to the actual input."/>');
	    
	    checkBoxDiv.append(checkBox);
	    checkBoxDiv.append("asReference");
	    
	    complexInputElements.identifier = labelText;
	    complexInputElements.inputField = fieldDiv.html();
	    complexInputElements.asReference = checkBoxDiv.html();
	    complexInputElements.formats = formatDropBoxDiv.html();
	    
	    return complexInputElements;    
	},

	// helper function to create a literal input textfield or dropdown
	createLiteralInput : function(input, self) {
	    
	    var literalInputElements = {};        
	    
	    var fieldDiv = jQuery("<div />"); 
	    
	    var labelText = "";
	    
	    var number = "";
	    
	    if(input.maxOccurs > 1){
	    	number = (input.occurrence || 1);
	    } 
	    
	    var name = input.identifier;
	    
	    var fieldName = "input_"+ name + number;
	    var anyValue = input.literalData.anyValue;
	    // anyValue means textfield, otherwise we create a dropdown
	    var field = anyValue ? jQuery("<input />") : jQuery("<select />");    
	    
	    field.attr("name", fieldName);
	    var inputType = self.createInputTypeElement("literal", fieldName);

	    field.attr("title", input["abstract"]);
	    
	    fieldDiv.append(field);
	    fieldDiv.append(inputType);
	    
	    if(input.maxOccurs > 1){
	    	labelText = input.identifier + "(" + number + "/" + input.maxOccurs + ")";
	    }else{
	    	labelText = input.identifier;
	    }
	    
	    if (anyValue) {
	        var dataType = input.literalData.dataType;
	    } else {
	        var option = jQuery("<option />");
	        //option.innerHTML = name;
	        field.append(option);
	        for (var v in input.literalData.allowedValues) {
	            option = jQuery("<option />");
	            option.attr("value", v);
	            option.html(v);
	            
	            if (input.predefinedValue && equalsString(input.predefinedValue, v)) {
	            	option.attr("selected", true);
	            }
	            
	            field.append(option);
	        }
	    
	   		if(input.literalData.defaultValue){
	   			field.attr("value", input.literalData.defaultValue); 
	   		}
	    }
	    
	    if (input.predefinedValue) {
	    	if (anyValue) {
	    		field.attr("value", input.predefinedValue);
	    	}
	    }
	    
	    literalInputElements.identifier = labelText;
	    literalInputElements.inputField = fieldDiv.html();
	    
	    return literalInputElements; 
	},

	// helper function to dynamically create a bounding box input
	createBoundingBoxInput : function(input, self) {
	    
	    var bboxInputElements = {};        
	    
	    var fieldDiv = jQuery("<div />"); 
	    
	    var labelText = "";
	    
	    var number = "";
	    
	    if(input.maxOccurs > 1){
	    	number = (input.occurrence || 1);
	    } 
	    
	    var name = input.identifier;
	    
	    var fieldName = "input_"+ name + number;
	    var field = jQuery("<input />");
	    field.attr("title", input["abstract"]);

	    field.attr("name", fieldName);
	    var inputType = self.createInputTypeElement("bbox", fieldName);

	    field.attr("title", input["abstract"]);
	    
	    if (input.predefinedValue) {
	    	field.attr("value", input.predefinedValue);
	    }
	    
	    fieldDiv.append(field);
	    fieldDiv.append(inputType);
	    
	    if(input.maxOccurs > 1){
	    	labelText = input.identifier + "(" + number + "/" + input.maxOccurs + ")";
	    }else{
	    	labelText= input.identifier;
	    }
	    
	    bboxInputElements.identifier = labelText;
	    bboxInputElements.inputField = fieldDiv.html();
	    bboxInputElements.description = "left, bottom, right, top";
		
		return bboxInputElements;
	},

	createInputTypeElement : function(theType, theId) {
		var typeInput = jQuery('<input type="hidden" value="'+theType+'" name="type_'+theId+ '" />');
		
		return typeInput;
	},
	
	createFormatDrowpdownEntry : function(format) {
		var formatString = "";
		
		//if these exist, append with semicolon, else return empty string
    	var schema = format.schema;
    	var encoding = format.encoding;
    	var mimeType = format.mimeType;
    	
		//mimeType is mandatory, schema and encoding are not
    	if(schema && encoding){
    		formatString = mimeType + "; " + schema + "; " + encoding;
    	}else if(!schema && encoding){
    		formatString = mimeType + "; " + encoding;
    	}else if(schema && !encoding){
    		formatString = mimeType + "; " + schema;
    	}else{
    		formatString = mimeType;
    	}
    	
    	return formatString;
	},

	createFormOutputs : function(processDescription){
		var container = jQuery('<div id="output"></div>');
	    
		jQuery.tmpl(TEMPLATE_EXECUTE_OUTPUTS_MARKUP, "").appendTo(container);
		
		var outputsUl = jQuery('<ul id="outputs" class="wps-execute-response-list"/>');
		container.append(outputsUl);
		
		var outputs = processDescription.processOutputs;
		
		for (var i = 0; i < outputs.length; i++) {
			var output = outputs[i];
			
	    	var id = "output_"+output.identifier;
	    	
	    	var templateProperties = {};
	    	
	    	var template = null;
	    	
	    	var outputSettingsDiv = jQuery("<div />");
	    	
	    	var checkBox = jQuery('<input type="checkbox"/>');
	    	checkBox.attr("name", id);
	    	checkBox.attr("title", "Enable this output.");
	    	
	    	var typeField = this.createInputTypeElement(output.complexOutput ? "complex" : "literal", id);
	    	
	    	if (output.selected) {
	    		checkBox.attr("checked", "checked");
	    	}
	    	
	    	outputSettingsDiv.append(checkBox);
	    	outputSettingsDiv.append(typeField);
	    	
	    	templateProperties.identifier = id;
	    	templateProperties.settings = outputSettingsDiv.html();
	    	
	    	if (output.complexOutput) {
	    		var formats = output.complexOutput.supported.formats;
	    		var defaultFormat = output.complexOutput["default"].formats[0];
	    		
	    		var formatDropBox = this.createFormatDropdown("format_"+id, formats, output);
	    		
	    		// set the default as selected in the dropdown
	    		formatDropBox.val(JSON.stringify(defaultFormat));
	    		
	    		var formatDropBoxDiv = jQuery("<div />");
	    		
	    		formatDropBoxDiv.append(formatDropBox);
	    		    		
	    		// FIXME this looses the selection again!
	    		templateProperties.formats = formatDropBoxDiv.html();
	    		
	    		template = TEMPLATE_EXECUTE_COMPLEX_OUTPUTS_MARKUP;
	    		
	    	} else if (output.literalOutput) {
	    		
	    		template = TEMPLATE_EXECUTE_LITERAL_OUTPUTS_MARKUP;
	    	
	    	} else if (output.boundingBoxOutput) {
	    		
	    		template = TEMPLATE_EXECUTE_BBOX_OUTPUTS_MARKUP;    	
	    	}
	    	
	    	if (template) {
	    		jQuery.tmpl(template, templateProperties).appendTo(outputsUl);
	    	}
		}
		
		return container;
	},

	createFormatDropdown : function(id, formats, input){

	    // anyValue means textfield, otherwise we create a dropdown
	    var field = jQuery('<select name="'+id+'" title="'+input["abstract"]+'"/>');
	    
	    var option;
	    for (var i = 0; i < formats.length; i++) {
	    	var format = formats[i];
	    	
	    	var formatString = this.createFormatDrowpdownEntry(format);

	    	option = jQuery('<option>'+formatString+'</option>');
	        option.val(JSON.stringify(format));
	        field.append(option);
	    }
		return field;
	},

	addInputCopyButton : function(id){
		var button = jQuery('<button class="add-input-copy" id="'+id+'-copy-button" />');
		return button;
	},

	// if maxOccurs is > 1, this will add a copy of the field
	createCopy : function(input, propertyCreationFunction) {
	    if (input.maxOccurs && input.maxOccurs > 1) {
	        // add another copy of the field - check maxOccurs
	        if(input.occurrence && input.occurrence >= input.maxOccurs){
	        	return;
	        }
	        var newInput = jQuery.extend({}, input);
	        // we recognize copies by the occurrence property
	        input.occurrence = (input.occurrence || 1) + 1;
	        newInput.occurrence = input.occurrence;
	        return propertyCreationFunction(newInput);
	    }
	}
	
});

var FormParser = Class.extend({
	
	init : function(settings) {
		this.settings = settings;
	},
	
	parseInputs : function(formValues) {
		var inputs = [];
		var inputNameToPosition = {};
		
		for (var i = 0; i < formValues.length; i++) {
			var prop = formValues[i];
			if (stringStartsWith(prop.name, "input_")) {
				// only add input elements with non-emtpy value:
				if(prop.value) {
					var j = inputs.length;
					inputs[j] = {};
					inputs[j].identifier = prop.name.substring(6, prop.name.length);
					inputs[j].value = prop.value;
					inputNameToPosition[prop.name] = j;
				}
			}
		}
		
		/*
		 * look for each input's type
		 */
		for (var i = 0; i < formValues.length; i++) {
			var prop = formValues[i];
			if (stringStartsWith(prop.name, "type_input")) {
				var originalInputName = prop.name.substring(5, prop.name.length);
				
				// check if input is set before setting the type
				if(inputNameToPosition[originalInputName] != null) {
					inputs[inputNameToPosition[originalInputName]].type = prop.value;
					
					if (stringStartsWith(prop.value, "complex")) {
						inputs[inputNameToPosition[originalInputName]].complexPayload = inputs[inputNameToPosition[originalInputName]].value;
					}
					else if (stringStartsWith(prop.value, "bbox")) {
						this.parseBboxValue(inputs[inputNameToPosition[originalInputName]].value, inputs[inputNameToPosition[originalInputName]]);
					}
				}
			}
		}
		
		/*
		 * check asReference flag
		 */
		for (var i = 0; i < formValues.length; i++) {
			var prop = formValues[i];
			if (stringStartsWith(prop.name, "checkbox_input")) {
				var originalInputName = prop.name.substring(9, prop.name.length);
				/*
				 * its only present in the array if checked
				 */
				inputs[inputNameToPosition[originalInputName]].asReference = true;
			}
		}
		
		/*
		 * look for each input's format
		 */
		for (var i = 0; i < formValues.length; i++) {
			var prop = formValues[i];
			if (stringStartsWith(prop.name, "format_input")) {
				var originalInputName = prop.name.substring(7, prop.name.length);
				var formatObject = JSON.parse(prop.value);
				this.parseFormatObject(formatObject, inputs[inputNameToPosition[originalInputName]]);
			}
		}
		
		return inputs;
	},
	
	parseBboxValue : function(bboxString, targetObject) {
		var array = bboxString.split(",");

		if (array.length < 4) {
			for (var i = array.length; i < 4; i++) {
				/*
				 * bad input, fill it with zero
				 * TODO: do validation in prior
				 */
				array[i] = "0.0";
			}
		}
		
		targetObject.lowerCorner = jQuery.trim(array[0]) + " " + jQuery.trim(array[1]);
		targetObject.upperCorner = jQuery.trim(array[2]) + " " + jQuery.trim(array[3]);
	},
	
	parseOutputs : function(formValues) {
		var outputs = [];
		var outputNameToPosition = {};
		
		for (var i = 0; i < formValues.length; i++) {
			var prop = formValues[i];
			if (stringStartsWith(prop.name, "output_")) {
				var j = outputs.length;
				outputs[j] = {};
				outputs[j].identifier = prop.name.substring(7, prop.name.length);

				//TODO: currently not supported in the form
				outputs[j].asReference = false;
				outputNameToPosition[prop.name] = j;
			}
		}
		
		/*
		 * look for each outputs type
		 */
		for (var i = 0; i < formValues.length; i++) {
			var prop = formValues[i];
			if (stringStartsWith(prop.name, "type_output")) {
				var originalName = prop.name.substring(5, prop.name.length);
				
				// only set output properties for selected outputs
				if(outputNameToPosition[originalName] != null) {
					outputs[outputNameToPosition[originalName]].type = prop.value;
					
					//TODO: set via form
					if (stringStartsWith(prop.value, "complex")) {
						outputs[outputNameToPosition[originalName]].asReference = true;
					}
				}
			}
		}
		
		/*
		 * look for each outputs format
		 */
		for (var i = 0; i < formValues.length; i++) {
			var prop = formValues[i];
			if (stringStartsWith(prop.name, "format_output")) {
				var originalName = prop.name.substring(7, prop.name.length);
				var formatObject = JSON.parse(prop.value);
				
				if(outputNameToPosition[originalName] != null) {
					this.parseFormatObject(formatObject, outputs[outputNameToPosition[originalName]]);
				}
			}
		}
		
		return outputs;
	},
	
	parseFormatObject : function(formatObject, targetObject) {
		if (formatObject.mimeType) {
			targetObject.mimeType = formatObject.mimeType;
		}
		
		if (formatObject.schema) {
			targetObject.schema = formatObject.schema;
		}
		
		if (formatObject.encoding) {
			targetObject.encoding = formatObject.encoding;
		}
	},
	
	parseProcessIdentifier : function(formValues) {
		for (var i = 0; i < formValues.length; i++) {
			var prop = formValues[i];
			if (equalsString(prop.name, "processIdentifier")) {
				return prop.value;
			}
		}
		return "";
	},
	
	parseOutputStyle : function(formValues) {
		return {
			   storeExecuteResponse: true,
			   lineage: false,
			   status: true
		   };
	}

});
var wps;

function resolveRequest(type, method, settings) {
	if (type == GET_CAPABILITIES_TYPE) {
		return new GetCapabilitiesGetRequest(settings);
	}
	else if (type == DESCRIBE_PROCESS_TYPE) {
		return new GetRequest(settings);
	}
	else if (type == EXECUTE_TYPE && method == METHOD_POST) {
		return new PostRequest(settings);
	}
	
	return new GetRequest(settings);
}

function resolveGetParameters() {
	var params = jQuery.url();
	var url = params.param(PARAM_WPS_REQUEST_URL);
	var requestType = params.param(PARAM_WPS_REQUEST_TYPE);
	
	if (url && requestType) {
		return {url: decodeURIComponent((url+'').replace(/\+/g, '%20')), requestType: requestType};
	}

	return null;
}

function assertValidState(settings) {
	var dataValid = true;
	if (settings.method == METHOD_POST) {
		dataValid = jQuery.isXMLDoc(settings.data);
	}
	return settings.url && settings.requestType && settings.method && dataValid;
}

function callbackOnResponseParsed(responseData, domElement, originalRequest) {
	var factory = new ResponseFactory();
	var responseHandler = factory.resolveResponseHandler(responseData, originalRequest);
	
	if (responseHandler) {
		domElement.html(responseHandler.createMarkup());
	}
	
	if (originalRequest.updateSwitch) {
		if (!originalRequest.updateSwitch.callback) {
			originalRequest.updateSwitch.callback = callbackOnResponseParsed;
		}
		if (!originalRequest.updateSwitch.element) {
			originalRequest.updateSwitch.element = "wps-execute-autoUpdate";
		}
		
		jQuery('#'+originalRequest.updateSwitch.element).click(function() {
			
			var updateSwitch = originalRequest.updateSwitch;
			
			var getSettings = {
				url : originalRequest.settings.url,
				requestType : originalRequest.settings.requestType,
				type : "GET",
				domElement : originalRequest.settings.domElement
			};
			
			originalRequest = new GetRequest(getSettings);
			originalRequest.execute(updateSwitch.callback, updateSwitch);
		});
		jQuery('#'+originalRequest.updateSwitch.element).css( 'cursor', 'pointer' );
	}
}

function removePocessesFromSelectFast(targetDomElement){
	var selectObj = document.getElementById(targetDomElement);
	var selectParentNode = selectObj.parentNode;
	var newSelectObj = selectObj.cloneNode(false); // Make a shallow copy
	selectParentNode.replaceChild(newSelectObj, selectObj);
    
    var option = document.createElement("option");
    option.innerHTML = "Select a process";
    option.value = "Select a process";
    newSelectObj.appendChild(option);	
    newSelectObj.onchange = selectObj.onchange;
	return newSelectObj;
}

// using OpenLayers.Format.WPSCapabilities to read the capabilities
// and fill available process list
function getCapabilities(wpsUrl) {
	var processesDropdown = "processes";
    
	jQuery.wpsSetup({configuration : {url : wpsUrl}});
	// wps = this.options[this.selectedIndex].value;
    
    removePocessesFromSelectFast(processesDropdown);
    
    var getCap = new GetCapabilitiesGetRequest({
    	url : wps.getServiceUrl()
    });
    
    getCap.execute(function(response, targetDomElement, originalRequest, updateSwitch) {
    	// TODO read response with GetCapabilitiesResponse.js instead of
		// OpenLayers
        capabilities = new OpenLayers.Format.WPSCapabilities().read(
                response);
        var dropdown = document.getElementById(processesDropdown);
        var offerings = capabilities.processOfferings, option;
        // populate the dropdown
        // TODO extract populating the dropbown - this function should allow any
		// output for the processes. maybe just return the parsed offerings? or
		// accept an offeringCallback!
        for (var p in offerings) {
            option = document.createElement("option");
            option.innerHTML = offerings[p].identifier;
            option.value = p;
            dropdown.appendChild(option);				
        }
        
    	jQuery("#"+processesDropdown).each(function() {
	    	var selectedValue = jQuery(this).val();
	    	// Sort all options by text
	    	jQuery(this).html(jQuery("option", jQuery(this)).sort(function(a, b) {
	    		return a.text.toUpperCase() == b.text.toUpperCase() ? 0 : a.text.toUpperCase() < b.text.toUpperCase() ? -1 : 1;
	    	}));
	    	jQuery(this).val(selectedValue);
    	});
    });
    
}

// using OpenLayers.Format.WPSDescribeProcess to get information about a
// process
function describeProcess(processIdentifier, wpsUrl, targetContainer) {
	if (!processIdentifier) {
		processIdentifier = this.options[this.selectedIndex].value;		
	}
	
	if (!wpsUrl) {
		wpsUrl = wps.getServiceUrl();
	}
	
	if (!targetContainer) {
		targetContainer = "wps-execute-container";
	}
    
    var describeProcess = new DescribeProcessGetRequest({
    	url : wpsUrl,
    	processIdentifier: processIdentifier
    });
    
    // build form for execute
    describeProcess.execute(function(response, targetDomElement, originalRequest, updateSwitch) {
    		// TODO read response with DescribeProcessResponse.js
            var parsed = new OpenLayers.Format.WPSDescribeProcess().read(
                response
            );
            
            var process = parsed.processDescriptions[processIdentifier];
            
            var formBuilder = new FormBuilder();
            formBuilder.clearForm(jQuery('#'+targetContainer));
            formBuilder.buildExecuteForm(jQuery('#'+targetContainer), process, execute);
            
            // create a link to the full process description
            var processDescriptionLink = jQuery('<a title="Full process description" target="_blank">Show Description</a>');
            processDescriptionLink.attr("href", describeProcess.settings.url);
            jQuery('#'+targetContainer).prepend(jQuery('<div class="wps-description-link">').append(processDescriptionLink));
            
            // create links to the metadata elements
            var metadata = jQuery(response.getElementsByTagNameNS(OWS_11_NAMESPACE, "Metadata"));
            if(metadata.length > 0) {
	            var formMetadata = jQuery('<div class="wps-description-metadata">');
	            formMetadata.append("<span>Metadata</span>");
	            metadata.each(function(index, value) {
	            	var m = jQuery(value);
	            	formMetadata.append(jQuery("<span class=\"wps-metadata-link\"><a id=\"wps-description-metadata-" + index + "\" href=\"" + m.attr("xlin:href") + "\">" + m.attr("xlin:title") + "</a></span>"));
	    		});
	            jQuery('#'+targetContainer).prepend(formMetadata);
            }
        });
    
}

// execute the process
function execute(formId, wpsUrl) {
    var formValues = jQuery('#'+formId).serializeArray();
    
    var parser = new FormParser();
    var inputs = parser.parseInputs(formValues);
    var outputs = parser.parseOutputs(formValues);
    var processIdentifier = parser.parseProcessIdentifier(formValues);
    var outputStyle = parser.parseOutputStyle(formValues);
    
    if (!wpsUrl) {
    	wpsUrl = wps.getServiceUrl();
    }
    
    var settings = {
			url: wpsUrl,
			inputs: inputs,
			outputs: outputs,
			outputStyle: outputStyle,
			processIdentifier: processIdentifier,
			domElement: jQuery('#executeProcess')
	};

	var originalRequest = new ExecuteRequest(settings);

	originalRequest.execute(callbackOnResponseParsed, {});
}

/*
 * jQuery plugin definitions
 */
(function(jQuery) {

	jQuery.fn.extend({
		wpsCall : function( options ) {
	    	var settings;
	    	if (options && options.viaUrl) {
	    		/*
				 * Call via GET parameters
				 */
	    		settings = jQuery.extend(resolveGetParameters(), {method: METHOD_GET});
	    	}
	    	else {
	            /*
				 * Custom User Call
				 */
	            settings = jQuery.extend({
	                method: METHOD_GET
	            }, options);
	    	}
	    	
	    	if (assertValidState(settings)) {
	    		return this.each( function() {
	            	var requestSettings = jQuery.extend({
	                    domElement: jQuery(this)
	                }, settings);
	            	
	            	var request = resolveRequest(requestSettings.requestType, requestSettings.method,
	            			requestSettings);
	            	request.execute(callbackOnResponseParsed, options.updateSwitch);
	            });	
	    	}
	    	
	    	return this.each();
	    }
	});
	
	jQuery.extend({
		wpsSetup : function(setup) {
			if (setup.reset) {
				wpsResetSetup();
				return;
			}
			
	    	if (setup.templates) {
	    		var templates = setup.templates;
	    		if (templates.capabilities) {
	    			if (typeof templates.capabilities == 'string') {
	    				USER_TEMPLATE_CAPABILITIES_MARKUP = templates.capabilities;
	    			}
	        	}
	        	if (templates.processDescription) {
	        		if (typeof templates.processDescription == 'string') {
	        			USER_TEMPLATE_PROCESS_DESCRIPTION_MARKUP = templates.processDescription;
	        		}
	        	}
	        	if (templates.executeResponse) {
	        		if (typeof templates.executeResponse == 'string') {
	        			USER_TEMPLATE_EXECUTE_RESPONSE_MARKUP = templates.executeResponse;
	        		}
	        	}
	    	}
	    	
	    	if (setup.proxy) {
	    		USE_PROXY = true;
	    		PROXY_URL = setup.proxy.url;
	    		/*
				 * setup OpenLayers to use the proxy as well
				 */
	    		if (OpenLayers) {
	    			OpenLayers.ProxyHost = setup.proxy.url;
	    		}
	    		PROXY_TYPE = setup.proxy.type;
	    	}
	    	
	    	if (setup.configuration) {
	    		wps = new WPSConfiguration(setup.configuration);
	    	}

		}
	});
    
}(jQuery));

