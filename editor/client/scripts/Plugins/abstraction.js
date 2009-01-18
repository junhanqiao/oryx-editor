/** * Copyright (c) 2008, Artem Polyvyanyy * * Permission is hereby granted, free of charge, to any person obtaining a * copy of this software and associated documentation files (the "Software"), * to deal in the Software without restriction, including without limitation * the rights to use, copy, modify, merge, publish, distribute, sublicense, * and/or sell copies of the Software, and to permit persons to whom the * Software is furnished to do so, subject to the following conditions: * * The above copyright notice and this permission notice shall be included in * all copies or substantial portions of the Software. * * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER * DEALINGS IN THE SOFTWARE. **/if (!ORYX.Plugins)     ORYX.Plugins = new Object();	Ext.namespace("Ext.ux");Ext.ux.NotificationMgr = {  positions: []};Ext.ux.Notification = Ext.extend(Ext.Window, {  initComponent: function(){    Ext.apply(this, {      iconCls: this.iconCls || 'x-icon-information',      cls: 'x-notification',      width: 200,      autoHeight: true,      plain: false,      draggable: false,      bodyStyle: this.bodyStyle || 'text-align:center'    });    if(this.autoDestroy) {      this.task = new Ext.util.DelayedTask(this.hide, this);    } else {      this.closable = true;    }    Ext.ux.Notification.superclass.initComponent.call(this);  },  setMessage: function(msg){    this.body.update(msg);  },  setTitle: function(title, iconCls){    Ext.ux.Notification.superclass.setTitle.call(this, title, iconCls||this.iconCls);  },  onRender:function(ct, position) {    Ext.ux.Notification.superclass.onRender.call(this, ct, position);  },  onDestroy: function(){    Ext.ux.NotificationMgr.positions.remove(this.pos);    Ext.ux.Notification.superclass.onDestroy.call(this);  },  cancelHiding: function(){    this.addClass('fixed');    if(this.autoDestroy) {      this.task.cancel();    }  },  afterShow: function(){    Ext.ux.Notification.superclass.afterShow.call(this);    Ext.fly(this.body.dom).on('click', this.cancelHiding, this);    if(this.autoDestroy) {      this.task.delay(this.hideDelay || 5000);     }  },  animShow: function(){    this.pos = 0;    while(Ext.ux.NotificationMgr.positions.indexOf(this.pos)>-1)      this.pos++;    Ext.ux.NotificationMgr.positions.push(this.pos);    this.setSize(200,100);    this.el.alignTo(document, "br-br", [ -20, -20-((this.getSize().height+10)*this.pos) ]);    this.el.slideIn('b', {      duration: 1,      callback: this.afterShow,      scope: this    });  },  animHide: function(){       Ext.ux.NotificationMgr.positions.remove(this.pos);    this.el.ghost("b", {      duration: 1,      remove: true    });  },  focus: Ext.emptyFn }); Ext.namespace("ORYX.Plugins");/************************************************************* * ABSTRACTION PLUGIN *************************************************************/ORYX.Plugins.Abstraction = Clazz.extend({		facade		: undefined,	candidate	: undefined,	rdf			: undefined,	active		: false,		response	: undefined,	new_center	: undefined,      	construct: function(facade) {		this.facade = facade;		this.facade.offer({			'name': 'Abstraction mode',			'description': 'Abstraction mode',			'functionality': this.toggleAbstractionMode.bind(this),			'group': 'Abstraction',			'icon': ORYX.PATH + "images/wrench.png",			'index': 1,			'minShape': 0,			'maxShape': 0});	},		toggleAbstractionMode: function(){		// update status		this.active = !this.active;				// show status notification		var ntitle	= (this.active) ? 'Enabled' : 'Disabled';		var ntext	= (this.active) ? 'Abstraction mode is enabled' : 'Abstraction mode is disabled';		this.showNotification(ntitle,ntext);				// register/unregister events		if (this.active) {			this.callback = this.doMouseUp.bind(this)			this.facade.registerOnEvent(ORYX.CONFIG.EVENT_MOUSEUP, this.callback);		}		else {			this.facade.unregisterOnEvent(ORYX.CONFIG.EVENT_MOUSEUP, this.callback);			this.callback = undefined;						this.hideOverlays();						if (this.hasShape(this.candidate)) this.hideOverlayOnShape(this.candidate);			this.candidate = undefined;		}	},		doMouseUp: function(event, shape){		if (this.candidate == shape)			this.doAbstraction();        else if (this.isAbstractionTarget(shape)) {			// select abstraction candidate			if (this.hasShape(this.candidate)) this.hideOverlayOnShape(this.candidate);						this.candidate = shape;			this.new_center = this.candidate.bounds.center();						this.showOverlayOnShape(this.candidate, {fill: "green"});						// retrieve abstraction fragment from server			this.requestAbstractionComponent(shape);        }    },		/**	* Do abstraction	*/	doAbstraction: function(){        if (this.response['fragment'] instanceof Array && this.response['fragment'].length>0) {						// remove all except entry and exit			for (var i=0; i<this.response['fragment'].length; i++) {								if (this.response['fragment'][i]!=this.response['entry'] && 						this.response['fragment'][i]!=this.response['exit'])				{					var s = this.facade.getCanvas().getChildShapeByResourceId(this.response['fragment'][i]);									if (s) this.facade.deleteShape(s);				}			}									// create aggregating subprocess			this.createSubprocess(this.new_center,"SUBPROCESS");						this.response = undefined;			this.candidate = undefined;		}    },		isAbstractionTarget: function(shape){        return (shape.getStencil().id().search(/#(Task)$/) > -1) || (shape.getStencil().id().search(/#(CollapsedSubprocess)$/) > -1);    },		showOverlayOnShape: function(shape, attributes, node){        this.hideOverlayOnShape(shape);                this.facade.raiseEvent({            type: ORYX.CONFIG.EVENT_OVERLAY_SHOW,            id: "abs." + shape.resourceId,            shapes: [shape],            attributes: attributes,            node: (node ? node : null),            nodePosition: shape instanceof ORYX.Core.Edge ? "END" : "SE"        });    },        hideOverlayOnShape: function(shape){        this.facade.raiseEvent({            type: ORYX.CONFIG.EVENT_OVERLAY_HIDE,            id: "abs." + shape.resourceId        });    },		showNotification: function(ntitle,ntext){        new Ext.ux.Notification({            title: ntitle,            html: ntext,            iconCls: 'error',            hideDelay:  1000,            bodyStyle: {'text-align':'left'}        }).show(document);     },		doRequestAbstractionComponent: function(options){        new Ajax.Request(ORYX.CONFIG.ABSTRACTION, {            method: 'POST',            asynchronous: false,            parameters: {                erdf: this.facade.getERDF(),                shape_id: options.task.resourceId            },            onSuccess: options.onSuccess        });    },		requestAbstractionComponent: function(shape){        this.doRequestAbstractionComponent({			task : shape,			onSuccess: this.successOnRequestAbstractionComponent.bind(this)        });    },		/**	 * Abstraction request delivered response successfuly	 * - show abstraction fragment	 */	successOnRequestAbstractionComponent: function(request){		this.showNotification('DEBUG: Response', request.responseText);				this.response = request.responseText.evalJSON();				this.hideOverlays();		this.showAbstractionFragment();	},		hasShape: function(shape){		if(!this.isDefined(shape)) return false;		return true;  		//return !!this.facade.getCanvas().getChildShapeByResourceId(shape.resourceId);	},		isDefined: function(variable){		return (typeof(variable) == 'undefined') ? false : true;	},		/**	 * Hide all overlays on a current model	 */	hideOverlays: function(){        var els = this.facade.getCanvas().getChildShapes(true);        for (i = 0; i < els.size(); i++) {			if (els[i].resourceId != this.candidate.resourceId)				this.hideOverlayOnShape(els[i]);		}	},		/**	 * Show abstraction fragment	 * - highlight all elements of abstraction region	 */	showAbstractionFragment: function(){		if (this.response['fragment'] instanceof Array && this.response['fragment'].length > 0) {						for (var i=0; i<this.response['fragment'].length; i++) {				var s = this.facade.getCanvas().getChildShapeByResourceId(this.response['fragment'][i]);								if (s) {					this.facade.raiseEvent({						type: ORYX.CONFIG.EVENT_OVERLAY_SHOW,						id: "abs." + s.resourceId,						shapes: [s],						attributes: {stroke: 'red'}					});				}			}		}	},		/**	 * Creates subprocess	 * 	 * @param {Object} center	 * * @param {Object} title	 */	createSubprocess: function(coordinates,title){					var type = "CollapsedSubprocess";		// create a new Stencil				var ssn 	= this.facade.getStencilSets().keys()[0];								var stencil = ORYX.Core.StencilSet.stencil(ssn + type);		if(!stencil) return null;					// create a new Shape		var newShape = new ORYX.Core.Node({'eventHandlerCallback':this.facade.raiseEvent },stencil);		// add the shape to the canvas		this.facade.getCanvas().add(newShape);		// set properties		newShape.bounds.centerMoveTo(coordinates.x,coordinates.y);		newShape.setProperty("oryx-name",title);				// add control flow		var entry = this.facade.getCanvas().getChildShapeByResourceId(this.response['entry']);		var exit = this.facade.getCanvas().getChildShapeByResourceId(this.response['exit']);				if (entry && exit) { // link: entry -> newShape -> exit			this.createSequenceFlow(entry,newShape);			this.createSequenceFlow(newShape,exit);		}				// update canvas		this.facade.getCanvas().update();				return newShape;	},		createSequenceFlow: function(from_shape, to_shape) {		var type	= "SequenceFlow";		var ssn 	= this.facade.getStencilSets().keys()[0];								var stencil = ORYX.Core.StencilSet.stencil(ssn + type);				if(!stencil) return null;				// create a new Edge		var newEdge = new ORYX.Core.Edge({'eventHandlerCallback':this.facade.raiseEvent },stencil);				// add the edge to the canvas		this.facade.getCanvas().add(newEdge);				// set dockers		newEdge.dockers.first().setDockedShape(from_shape);		newEdge.dockers.first().setReferencePoint({x: from_shape.bounds.width() / 2.0, y: from_shape.bounds.height() / 2.0});				newEdge.dockers.last().setDockedShape(to_shape);		newEdge.dockers.last().setReferencePoint({x: to_shape.bounds.width() / 2.0, y: to_shape.bounds.height() / 2.0});				// update canvas		this.facade.getCanvas().update();				return newEdge;	}});