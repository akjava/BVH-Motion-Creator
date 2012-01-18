package com.akjava.gwt.bvhtools.client.file;

import com.google.gwt.user.client.ui.TextArea;

public class TextAreaDataContainer implements BVHDataContainer{
	private String  name;
	private TextArea textArea;
	public TextAreaDataContainer(String name,TextArea textArea){
		this.name=name;
		this.textArea=textArea;
	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void readText(BVHDataListener listener) {
		listener.dataLoaded(textArea.getText());
	}

}
