package com.akjava.gwt.bvhtools.client.tools;

import com.akjava.gwt.bvhtools.client.BVHTools;
import com.akjava.gwt.bvhtools.client.file.TextAreaDataContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class AbstractTool  {
	protected VerticalPanel panel;
	protected TextAreaDataContainer bvhContainer;
	
	public AbstractTool(VerticalPanel panel){
		this.panel=panel;
	}
	public Button createPlayButton(String name,final TextArea textArea){
		
		if(bvhContainer==null){
		bvhContainer=new TextAreaDataContainer(name,textArea);
		}
		Button play=new Button("Play");
		play.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(textArea.getText().isEmpty()){
					return;//do nothing
				}
				BVHTools.getInstance().addBVHData(bvhContainer);
			}
		});
		return play;
	}
}
