var Monitor = function () {
    this.init()
    console.log("BehavioSec, Starting monitoring")
};
Monitor.prototype = {
    behavioData: [],
    anonMap: [],
    startTimestamp: (new Date).getTime(),
    lastViewport: [-1, -1],
    lastDevicePixelRatio: window.devicePixelRatio,
    lastTarget: null,
    behavio_hidden: null,
    behavio_hidden_id: "behavio_hidden",
    metaNames: {pageId: "bwpageid"},
    ignoreFields: {ids: [], names: []},
    haveMouse: !0,
    collectMouseMovement: !0,
    lastKey: -1,
    textLengths: {},
    behavioweb_config: {
        anonymous: {by_name: [], by_id: [], by_type: ["password"]},
        masked: {by_name: [], by_id: [], by_type: []}
    },
    e: {
        ptype: null,
        ptypes: {},
        k229: 0,
        kn: 0,
        tz: (new Date).getTimezoneOffset(),
        pr: window.devicePixelRatio,
        u: {},
        f: null
    },
    g: {meta: {}, events: []},
    isAndroid: /android (\d+)/i.test(window.navigator.userAgent),
    isFirefox: /firefox/i.test(window.navigator.userAgent),
    hasFallbackListeners: !1,
    init: function () {
        var goodToGoInterval;
        goodToGoInterval = setInterval(function () {
            "complete" == document.readyState && (bw.startMonitor(), this.initialized = !0, clearInterval(goodToGoInterval))
        }, 10)
    },
    submitHandler: function (e) {
        var field = document.getElementById(bw.behavio_hidden_id);
        field && (field.value = JSON.stringify(bw.getBehavioData(), "", ""))
    },
    getBehavioData: function () {
        var data = bw.behavioData.slice();
        return data.push(["w", bw.getDataIntegrity(), bw.getPath()]), data
    },
    addKeyEvent: function (target, monitorType, data) {
        var i, l;
        for (i = this.behavioData.length - 1; -1 <= i; i--) {
            if (-1 == i) {
                l = null == this.behavioData[0] ? 0 : this.behavioData.length, this.behavioData[l] = [], "a" == monitorType ? this.behavioData[l][0] = "fa" : "n" == monitorType ? this.behavioData[l][0] = "f" : "m" === monitorType && (this.behavioData[l][0] = "fm"), this.behavioData[l][1] = target, this.behavioData[l][2] = [], this.behavioData[l][2][0] = data;
                break
            }
            if ("a" == monitorType) {
                if ("fa" !== this.behavioData[i][0]) continue
            } else if ("n" == monitorType) {
                if ("f" !== this.behavioData[i][0]) continue
            } else if ("m" == monitorType && "fm" !== this.behavioData[i][0]) continue;
            if (this.behavioData[i][1] == target) {
                this.behavioData[i][2][this.behavioData[i][2].length] = data;
                break
            }
        }
    },
    addEvent: function (data, field) {
        var i, l;
        for (i = this.behavioData.length - 1; -1 <= i; i--) {
            if (-1 == i) {
                l = null == this.behavioData[0] ? 0 : this.behavioData.length, this.behavioData[l] = [], this.behavioData[l][0] = "c", this.behavioData[l][1] = [], this.behavioData[l][1][0] = data, this.behavioData[l][2] = window.location.pathname.split("?")[0];
                break
            }
            if ("c" == this.behavioData[i][0]) {
                this.behavioData[i][1][this.behavioData[i][1].length] = data;
                break
            }
        }
    },
    getTimestamp: function () {
        return (new Date).getTime() - this.startTimestamp
    },
    checkTarget: function (event, timestamp) {
        var element = document.elementFromPoint(event.clientX, event.clientY), data = [];
        null != element && element != this.lastTarget && void 0 !== element && void 0 !== element.parentNode && (data[0] = "t", data[1] = element.nodeName + "#" + element.id + "#" + element.parentNode.nodeName + "#" + element.parentNode.id, data[2] = timestamp || bw.getTimestamp(), this.lastTarget = element, bw.addEvent(data))
    },
    checkViewport: function (timestamp) {
        if (this.lastViewport[0] !== document.documentElement.clientWidth || this.lastViewport[1] !== document.documentElement.clientHeight) {
            var data = [];
            data[0] = "v", data[1] = document.documentElement.clientWidth, data[2] = document.documentElement.clientHeight, data[3] = timestamp || bw.getTimestamp(), this.lastViewport[0] = document.documentElement.clientWidth, this.lastViewport[1] = document.documentElement.clientHeight, bw.addEvent(data)
        }
    },
    checkDevicePixelRatio: function () {
        this.lastDevicePixelRatio !== window.devicePixelRatio && (this.lastDevicePixelRatio = window.devicePixelRatio, bw.addGlobalEvent("G", this.lastDevicePixelRatio))
    },
    pointerMoveHandler: function (event) {
        if (event.getCoalescedEvents) {
            var events = event.getCoalescedEvents() || [];
            0 == events.length && events.push(event);
            for (var base = bw.getTimestamp(), lastTimestamp = event.timeStamp, i = 0; i < events.length; i++) {
                var e = events[i], ts = base - Math.round(lastTimestamp - (e.timeStamp || lastTimestamp));
                (data = [])[0] = "mm", data[1] = e.clientX, data[2] = e.clientY, data[3] = ts, bw.checkTarget(event, ts), bw.checkViewport(ts), bw.addEvent(data);
                var pt = e.pointerType || "unknown";
                bw.e.ptypes[pt] = (bw.e.ptypes[pt] || 0) + 1
            }
            bw.e.ptype = "pc"
        } else {
            var data;
            ts = bw.getTimestamp();
            (data = [])[0] = "mm", data[1] = event.clientX, data[2] = event.clientY, data[3] = ts, bw.checkTarget(event, ts), bw.checkViewport(ts), bw.addEvent(data);
            pt = event.pointerType || "unknown";
            bw.e.ptypes[pt] = (bw.e.ptypes[pt] || 0) + 1, bw.e.ptype = "pm"
        }
    },
    pointerDownHandler: function (event) {
        var ts = bw.getTimestamp(), data = [];
        data[0] = "md", data[1] = event.clientX, data[2] = event.clientY, data[3] = ts, data[4] = event.button, bw.checkTarget(event, ts), bw.checkViewport(ts), bw.checkDevicePixelRatio(), bw.addEvent(data);
        var pt = event.pointerType || "unknown";
        bw.e.ptypes[pt] = (bw.e.ptypes[pt] || 0) + 1
    },
    pointerUpHandler: function (event) {
        var ts = bw.getTimestamp(), data = [];
        data[0] = "mu", data[1] = event.clientX, data[2] = event.clientY, data[3] = ts, data[4] = event.button, bw.checkTarget(event, ts), bw.checkViewport(ts), bw.checkDevicePixelRatio(), bw.addEvent(data);
        var pt = event.pointerType || "unknown";
        bw.e.ptypes[pt] = (bw.e.ptypes[pt] || 0) + 1
    },
    mouseMoveHandler: function (event) {
        var data = [];
        data[0] = "mm", data[1] = event.clientX, data[2] = event.clientY, data[3] = bw.getTimestamp(), bw.checkTarget(event), bw.checkViewport(), bw.addEvent(data)
    },
    mouseDownHandler: function (event) {
        var data = [];
        data[0] = "md", data[1] = event.clientX, data[2] = event.clientY, data[3] = bw.getTimestamp(), data[4] = event.button, bw.checkTarget(event), bw.checkViewport(), bw.checkDevicePixelRatio(), bw.addEvent(data)
    },
    mouseUpHandler: function (event) {
        var data = [];
        data[0] = "mu", data[1] = event.clientX, data[2] = event.clientY, data[3] = bw.getTimestamp(), data[4] = event.button, bw.checkTarget(event), bw.checkViewport(), bw.checkDevicePixelRatio(), bw.addEvent(data)
    },
    keyHandler: function (event) {
        if ("undefined" == typeof KeyboardEvent || event instanceof KeyboardEvent) {
            var field, monitorType, data = [], keyCode = 0 == event.keyCode ? 229 : event.keyCode, keyId = keyCode,
                source = event.currentTarget ? event.currentTarget : event.srcElement, caretPos = 0;
            if (229 != keyCode || "keydown" != event.type || bw.isFirefox || bw.hasFallbackListeners || (bw.fallbackListeners(), bw.hasFallbackListeners = !0), field = source.type + "#" + source.name, null == keyCode && (keyId = keyCode = -500), "a" == (monitorType = bw.getMonitorType(source))) {
                if (9 == keyCode || 13 == keyCode) return;
                if (document.selection) {
                    source.focus();
                    var Sel = document.selection.createRange(),
                        SelLength = document.selection.createRange().text.length;
                    Sel.moveStart("character", -source.value.length), caretPos = Sel.text.length - SelLength
                } else (source.selectionStart || "0" == source.selectionStart) && (caretPos = source.selectionStart);
                8 == keyCode ? "keydown" == event.type ? (null == bw.anonMap[keyCode] && (bw.anonMap[keyCode] = caretPos), data[0] = -1, data[1] = caretPos) : "keyup" == event.type && (data[0] = -2, data[1] = bw.anonMap[keyCode], bw.anonMap[keyCode] = null) : 46 == keyCode ? "keydown" == event.type ? (null == bw.anonMap[keyCode] && (bw.anonMap[keyCode] = caretPos), data[0] = -3, data[1] = caretPos) : "keyup" == event.type && (data[0] = -4, data[1] = bw.anonMap[keyCode], bw.anonMap[keyCode] = null) : "keydown" == event.type ? (null == bw.anonMap[keyCode] && (bw.anonMap[keyCode] = caretPos), data[0] = 0, data[1] = caretPos) : "keyup" == event.type && (data[0] = 1, data[1] = bw.anonMap[keyCode], bw.anonMap[keyCode] = null)
            } else if ("m" === monitorType) {
                if (document.selection) {
                    source.focus();
                    Sel = document.selection.createRange(), SelLength = document.selection.createRange().text.length;
                    Sel.moveStart("character", -source.value.length), caretPos = Sel.text.length - SelLength
                } else (source.selectionStart || "0" == source.selectionStart) && (caretPos = source.selectionStart);
                caretPos = -1 * (caretPos + 1), keyCode < 47 && 32 != keyCode || 90 < keyCode && keyCode < 96 || 112 <= keyCode && keyCode <= 123 || 144 == keyCode ? "keyup" === event.type ? (bw.lastKey = -1, data[0] = 1, data[1] = keyId) : "keydown" === event.type && (data[0] = 0, data[1] = keyId) : "keydown" == event.type ? (null == bw.anonMap[keyCode] && (bw.anonMap[keyCode] = caretPos), data[0] = 0, data[1] = caretPos) : "keyup" == event.type && (data[0] = 1, data[1] = bw.anonMap[keyCode], bw.anonMap[keyCode] = null)
            } else {
                if (229 == keyCode && "keydown" == event.type) bw.setTextLength(field, source.value.length); else if (229 == keyCode && "keyup" == event.type) {
                    var s = bw.textLengths[field] || [], ul = source.value.length, dl = s.pop() || 0;
                    if (-1 != bw.lastKey || ul < dl) {
                        ul - dl <= 1 && (keyId = ul < dl ? 8 : bw.lastKey);
                        var targetData = bw.getTargetData(field), prevKey = targetData[targetData.length - 1];
                        229 == prevKey[1] && (prevKey[1] = keyId), bw.lastKey = -1
                    }
                }
                "keyup" === event.type ? (bw.lastKey = -1, data[0] = 1, data[1] = keyId) : "keydown" === event.type && (data[0] = 0, data[1] = keyId, 229 == keyCode ? bw.e.k229++ : bw.e.kn++)
            }
            data[2] = bw.getTimestamp(), null != data[1] && bw.addKeyEvent(field, monitorType, data)
        }
    },
    getMonitorType: function (source) {
        for (i = 0; i < bw.behavioweb_config.anonymous.by_id.length; i++) if (bw.behavioweb_config.anonymous.by_id[i] == source.id) return "a";
        for (i = 0; i < bw.behavioweb_config.anonymous.by_name.length; i++) if (bw.behavioweb_config.anonymous.by_name[i] == source.name) return "a";
        for (i = 0; i < bw.behavioweb_config.anonymous.by_type.length; i++) if (bw.behavioweb_config.anonymous.by_type[i] == source.type) return "a";
        for (i = 0; i < bw.behavioweb_config.masked.by_id.length; i++) if (bw.behavioweb_config.masked.by_id[i] == source.id) return "m";
        for (i = 0; i < bw.behavioweb_config.masked.by_name.length; i++) if (bw.behavioweb_config.masked.by_name[i] == source.name) return "m";
        for (i = 0; i < bw.behavioweb_config.masked.by_type.length; i++) if (bw.behavioweb_config.masked.by_type[i] == source.type) return "m";
        return "n"
    },
    getTargetData: function (target) {
        for (var i = this.behavioData.length - 1; 0 <= i; i--) if (this.behavioData[i][1] == target) return this.behavioData[i][2]
    },
    setLastKey: function (k) {
        bw.lastKey = k
    },
    setTextLength: function (target, len) {
        (bw.textLengths[target] = bw.textLengths[target] || []).push(len)
    },
    keyTransformer: function (e) {
        e.data && 1 == e.data.length && bw.setLastKey(e.data.toUpperCase().charCodeAt(0))
    },
    keyComposition: function (e) {
        e.data && bw.setLastKey(e.data.toUpperCase().charCodeAt(e.data.length - 1))
    },
    fallbackListeners: function (field) {
        field && field.addEventListener ? (field.addEventListener("textInput", this.keyTransformer, !1), field.addEventListener("compositionupdate", this.keyComposition, !1)) : document.addEventListener && (document.addEventListener("textInput", this.keyTransformer, !1), document.addEventListener("compositionupdate", this.keyComposition, !1))
    },
    getDataIntegrity: function () {
        var fields = [];
        bw.addIntegrityInputs("input", fields), bw.addIntegrityInputs("textarea", fields);
        for (var i = 0; i < this.behavioData.length; ++i) if ("c" == this.behavioData[i][0]) {
            fields.push({movement: 0});
            break
        }
        return fields
    },
    addIntegrityInputs: function (type, fields) {
        for (var inputs = document.getElementsByTagName(type), i = 0; i < inputs.length; i++) {
            var field = inputs[i], ftype = field.type;
            if ("hidden" != ftype && "button" != ftype && "submit" != ftype && "radio" != ftype && "checkbox" != ftype && -1 === this.ignoreFields.names.indexOf(field.name) && -1 === this.ignoreFields.ids.indexOf(field.id)) {
                var name = field.type + "#" + field.name, val = field.value.length, obj = {};
                obj[name] = val, fields.push(obj)
            }
        }
    },
    getMeta: function (metaName) {
        var a, b, retVal = "";
        for (b = document.getElementsByTagName("meta"), a = 0; a < b.length; a++) metaName != b[a].name && metaName != b[a].getAttribute("property") || (retVal = b[a].content);
        return retVal
    },
    getPath: function () {
        var path = window.location.pathname.split("?")[0] || "/";
        return "" != bw.getMeta(bw.metaNames.pageId) && (path = bw.getMeta(bw.metaNames.pageId)), path
    },
    getClientInfo: function () {
        var v = function (arg) {
            try {
                return "" + arg
            } catch (e) {
                return "e"
            }
        }, c = function (callback) {
            try {
                return "" + callback()
            } catch (e) {
                return !1
            }
        };
        return [1, v(navigator.serviceWorker), v(navigator.geolocation), v(navigator.cookieEnabled), v(navigator.buildID), v(navigator.productSub), v(navigator.oscpu), v("ontouchstart" in window || 0 < navigator.maxTouchPoints || 0 < navigator.msMaxTouchPoints), c(function () {
            var r = document.createElement("canvas");
            return "" + (!!window.WebGLRenderingContext && (r.getContext("webgl") || r.getContext("experimental-webgl")))
        }), v(window.HTMLCanvasElement || document.createElement("canvas").getContext), v(!(!(screen.width < screen.availWidth) && screen.height < screen.availHeight)), c(function () {
            return void 0 !== navigator.languages && navigator.languages[0].substr(0, 2) !== navigator.language.substr(0, 2)
        }), v(navigator.doNotTrack), v(navigator.platform), v(navigator.cpuClass), v(!!window.openDatabase), v(!!document.body.addBehavior), v(!!window.indexedDB), v(!!window.sessionStorage), function () {
            var map = function (elems, callback, arg) {
                var value, key, ret = [], i = 0, length = elems.length;
                if (void 0 !== length && "number" == typeof length && (0 < length && elems[0] && elems[length - 1] || 0 === length)) for (; i < length; i++) null != (value = callback(elems[i], i, arg)) && (ret[ret.length] = value); else for (key in elems) null != (value = callback(elems[key], key, arg)) && (ret[ret.length] = value);
                return ret.concat.apply([], ret)
            }, serialize = function (object) {
                var type = typeof object;
                if (null === object) return '"emptyValue"';
                if ("string" == type || "number" === type || "boolean" === type) return '"' + (object + "").replace(/["]/g, '\\"') + '"';
                if ("function" === type) return '"functionValue"';
                if ("object" !== type) return "undefined" === type ? '"undefinedError"' : '"unknownTypeError"';
                var output = "{";
                try {
                    for (var item in object) "enabledPlugin" !== item && (output += '"' + item + '":' + serialize(object[item]) + ",")
                } catch (e) {
                }
                return output.replace(/\,$/, "") + "}"
            }, IE_ver = function () {
                var rv = -1;
                if ("Microsoft Internet Explorer" == navigator.appName) {
                    var ua = navigator.userAgent;
                    null != new RegExp("MSIE ([0-9]{1,}[.0-9]{0,})").exec(ua) && (rv = parseFloat(RegExp.$1))
                } else new RegExp("Trident").test(navigator.userAgent) && (ua = navigator.userAgent, null != new RegExp("11.0").exec(ua) && (rv = parseFloat(RegExp.$1)), navigator.userAgent.match(/Trident.*rv[ :]*/) && null != new RegExp("rv:([0-9]{1,}[.0-9]{0,})").exec(ua) && (rv = parseFloat(RegExp.$1)));
                return rv
            }();
            if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
                if (screen.height) if (screen.height < screen.width) var scrHeight = screen.height,
                    scrWidth = screen.width; else scrHeight = screen.width, scrWidth = screen.height;
                flmobile = 1
            } else screen.height && (scrHeight = screen.height, scrWidth = screen.width);
            return serialize([navigator.userAgent, navigator.language ? navigator.language : navigator.userLanguage, navigator.cookieEnabled, screen.height ? scrWidth + "x" + scrHeight + "x" + screen.colorDepth : "", screen.pixelDepth ? screen.pixelDepth : "", screen.deviceXDPI ? screen.deviceXDPI + "x" + screen.deviceYDPI : "", screen.systemXDPI ? screen.systemXDPI + "x" + screen.systemYDPI : "", screen.updateInterval ? screen.updateInterval : "", (new Date).getTimezoneOffset(), !!window.sessionStorage, !!window.localStorage, window.devicePixelRatio ? window.devicePixelRatio : 0, 0 < IE_ver && "11" != IE_ver ? function () {
                if (!(0 < IE_ver)) return "na";
                var rtrn_val, i;
                if (IE_ver < 11) try {
                    rtrn_val = new Array;
                    var all_clssPIs = new Array("Address Book", "AOL ART Image Format Support", "Arabic Text Display Support", "Chinese (Simplified) Text Display Support", "Chinese (traditional) Text Display Support", "DirectAnimation", "Dynamic HTML Data Binding for Java", "Dynamic HTML Data Binding", "DirectShow", "Hebrew Text Display Support", "Internet Explorer Browsing Enhancements", "Internet Connection Wizard", "Internet Explorer 5 Browser", "Internet Explorer Classes for Java", "Internet Explorer Help Engine", "Internet Explorer Help", "Japanese Text Display Support", "Java Plug-in", "Korean Text Display Support", "Language Auto-Selection", "MSN Messenger Service", "Microsoft virtual machine", "NetMeeting NT", "Offline Browsing Pack", "Outlook Express", "Pan-European Text Display Support", "Macromedia Shockwave Director", "Macromedia Flash", "Thai Text Display Support", "Task Scheduler", "Uniscribe", "Visual Basic Scripting Support", "Vietnamese Text Display Support", "Vector Graphics Rendering (VML)", "Web Folders", "Windows Media Player", "Windows Desktop Update NT");
                    document.body.addBehavior("#default#clientCaps");
                    var cntpi = 0;
                    for (i in {
                        "7790769C-0471-11D2-AF11-00C04FA35D02": "abk",
                        "47F67D00-9E55-11D1-BAEF-00C04FC2D130": "aol",
                        "76C19B38-F0C8-11CF-87CC-0020AFEECF20": "arb",
                        "76C19B34-F0C8-11CF-87CC-0020AFEECF20": "chs",
                        "76C19B33-F0C8-11CF-87CC-0020AFEECF20": "cht",
                        "283807B5-2C60-11D0-A31D-00AA00B92C03": "dan",
                        "4F216970-C90C-11D1-B5C7-0000F8051515": "dhj",
                        "9381D8F2-0288-11D0-9501-00AA00B911A5": "dht",
                        "44BBA848-CC51-11CF-AAFA-00AA00B6015C": "dsh",
                        "76C19B36-F0C8-11CF-87CC-0020AFEECF20": "heb",
                        "630B1DA0-B465-11D1-9948-00C04F98BBC9": "ibe",
                        "5A8D6EE0-3E18-11D0-821E-444553540000": "icw",
                        "89820200-ECBD-11CF-8B85-00AA005B4383": "ie5",
                        "08B0E5C0-4FCB-11CF-AAA5-00401C608555": "iec",
                        "DE5AED00-A4BF-11D1-9948-00C04F98BBC9": "iee",
                        "45EA75A0-A269-11D1-B5BF-0000F8051515": "ieh",
                        "76C19B30-F0C8-11CF-87CC-0020AFEECF20": "jap",
                        "8AD9C840-044E-11D1-B3E9-00805F499D93": "jav",
                        "76C19B31-F0C8-11CF-87CC-0020AFEECF20": "krn",
                        "76C19B50-F0C8-11CF-87CC-0020AFEECF20": "lan",
                        "5945C046-LE7D-LLDL-BC44-00C04FD912BE": "msn",
                        "08B0E5C0-4FCB-11CF-AAA5-00401C608500": "mvm",
                        "44BBA842-CC51-11CF-AAFA-00AA00B6015B": "net",
                        "3AF36230-A269-11D1-B5BF-0000F8051515": "obp",
                        "44BBA840-CC51-11CF-AAFA-00AA00B6015C": "oex",
                        "76C19B32-F0C8-11CF-87CC-0020AFEECF20": "pan",
                        "2A202491-F00D-11CF-87CC-0020AFEECF20": "shw",
                        "D27CDB6E-AE6D-11CF-96B8-444553540000": "swf",
                        "76C19B35-F0C8-11CF-87CC-0020AFEECF20": "thi",
                        "CC2A9BA0-3BDD-11D0-821E-444553540000": "tks",
                        "3BF42070-B3B1-11D1-B5C5-0000F8051515": "uni",
                        "4F645220-306D-11D2-995D-00C04F98BBC9": "vbs",
                        "76C19B37-F0C8-11CF-87CC-0020AFEECF20": "vnm",
                        "10072CEC-8CC1-11D1-986E-00A0C955B42F": "vtc",
                        "73FA19D0-2D75-11D2-995D-00C04F98BBC9": "wfd",
                        "22D6F312-B0F6-11D0-94AB-0080C74C7E95": "wmp",
                        "89820200-ECBD-11CF-8B85-00AA005B4340": "wnt"
                    }) try {
                        comptVer = document.body.getComponentVersion("{" + i + "}", "ComponentID"), comptVer && rtrn_val.push(encodeURIComponent(all_clssPIs[cntpi] + " - " + comptVer.replace(/,/g, ".") + " -  - ") + ";"), cntpi++
                    } catch (e) {
                    }
                    return 0 === rtrn_val.length ? "none" : rtrn_val
                } catch (e) {
                    return "Err"
                }
            }() : map(navigator.plugins, function (e) {
                return [e.name, e.description, map(e, function (e) {
                    return [e.type, e.suffixes].join(":")
                }).join("|")].join("|")
            })])
        }()]
    },
    getElementIdentifier: function (e) {
        var element = e.target || e.srcElement || {};
        return element.type && (element.id || element.name) ? element.type + "#" + (element.name || element.id) : element && element.parentNode ? element.nodeName + "#" + (element.id || element.name || "") + "#" + element.parentNode.nodeName + "#" + (element.parentNode.id || element.parentNode.name || "") : void 0
    },
    addGlobalEvent: function (kind, payload) {
        bw.g.events.push([kind, bw.getTimestamp(), payload])
    },
    addGlobalStats: function (kind) {
        bw.g.meta[kind] = kind in bw.g.meta ? ++bw.g.meta[kind] : 1
    },
    visibilityHandler: function (e) {
        var s = document.visibilityState;
        s ? bw.addGlobalEvent("o", s) : bw.addGlobalEvent("p", document.hidden)
    },
    targetHandler: function (e) {
        bw.addGlobalEvent(bw.targetMapping(e.type), bw.getElementIdentifier(e))
    },
    windowHandler: function (e) {
        bw.addGlobalEvent(bw.windowMapping(e.type), "")
    },
    globalKeyHandler: function (e) {
        var kind = "keydown" === e.type ? "a" : "s", target = e.target || e.srcElement, key = e.which || e.keyCode;
        9 === key || "Tab" === e.key ? bw.addGlobalEvent(kind + "f", bw.getElementIdentifier(e)) : 13 === key || "Enter" === e.key ? bw.addGlobalEvent(kind + "v", bw.getElementIdentifier(e)) : 16 === key || "Shift" === e.key ? bw.addGlobalEvent(kind + "n", bw.getElementIdentifier(e)) : 17 === key || "Control" === e.key ? bw.addGlobalEvent(kind + "z", bw.getElementIdentifier(e)) : 18 === key || "Alt" === e.key ? bw.addGlobalEvent(kind + "a", bw.getElementIdentifier(e)) : 20 === key || "CapsLock" === e.key ? bw.addGlobalEvent(kind + "w", bw.getElementIdentifier(e)) : 37 === key || "ArrowLeft" === e.key ? bw.addGlobalEvent(kind + "r", bw.getElementIdentifier(e)) : 38 === key || "ArrowUp" === e.key ? bw.addGlobalEvent(kind + "t", bw.getElementIdentifier(e)) : 39 === key || "ArrowRight" === e.key ? bw.addGlobalEvent(kind + "d", bw.getElementIdentifier(e)) : 40 === key || "ArrowDown" === e.key ? bw.addGlobalEvent(kind + "k", bw.getElementIdentifier(e)) : target && void 0 === target.type && bw.addGlobalStats(bw.metaMapping(e.type))
    },
    wheelHandler: function (event) {
        bw.addGlobalStats("M"), bw.checkDevicePixelRatio()
    },
    scrollHandler: function (event) {
        bw.addGlobalStats("k"), bw.checkDevicePixelRatio()
    },
    windowResizeHandler: function (event) {
        bw.checkDevicePixelRatio()
    },
    addVisibilityListener: function () {
        void 0 !== document.visibilityState ? document.addEventListener("visibilitychange", this.visibilityHandler, !1) : void 0 !== document.mozHidden ? document.addEventListener("mozvisibilitychange", this.visibilityHandler, !1) : void 0 !== document.webkitHidden ? document.addEventListener("webkitvisibilitychange", this.visibilityHandler, !1) : void 0 !== document.msHidden && document.addEventListener("msvisibilitychange", this.visibilityHandler, !1)
    },
    addCopyPasteCutListeners: function () {
        document.addEventListener ? (document.addEventListener("copy", this.targetHandler, !0), document.addEventListener("paste", this.targetHandler, !0), document.addEventListener("cut", this.targetHandler, !0)) : document.attachEvent && document.body && document.body.attachEvent("onpaste", this.targetHandler)
    },
    addMenuListener: function () {
        document.addEventListener ? document.addEventListener("contextmenu", this.targetHandler, !0) : document.attachEvent && document.attachEvent("oncontextmenu", this.targetHandler)
    },
    addFocusListeners: function () {
        document.addEventListener ? (document.addEventListener("focus", this.targetHandler, !0), document.addEventListener("blur", this.targetHandler, !0), window.addEventListener("focus", this.windowHandler, !1), window.addEventListener("blur", this.windowHandler, !1)) : document.attachEvent && (document.attachEvent("onfocus", this.targetHandler), document.attachEvent("onblur", this.targetHandler), window.attachEvent("onfocus", this.windowHandler), window.attachEvent("onblur", this.windowHandler))
    },
    addSubmitListener: function () {
        document.addEventListener ? document.addEventListener("submit", this.targetHandler, !0) : document.attachEvent && document.attachEvent("onsubmit", this.targetHandler)
    },
    addMouseListeners: function () {
        document.addEventListener ? (document.addEventListener("mouseup", this.targetHandler), document.addEventListener("mousedown", this.targetHandler)) : document.attachEvent && (document.attachEvent("onmouseup", this.targetHandler), document.attachEvent("onmousedown", this.targetHandler)), window.addEventListener ? ("onwheel" in document ? window.addEventListener("wheel", this.wheelHandler, !1) : void 0 !== document.onmousewheel ? window.addEventListener("mousewheel", this.wheelHandler, !1) : document.addEventListener && window.addEventListener("DOMMouseScroll", this.wheelHandler), window.addEventListener("scroll", this.scrollHandler)) : document.attachEvent && (document.attachEvent("onmousewheel", this.wheelHandler), document.attachEvent("onscroll", this.scrollHandler))
    },
    addKeyListeners: function () {
        document.addEventListener ? (document.addEventListener("keydown", this.globalKeyHandler), document.addEventListener("keyup", this.globalKeyHandler)) : document.attachEvent && (document.attachEvent("onkeydown", this.globalKeyHandler), document.attachEvent("onkeyup", this.globalKeyHandler))
    },
    addWindowListeners: function () {
        document.addEventListener ? window.addEventListener("resize", this.windowResizeHandler) : document.attachEvent && window.attachEvent("onresize", this.windowResizeHandler)
    },
    targetMapping: function (type) {
        switch (type) {
            case"mousedown":
                return "D";
            case"mouseup":
                return "E";
            case"submit":
                return "i";
            case"focus":
                return "n";
            case"blur":
                return "U";
            case"paste":
                return "z";
            case"copy":
                return "u";
            case"cut":
                return "c";
            case"contextmenu":
                return "X";
            default:
                return "T"
        }
    },
    windowMapping: function (type) {
        switch (type) {
            case"blur":
                return "r";
            case"focus":
                return "v";
            default:
                return "I"
        }
    },
    metaMapping: function (type) {
        switch (type) {
            case"keydown":
                return "e";
            case"keyup":
                return "w";
            default:
                return "T"
        }
    },
    startMonitor: function () {
        var i, j, thisForm, fields, field, hiddenField, forms = document.getElementsByTagName("form");
        if (null == document.getElementById(bw.behavio_hidden_id) || void 0 === document.getElementById(bw.behavio_hidden_id)) for ((hiddenField = document.createElement("input")).setAttribute("type", "hidden"), hiddenField.setAttribute("name", bw.behavio_hidden_id), hiddenField.setAttribute("id", bw.behavio_hidden_id), i = 0; i < forms.length; i++) (thisForm = forms[i]).appendChild(hiddenField);
        for ("undefined" != typeof jQuery && jQuery("form").submit(bw.submitHandler), i = 0; i < forms.length; i++) {
            for (thisForm = forms[i], "undefined" == typeof jQuery && (thisForm.addEventListener ? thisForm.addEventListener("submit", bw.submitHandler, !1) : thisForm.attachEvent && thisForm.attachEvent("onsubmit", bw.submitHandler)), fields = thisForm.getElementsByTagName("textarea"), j = 0; j < fields.length; j++) field = fields[j], -1 === this.ignoreFields.names.indexOf(field.name) && -1 === this.ignoreFields.ids.indexOf(field.id) && (field.addEventListener ? (field.addEventListener("keydown", this.keyHandler, !1), field.addEventListener("keyup", this.keyHandler, !1), this.isAndroid && !this.isFirefox && this.fallbackListeners(field)) : field.attachEvent && (field.attachEvent("onkeydown", this.keyHandler), field.attachEvent("onkeyup", this.keyHandler)));
            for (fields = thisForm.getElementsByTagName("input"), j = 0; j < fields.length; j++) "checkbox" !== (field = fields[j]).type && "radio" !== field.type && "hidden" !== field.type && "button" !== field.type && "submit" !== field.type && -1 === this.ignoreFields.names.indexOf(field.name) && -1 === this.ignoreFields.ids.indexOf(field.id) && (field.addEventListener ? (field.addEventListener("keydown", this.keyHandler, !1), field.addEventListener("keyup", this.keyHandler, !1), this.isAndroid && !this.isFirefox && this.fallbackListeners(field)) : field.attachEvent && (field.attachEvent("onkeydown", this.keyHandler), field.attachEvent("onkeyup", this.keyHandler)))
        }
        !0 === this.haveMouse && (document.addEventListener ? window.PointerEvent ? (document.addEventListener("pointerdown", this.pointerDownHandler, !1), document.addEventListener("pointerup", this.pointerUpHandler, !1), this.collectMouseMovement && document.addEventListener("pointermove", this.pointerMoveHandler, !1)) : (this.e.ptype = "mm", document.addEventListener("mousedown", this.mouseDownHandler, !1), document.addEventListener("mouseup", this.mouseUpHandler, !1), this.collectMouseMovement && document.addEventListener("mousemove", this.mouseMoveHandler, !1)) : document.attachEvent && (this.e.ptype = "mm", document.attachEvent("onmousedown", this.mouseDownHandler), document.attachEvent("onmouseup", this.mouseUpHandler), this.collectMouseMovement && document.attachEvent("onmousemove", this.mouseMoveHandler, !1))), this.addVisibilityListener(), this.addCopyPasteCutListeners(), this.addMenuListener(), this.addFocusListeners(), this.addSubmitListener(), this.addMouseListeners(), this.addKeyListeners(), this.addWindowListeners(), this.isAndroid && (this.hasFallbackListeners = !0);
        var _navigator = {};
        for (i in navigator) _navigator[i] = navigator[i];
        delete _navigator.plugins, delete _navigator.mimeTypes;
        var events, ticks, last, tx, _screen = {};
        for (i in screen) _screen[i] = screen[i];
        this._navigator = _navigator, this._screen = _screen, this.behavioData = [], this.behavioData[0] = ["m", "n", _navigator], this.behavioData[1] = ["m", "s", _screen], this.behavioData[2] = ["m", "v", 261], this.behavioData[3] = ["m", "e", this.e], this.g.meta = {}, this.g.events = [], this.behavioData[4] = ["m", "k", this.g.meta, this.g.events], this.addGlobalEvent("Z", (new Date).getTime()), this.e.f = this.getClientInfo(), events = {}, ticks = 0, last = Math.round(window.performance && window.performance.now ? window.performance.now() : (new Date).getTime()), tx = setInterval(function () {
            var n = Math.round(window.performance && window.performance.now ? window.performance.now() : (new Date).getTime());
            events[n - last] = (events[n - last] || 0) + 1, last = n, 100 < ++ticks && clearInterval(tx)
        }, 10), setTimeout(function () {
            clearInterval(tx), bw.e.u = events
        }, 1e3)
    }
};
var JSON, bw = new Monitor;

function readyState(fn) {
    "interactive" != document.readyState && "complete" != document.readyState || fn()
}

"undefined" == typeof console && (this.console = {
    log: function () {
    }, info: function () {
    }, error: function () {
    }, warn: function () {
    }
}), Date.now = Date.now || function () {
    return +new Date
}, JSON || (JSON = {}), function () {
    function d(f) {
        return f < 10 ? "0" + f : f
    }

    "function" != typeof Date.prototype.toJSON && (Date.prototype.toJSON = function (f) {
        return isFinite(this.valueOf()) ? this.getUTCFullYear() + "-" + d(this.getUTCMonth() + 1) + "-" + d(this.getUTCDate()) + "T" + d(this.getUTCHours()) + ":" + d(this.getUTCMinutes()) + ":" + d(this.getUTCSeconds()) + "Z" : null
    }, String.prototype.toJSON = Number.prototype.toJSON = Boolean.prototype.toJSON = function (f) {
        return this.valueOf()
    });
    var h, a, c,
        i = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        e = {"\b": "\\b", "\t": "\\t", "\n": "\\n", "\f": "\\f", "\r": "\\r", '"': '\\"', "\\": "\\\\"};

    function b(f) {
        return i.lastIndex = 0, i.test(f) ? '"' + f.replace(i, function (j) {
            var k = e[j];
            return "string" == typeof k ? k : "\\u" + ("0000" + j.charCodeAt(0).toString(16)).slice(-4)
        }) + '"' : '"' + f + '"'
    }

    "function" != typeof JSON.stringify && (JSON.stringify = function (l, j, k) {
        var f;
        if (a = h = "", "number" == typeof k) for (f = 0; f < k; f += 1) a += " "; else "string" == typeof k && (a = k);
        if ((c = j) && "function" != typeof j && ("object" != typeof j || "number" != typeof j.length)) throw new Error("JSON.stringify");
        return function g(q, n) {
            var l, j, r, f, m, o = h, p = n[q];
            switch (p && "object" == typeof p && "function" == typeof p.toJSON && (p = p.toJSON(q)), "function" == typeof c && (p = c.call(n, q, p)), typeof p) {
                case"string":
                    return b(p);
                case"number":
                    return isFinite(p) ? String(p) : "null";
                case"boolean":
                case"null":
                    return String(p);
                case"object":
                    if (!p) return "null";
                    if (h += a, m = [], "[object Array]" == Object.prototype.toString.apply(p)) {
                        for (f = p.length, l = 0; l < f; l += 1) m[l] = g(l, p) || "null";
                        return r = 0 == m.length ? "[]" : h ? "[\n" + h + m.join(",\n" + h) + "\n" + o + "]" : "[" + m.join(",") + "]", h = o, r
                    }
                    if (c && "object" == typeof c) for (f = c.length, l = 0; l < f; l += 1) "string" == typeof c[l] && (r = g(j = c[l], p)) && m.push(b(j) + (h ? ": " : ":") + r); else for (j in p) Object.prototype.hasOwnProperty.call(p, j) && (r = g(j, p)) && m.push(b(j) + (h ? ": " : ":") + r);
                    return r = 0 == m.length ? "{}" : h ? "{\n" + h + m.join(",\n" + h) + "\n" + o + "}" : "{" + m.join(",") + "}", h = o, r
            }
        }("", {"": l})
    })
}(), Array.prototype.indexOf || (Array.prototype.indexOf = function (obj, start) {
    var i, j;
    for (i = start || 0, j = this.length; i < j; i++) if (this[i] === obj) return i;
    return -1
});