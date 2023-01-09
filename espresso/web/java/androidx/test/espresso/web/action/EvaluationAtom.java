package androidx.test.espresso.web.action;
// GENERATED CODE DO NOT EDIT
final class EvaluationAtom {
/* field: EXECUTE_SCRIPT_ANDROID license: 

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.


 Copyright The Closure Library Authors.
 SPDX-License-Identifier: Apache-2.0
 */
  static final String EXECUTE_SCRIPT_ANDROID =
    "function(){return(function(){/*\n\n Copyright The Closure Library Au" +
    "thors.\n SPDX-License-Identifier: Apache-2.0\n*/\nvar k=this||self;fu" +
    "nction l(a){var b=typeof a;return\"object\"!=b?b:a?Array.isArray(a)?" +
    "\"array\":b:\"null\"}function m(a){var b=l(a);return\"array\"==b||\"objec" +
    "t\"==b&&\"number\"==typeof a.length}function n(a){var b=typeof a;retu" +
    "rn\"object\"==b&&null!=a||\"function\"==b}\nfunction p(a,b){function c(" +
    "){}c.prototype=b.prototype;a.j=b.prototype;a.prototype=new c;a.pro" +
    "totype.constructor=a;a.i=function(d,e,f){for(var g=Array(arguments" +
    ".length-2),h=2;h<arguments.length;h++)g[h-2]=arguments[h];return b" +
    ".prototype[e].apply(d,g)}};function q(a,b){for(var c=a.length,d=Ar" +
    "ray(c),e=\"string\"===typeof a?a.split(\"\"):a,f=0;f<c;f++)f in e&&(d[" +
    "f]=b.call(void 0,e[f],f,a));return d};function r(a,b){var c={},d;f" +
    "or(d in a)b.call(void 0,a[d],d,a)&&(c[d]=a[d]);return c}function t" +
    "(a,b){var c={},d;for(d in a)c[d]=b.call(void 0,a[d],d,a);return c}" +
    "function u(a,b){return null!==a&&b in a}function v(a,b){for(var c " +
    "in a)if(b.call(void 0,a[c],c,a))return c};var w=String.prototype.t" +
    "rim?function(a){return a.trim()}:function(a){return/^[\\s\\xa0]*([\\s" +
    "\\S]*?)[\\s\\xa0]*$/.exec(a)[1]};function x(a,b){return a<b?-1:a>b?1:" +
    "0};function y(){var a=k.navigator;return a&&(a=a.userAgent)?a:\"\"};" +
    "/*\n\n Copyright 2014 Software Freedom Conservancy\n\n Licensed under " +
    "the Apache License, Version 2.0 (the \"License\");\n you may not use " +
    "this file except in compliance with the License.\n You may obtain a" +
    " copy of the License at\n\n      http://www.apache.org/licenses/LICE" +
    "NSE-2.0\n\n Unless required by applicable law or agreed to in writin" +
    "g, software\n distributed under the License is distributed on an \"A" +
    "S IS\" BASIS,\n WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either" +
    " express or implied.\n See the License for the specific language go" +
    "verning permissions and\n limitations under the License.\n*/\nvar z=w" +
    "indow;function A(a,b){this.code=a;this.h=B[a]||\"unknown error\";thi" +
    "s.message=b||\"\";a=this.h.replace(/((?:^|\\s+)[a-z])/g,function(c){r" +
    "eturn c.toUpperCase().replace(/^[\\s\\xa0]+/g,\"\")});b=a.length-5;if(" +
    "0>b||a.indexOf(\"Error\",b)!=b)a+=\"Error\";this.name=a;a=Error(this.m" +
    "essage);a.name=this.name;this.stack=a.stack||\"\"}p(A,Error);\nvar B=" +
    "{15:\"element not selectable\",11:\"element not visible\",31:\"unknown " +
    "error\",30:\"unknown error\",24:\"invalid cookie domain\",29:\"invalid e" +
    "lement coordinates\",12:\"invalid element state\",32:\"invalid selecto" +
    "r\",51:\"invalid selector\",52:\"invalid selector\",17:\"javascript erro" +
    "r\",405:\"unsupported operation\",34:\"move target out of bounds\",27:\"" +
    "no such alert\",7:\"no such element\",8:\"no such frame\",23:\"no such w" +
    "indow\",28:\"script timeout\",33:\"session not created\",10:\"stale elem" +
    "ent reference\",21:\"timeout\",25:\"unable to set cookie\",\n26:\"unexpec" +
    "ted alert open\",13:\"unknown error\",9:\"unknown command\"};A.prototyp" +
    "e.toString=function(){return this.name+\": \"+this.message};function" +
    " C(){}\nfunction D(a,b,c){if(null==b)c.push(\"null\");else{if(\"object" +
    "\"==typeof b){if(Array.isArray(b)){var d=b;b=d.length;c.push(\"[\");f" +
    "or(var e=\"\",f=0;f<b;f++)c.push(e),D(a,d[f],c),e=\",\";c.push(\"]\");re" +
    "turn}if(b instanceof String||b instanceof Number||b instanceof Boo" +
    "lean)b=b.valueOf();else{c.push(\"{\");e=\"\";for(d in b)Object.prototy" +
    "pe.hasOwnProperty.call(b,d)&&(f=b[d],\"function\"!=typeof f&&(c.push" +
    "(e),E(d,c),c.push(\":\"),D(a,f,c),e=\",\"));c.push(\"}\");return}}switch" +
    "(typeof b){case \"string\":E(b,c);break;case \"number\":c.push(isFinit" +
    "e(b)&&\n!isNaN(b)?String(b):\"null\");break;case \"boolean\":c.push(Str" +
    "ing(b));break;case \"function\":c.push(\"null\");break;default:throw E" +
    "rror(\"Unknown type: \"+typeof b);}}}var F={'\"':'\\\\\"',\"\\\\\":\"\\\\\\\\\",\"/" +
    "\":\"\\\\/\",\"\\b\":\"\\\\b\",\"\\f\":\"\\\\f\",\"\\n\":\"\\\\n\",\"\\r\":\"\\\\r\",\"\\t\":\"\\\\t\",\"\\v" +
    "\":\"\\\\u000b\"},G=/\\uffff/.test(\"\\uffff\")?/[\\\\\"\\x00-\\x1f\\x7f-\\uffff]/" +
    "g:/[\\\\\"\\x00-\\x1f\\x7f-\\xff]/g;function E(a,b){b.push('\"',a.replace(" +
    "G,function(c){var d=F[c];d||(d=\"\\\\u\"+(c.charCodeAt(0)|65536).toStr" +
    "ing(16).slice(1),F[c]=d);return d}),'\"')};function H(a){return(a=a" +
    ".exec(y()))?a[1]:\"\"}H(/Android\\s+([0-9.]+)/)||H(/Version\\/([0-9.]+" +
    ")/);function I(a){var b=0,c=w(String(J)).split(\".\");a=w(String(a))" +
    ".split(\".\");for(var d=Math.max(c.length,a.length),e=0;0==b&&e<d;e+" +
    "+){var f=c[e]||\"\",g=a[e]||\"\";do{f=/(\\d*)(\\D*)(.*)/.exec(f)||[\"\",\"\"" +
    ",\"\",\"\"];g=/(\\d*)(\\D*)(.*)/.exec(g)||[\"\",\"\",\"\",\"\"];if(0==f[0].lengt" +
    "h&&0==g[0].length)break;b=x(0==f[1].length?0:parseInt(f[1],10),0==" +
    "g[1].length?0:parseInt(g[1],10))||x(0==f[2].length,0==g[2].length)" +
    "||x(f[2],g[2]);f=f[3];g=g[3]}while(0==b)}}var K=/Android\\s+([0-9\\." +
    "]+)/.exec(y()),J=K?K[1]:\"0\";I(2.3);I(4);function L(a){function b(c" +
    ",d){switch(l(c)){case \"string\":case \"number\":case \"boolean\":return" +
    " c;case \"function\":return c.toString();case \"array\":return q(c,fun" +
    "ction(f){return b(f,d)});case \"object\":if(0<=d.indexOf(c))throw ne" +
    "w A(17,\"Recursive object cannot be transferred\");if(u(c,\"nodeType\"" +
    ")&&(1==c.nodeType||9==c.nodeType)){var e={};e.ELEMENT=M(c);return " +
    "e}if(u(c,\"document\"))return e={},e.WINDOW=M(c),e;d.push(c);if(m(c)" +
    ")return q(c,function(f){return b(f,d)});c=r(c,function(f,g){return" +
    "\"number\"===typeof g||\n\"string\"===typeof g});return t(c,function(f)" +
    "{return b(f,d)});default:return null}}return b(a,[])}function N(a," +
    "b){return Array.isArray(a)?q(a,function(c){return N(c,b)}):n(a)?\"f" +
    "unction\"==typeof a?a:u(a,\"ELEMENT\")?O(a.ELEMENT,b):u(a,\"WINDOW\")?O" +
    "(a.WINDOW,b):t(a,function(c){return N(c,b)}):a}function P(a){a=a||" +
    "document;var b=a.$wdc_;b||(b=a.$wdc_={},b.g=Date.now());b.g||(b.g=" +
    "Date.now());return b}\nfunction M(a){var b=P(a.ownerDocument),c=v(b" +
    ",function(d){return d==a});c||(c=\":wdc:\"+b.g++,b[c]=a);return c}\nf" +
    "unction O(a,b){a=decodeURIComponent(a);b=b||document;var c=P(b);if" +
    "(!u(c,a))throw new A(10,\"Element does not exist in cache\");var d=c" +
    "[a];if(u(d,\"setInterval\")){if(d.closed)throw delete c[a],new A(23," +
    "\"Window has been closed.\");return d}for(var e=d,f=!!HTMLElement.pr" +
    "ototype.attachShadow;e;){if(e==b.documentElement)return d;f&&e ins" +
    "tanceof ShadowRoot?e.host.shadowRoot!==e?e=null:e=e.host:e=e.paren" +
    "tNode}delete c[a];throw new A(10,\"Element is no longer attached to" +
    " the DOM\");};function Q(a,b,c,d){d=d||z;try{a:{var e=a;if(\"string\"" +
    "===typeof e)try{a=new d.Function(e);break a}catch(h){throw h;}a=d=" +
    "=window?e:new d.Function(\"return (\"+e+\").apply(null,arguments);\")}" +
    "var f=N(b,d.document);var g={status:0,value:L(a.apply(null,f))}}ca" +
    "tch(h){g={status:u(h,\"code\")?h.code:13,value:{message:h.message}}}" +
    "c&&(a=[],D(new C,g,a),g=a.join(\"\"));return g}var R=[\"_\"],S=k;R[0]i" +
    "n S||\"undefined\"==typeof S.execScript||S.execScript(\"var \"+R[0]);\n" +
    "for(var T;R.length&&(T=R.shift());)R.length||void 0===Q?S[T]&&S[T]" +
    "!==Object.prototype[T]?S=S[T]:S=S[T]={}:S[T]=Q;;return this._.appl" +
    "y(null,arguments);}).apply({navigator:typeof window!=\"undefined\"?w" +
    "indow.navigator:null},arguments);}\n";
  static final String EXECUTE_SCRIPT_ANDROID_license =
    "\n\n Copyright 2014 Software Freedom Conservancy\n\n Licensed under th" +
    "e Apache License, Version 2.0 (the \"License\");\n you may not use th" +
    "is file except in compliance with the License.\n You may obtain a c" +
    "opy of the License at\n\n      http://www.apache.org/licenses/LICENS" +
    "E-2.0\n\n Unless required by applicable law or agreed to in writing," +
    " software\n distributed under the License is distributed on an \"AS " +
    "IS\" BASIS,\n WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either e" +
    "xpress or implied.\n See the License for the specific language gove" +
    "rning permissions and\n limitations under the License.\n\n\n Copyright" +
    " The Closure Library Authors.\n SPDX-License-Identifier: Apache-2.0" +
    "\n";
  private static final String EXECUTE_SCRIPT_ANDROID_original() {
    return EXECUTE_SCRIPT_ANDROID.replaceAll("xxx_rpl_lic", EXECUTE_SCRIPT_ANDROID_license);
  }

/* field: GET_ELEMENT_ANDROID license: 

 Copyright 2014 Software Freedom Conservancy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.


 Copyright The Closure Library Authors.
 SPDX-License-Identifier: Apache-2.0
 */
  static final String GET_ELEMENT_ANDROID =
    "function(){return(function(){/*\n\n Copyright The Closure Library Au" +
    "thors.\n SPDX-License-Identifier: Apache-2.0\n*/\nvar h=this||self;fu" +
    "nction k(a,b){function e(){}e.prototype=b.prototype;a.j=b.prototyp" +
    "e;a.prototype=new e;a.prototype.constructor=a;a.i=function(c,d,f){" +
    "for(var g=Array(arguments.length-2),n=2;n<arguments.length;n++)g[n" +
    "-2]=arguments[n];return b.prototype[d].apply(c,g)}};var l=String.p" +
    "rototype.trim?function(a){return a.trim()}:function(a){return/^[\\s" +
    "\\xa0]*([\\s\\S]*?)[\\s\\xa0]*$/.exec(a)[1]};function m(a,b){return a<b" +
    "?-1:a>b?1:0};function p(){var a=h.navigator;return a&&(a=a.userAge" +
    "nt)?a:\"\"};/*\n\n Copyright 2014 Software Freedom Conservancy\n\n Licen" +
    "sed under the Apache License, Version 2.0 (the \"License\");\n you ma" +
    "y not use this file except in compliance with the License.\n You ma" +
    "y obtain a copy of the License at\n\n      http://www.apache.org/lic" +
    "enses/LICENSE-2.0\n\n Unless required by applicable law or agreed to" +
    " in writing, software\n distributed under the License is distribute" +
    "d on an \"AS IS\" BASIS,\n WITHOUT WARRANTIES OR CONDITIONS OF ANY KI" +
    "ND, either express or implied.\n See the License for the specific l" +
    "anguage governing permissions and\n limitations under the License.\n" +
    "*/\nfunction q(a,b){this.code=a;this.h=r[a]||\"unknown error\";this.m" +
    "essage=b||\"\";a=this.h.replace(/((?:^|\\s+)[a-z])/g,function(e){retu" +
    "rn e.toUpperCase().replace(/^[\\s\\xa0]+/g,\"\")});b=a.length-5;if(0>b" +
    "||a.indexOf(\"Error\",b)!=b)a+=\"Error\";this.name=a;a=Error(this.mess" +
    "age);a.name=this.name;this.stack=a.stack||\"\"}k(q,Error);\nvar r={15" +
    ":\"element not selectable\",11:\"element not visible\",31:\"unknown err" +
    "or\",30:\"unknown error\",24:\"invalid cookie domain\",29:\"invalid elem" +
    "ent coordinates\",12:\"invalid element state\",32:\"invalid selector\"," +
    "51:\"invalid selector\",52:\"invalid selector\",17:\"javascript error\"," +
    "405:\"unsupported operation\",34:\"move target out of bounds\",27:\"no " +
    "such alert\",7:\"no such element\",8:\"no such frame\",23:\"no such wind" +
    "ow\",28:\"script timeout\",33:\"session not created\",10:\"stale element" +
    " reference\",21:\"timeout\",25:\"unable to set cookie\",\n26:\"unexpected" +
    " alert open\",13:\"unknown error\",9:\"unknown command\"};q.prototype.t" +
    "oString=function(){return this.name+\": \"+this.message};function t(" +
    "a){return(a=a.exec(p()))?a[1]:\"\"}t(/Android\\s+([0-9.]+)/)||t(/Vers" +
    "ion\\/([0-9.]+)/);function u(a){var b=0,e=l(String(v)).split(\".\");a" +
    "=l(String(a)).split(\".\");for(var c=Math.max(e.length,a.length),d=0" +
    ";0==b&&d<c;d++){var f=e[d]||\"\",g=a[d]||\"\";do{f=/(\\d*)(\\D*)(.*)/.ex" +
    "ec(f)||[\"\",\"\",\"\",\"\"];g=/(\\d*)(\\D*)(.*)/.exec(g)||[\"\",\"\",\"\",\"\"];if(" +
    "0==f[0].length&&0==g[0].length)break;b=m(0==f[1].length?0:parseInt" +
    "(f[1],10),0==g[1].length?0:parseInt(g[1],10))||m(0==f[2].length,0=" +
    "=g[2].length)||m(f[2],g[2]);f=f[3];g=g[3]}while(0==b)}}var w=/Andr" +
    "oid\\s+([0-9\\.]+)/.exec(p()),v=w?w[1]:\"0\";u(2.3);u(4);function x(a," +
    "b){a=decodeURIComponent(a);b=b||document;var e=b||document;var c=e" +
    ".$wdc_;c||(c=e.$wdc_={},c.g=Date.now());c.g||(c.g=Date.now());e=c;" +
    "if(!(null!==e&&a in e))throw new q(10,\"Element does not exist in c" +
    "ache\");c=e[a];if(null!==c&&\"setInterval\"in c){if(c.closed)throw de" +
    "lete e[a],new q(23,\"Window has been closed.\");return c}for(var d=c" +
    ",f=!!HTMLElement.prototype.attachShadow;d;){if(d==b.documentElemen" +
    "t)return c;f&&d instanceof ShadowRoot?d.host.shadowRoot!==d?d=null" +
    ":d=d.host:d=d.parentNode}delete e[a];\nthrow new q(10,\"Element is n" +
    "o longer attached to the DOM\");}var y=[\"_\"],z=h;y[0]in z||\"undefin" +
    "ed\"==typeof z.execScript||z.execScript(\"var \"+y[0]);for(var A;y.le" +
    "ngth&&(A=y.shift());)y.length||void 0===x?z[A]&&z[A]!==Object.prot" +
    "otype[A]?z=z[A]:z=z[A]={}:z[A]=x;;return this._.apply(null,argumen" +
    "ts);}).apply({navigator:typeof window!=\"undefined\"?window.navigato" +
    "r:null},arguments);}\n";
  static final String GET_ELEMENT_ANDROID_license =
    "\n\n Copyright 2014 Software Freedom Conservancy\n\n Licensed under th" +
    "e Apache License, Version 2.0 (the \"License\");\n you may not use th" +
    "is file except in compliance with the License.\n You may obtain a c" +
    "opy of the License at\n\n      http://www.apache.org/licenses/LICENS" +
    "E-2.0\n\n Unless required by applicable law or agreed to in writing," +
    " software\n distributed under the License is distributed on an \"AS " +
    "IS\" BASIS,\n WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either e" +
    "xpress or implied.\n See the License for the specific language gove" +
    "rning permissions and\n limitations under the License.\n\n\n Copyright" +
    " The Closure Library Authors.\n SPDX-License-Identifier: Apache-2.0" +
    "\n";
  private static final String GET_ELEMENT_ANDROID_original() {
    return GET_ELEMENT_ANDROID.replaceAll("xxx_rpl_lic", GET_ELEMENT_ANDROID_license);
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      {
        String message =     "EXECUTE_SCRIPT_ANDROID third_party/javascript/browser_automation/b" +
    "ot/fragments/execute_script_android.js";
        System.out.println(message);
      }
      {
        String message =     "GET_ELEMENT_ANDROID tools/android/webdriver/atom/get_element_andro" +
    "id.js";
        System.out.println(message);
      }
      System.exit(0);
    }
    String name = args[0];
    if("EXECUTE_SCRIPT_ANDROID".equals(name)) {
      System.out.print(EvaluationAtom.EXECUTE_SCRIPT_ANDROID_original());
      System.exit(0);
    }
    if("GET_ELEMENT_ANDROID".equals(name)) {
      System.out.print(EvaluationAtom.GET_ELEMENT_ANDROID_original());
      System.exit(0);
    }
    System.exit(1);
  }
}