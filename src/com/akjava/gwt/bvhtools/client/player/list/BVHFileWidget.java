package com.akjava.gwt.bvhtools.client.player.list;

import com.akjava.gwt.bvhtools.client.file.BVHDataContainer;
import com.akjava.gwt.html5.client.file.File;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class BVHFileWidget extends HorizontalPanel {
private BVHDataContainer file;
private DataListCell<BVHDataContainer> dataList;
public BVHFileWidget(BVHDataContainer f,DataListCell<BVHDataContainer> data){
	this.file=f;
	this.dataList=data;
	Label label=new Label(file.getName());
	
	label.setStylePrimaryName("bvhlabel");
	add(label);
	label.addClickHandler(new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			dataList.setSelection(file);
		}
	});
}
public BVHDataContainer getFile(){
	return file;
}
}
