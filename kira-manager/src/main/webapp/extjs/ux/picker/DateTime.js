/*
 * 带时间选择的日历选择器
 * 转载请注明来自于gogo1217.iteye.com
*/
Ext.define('Ext.ux.picker.DateTime', {
    extend: 'Ext.picker.Date',//继承于 Ext.picker.Date
    alias: 'widget.dateptimeicker',//添加xtype dateptimeicker
    okText:'确定',//确认按钮文字描述
    okTip:'确定',//确认按钮提示内容

    renderTpl: [
        '<div id="{id}-innerEl" role="grid">',
            '<div role="presentation" class="{baseCls}-header">',
                '<a id="{id}-prevEl" class="{baseCls}-prev {baseCls}-arrow" href="#" role="button" title="{prevText}" hidefocus="on" ></a>',
                '<div class="{baseCls}-month" id="{id}-middleBtnEl">{%this.renderMonthBtn(values, out)%}</div>',
                '<a id="{id}-nextEl" class="{baseCls}-next {baseCls}-arrow" href="#" role="button" title="{nextText}" hidefocus="on" ></a>',
            '</div>',
            '<table id="{id}-eventEl" class="{baseCls}-inner" cellspacing="0" role="presentation">',
                '<thead role="presentation"><tr role="presentation">',
                    '<tpl for="dayNames">',
                        '<th role="columnheader" class="{parent.baseCls}-column-header" title="{.}">',
                            '<div class="{parent.baseCls}-column-header-inner">{.:this.firstInitial}</div>',
                        '</th>',
                    '</tpl>',
                '</tr></thead>',
                '<tbody role="presentation"><tr role="presentation">',
                    '<tpl for="days">',
                        '{#:this.isEndOfWeek}',
                        '<td role="gridcell" id="{[Ext.id()]}">',
                           '<a role="presentation" hidefocus="on" class="{parent.baseCls}-date" href="#"></a>',
                        '</td>',
                    '</tpl>',
                '</tr></tbody>',
            '</table>',

            //指定时分秒渲染框架
            '<table id="{id}-timeEl" style="table-layout:auto;width:auto;margin:0 3px;" class="x-datepicker-inner" cellspacing="0">',
                '<tbody><tr>',
                    '<td>{%this.renderHourBtn(values,out)%}</td>',
                    '<td>{%this.renderMinuteBtn(values,out)%}</td>',
                    '<td>{%this.renderSecondBtn(values,out)%}</td>',
                '</tr></tbody>',
            '</table>',

            '<tpl if="showToday">',
                //添加一个确认按钮渲染
                '<div id="{id}-footerEl" role="presentation" class="{baseCls}-footer">{%this.renderOkBtn(values, out)%}{%this.renderTodayBtn(values, out)%}</div>',
            '</tpl>',
        '</div>',
        {
            firstInitial: function(value) {
                return Ext.picker.Date.prototype.getDayInitial(value);
            },
            isEndOfWeek: function(value) {
                // convert from 1 based index to 0 based
                // by decrementing value once.
                value--;
                var end = value % 7 === 0 && value !== 0;
                return end ? '</tr><tr role="row">' : '';
            },
            renderTodayBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.todayBtn.getRenderTree(), out);
            },
            renderMonthBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.monthBtn.getRenderTree(), out);
            },

            //指定渲染方法调用
            renderHourBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.hourBtn.getRenderTree(), out);//根据组件获得组件的html输出
            },
            renderMinuteBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.minuteBtn.getRenderTree(), out);
            },
            renderSecondBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.secondBtn.getRenderTree(), out);
            },
            renderOkBtn: function(values, out) {
                Ext.DomHelper.generateMarkup(values.$comp.okBtn.getRenderTree(), out);
            }
        }
    ],

    beforeRender: function () {
        var me = this,_$Number=Ext.form.field.Number;
        //在组件渲染之前，将自定义添加的时、分、秒和确认按钮进行初始化
        //组件宽度可能需要调整下，根据使用的theme不同，宽度需要调整
        me.hourBtn=new _$Number({
            minValue:0,
            maxValue:23,
            step:1,
            width:45
        });
        me.minuteBtn=new _$Number({
            minValue:0,
            maxValue:59,
            step:1,
            width:60,
            labelWidth:10,
            fieldLabel:'&nbsp;'
        });
        me.secondBtn=new _$Number({
            minValue:0,
            maxValue:59,
            step:1,
            width:60,
            labelWidth:10,
            fieldLabel:'&nbsp;'//在组件之前渲染 ':'
        });

        me.okBtn = new Ext.button.Button({
            ownerCt: me,
            ownerLayout: me.getComponentLayout(),
            text: me.okText,
            tooltip: me.okTip,
            tooltipType:'title',
            handler:me.okHandler,//确认按钮的事件委托
            scope: me
        });
        me.callParent();
    },
    
    finishRenderChildren: function () {
        var me = this;
        //组件渲染完成后，需要调用子元素的finishRender，从而获得事件绑定
        me.hourBtn.finishRender();
        me.minuteBtn.finishRender();
        me.secondBtn.finishRender();
        me.okBtn.finishRender();
        me.callParent();
    },

    /**
     * 确认 按钮触发的调用
     */
    okHandler : function(){
        var me = this,
            btn = me.okBtn;
            //alert(this.getValue())
        if(btn && !btn.disabled){
            me.setValue(this.getValue());
            me.fireEvent('select', me, me.value);
            me.onSelect();
        }
        return me;
    },

    /**
     * 覆盖了父类的方法，因为父类中是根据时间的getTime判断的，因此需要对时、分、秒分别值为0才能保证当前值的日期选择
     * @private
     * @param {Date} date The new date
     */
    selectedUpdate: function(date){
        this.callParent([Ext.Date.clearTime(date,true)]);
    },

    /**
     * 更新picker的显示内容，需要同时更新时、分、秒输入框的值
     * @private
     * @param {Date} date The new date
     * @param {Boolean} forceRefresh True to force a full refresh
     */
    update : function(date, forceRefresh){
        var me = this;
        me.hourBtn.setValue(date.getHours());
        me.minuteBtn.setValue(date.getMinutes());
        me.secondBtn.setValue(date.getSeconds());

        return this.callParent(arguments);
    },

    /**
     * 从picker选中后，赋值时，需要从时、分、秒也获得当前值
     * datetimefield也会调用这个方法对picker初始化，因此添加一个isfixed参数。
     * @param {Date} date The new date
     * @param {Boolean} isfixed True 时，忽略从时分秒中获取值
    */
    setValue : function(date, isfixed){
        var me = this;
        if(isfixed!==true){
            date.setHours(me.hourBtn.getValue());
            date.setMinutes(me.minuteBtn.getValue());
            date.setSeconds(me.secondBtn.getValue());
        }
        me.value=date;
        me.update(me.value);
        return me;
    },

    // @private
    // @inheritdoc
    beforeDestroy : function() {
        var me = this;

        if (me.rendered) {
            //销毁组件时，也需要销毁自定义的控件
            Ext.destroy(
                me.hourBtn,
                me.minuteBtn,
                me.secondBtn,
                me.okBtn
            );
        }
        me.callParent();
    }
},
function() {
    var proto = this.prototype,
        date = Ext.Date;

    proto.monthNames = date.monthNames;
    proto.dayNames   = date.dayNames;
    proto.format     = date.defaultFormat;
});
