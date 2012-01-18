package com.akjava.gwt.bvhtools.client.player.list;

import com.akjava.gwt.html5.client.file.File;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class BVHFileWidget extends HorizontalPanel {
private File file;
private DataListCell<File> dataList;
public BVHFileWidget(File f,DataListCell<File> data){
	this.file=f;
	this.dataList=data;
	Label label=new Label(file.getFileName());
	
	label.setStylePrimaryName("bvhlabel");
	add(label);
	label.addClickHandler(new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			dataList.setSelection(file);
		}
	});
}
public File getFile(){
	return file;
}
}
