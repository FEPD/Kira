Ext.Loader.setConfig({enabled:true});//开启动态加载
Ext.application({
	name : 'Kira',
	appFolder : 'app',
	launch : function() {
		application = this;
        this.loadModule(['MenuTree']);
	}
});