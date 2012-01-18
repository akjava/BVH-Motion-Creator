package com.akjava.gwt.bvhtools.client.player.list;

import java.util.ArrayList;
import java.util.List;

import com.akjava.gwt.lib.client.widget.cell.util.WidgetUtils;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

//this is easy to scroll than CellList system
//use cell list
public class DataListCell<T> extends ScrollPanel{
List<T> datas=new ArrayList<T>();
T selected;
private VerticalPanel panel;

DataListRenderer<T> renderer;
private String styleName="selected";
private ChangeSelectionListener<T> listener;
public ChangeSelectionListener<T> getListener() {
	return listener;
}


public void setListener(ChangeSelectionListener<T> listener) {
	this.listener = listener;
}


public DataListCell(DataListRenderer<T> renderer){
	panel=new VerticalPanel();
	this.renderer=renderer;
	setWidget(panel);
}


public void setDatas(List<T> datas){
	this.datas=datas;
	updateWidget();
}

public T getSelection(){
	return selected;
}
public void setSelection(T data){
	this.selected=data;
	int csize=panel.getWidgetCount();
	for(int i=0;i<csize;i++){
		Widget w=panel.getWidget(i);
		w.removeStyleName(styleName);
	}
	int select=datas.indexOf(data);
	if(select!=-1){
		panel.getWidget(select).addStyleName(styleName);
	}
	
	int y=WidgetUtils.calculateScrollY(panel, select);
	this.setVerticalScrollPosition(y-getOffsetHeight()/2);
	if(listener!=null){
		listener.onChangeSelection(data);
	}
}


private void updateWidget(){
	panel.clear();
	for(T data:datas){
		Widget widget=renderer.createWidget(data,this);
		panel.add(widget);
		
	}
}

public interface DataListRenderer<T> {
	public Widget createWidget(T data,DataListCell<T> dataList);
}
public interface ChangeSelectionListener<T>{
	public void onChangeSelection(T data);
}

}
