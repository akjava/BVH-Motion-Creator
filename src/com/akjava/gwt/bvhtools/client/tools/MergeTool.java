package com.akjava.gwt.bvhtools.client.tools;

import java.util.ArrayList;
import java.util.List;

import com.akjava.bvh.client.BVH;
import com.akjava.bvh.client.BVHMotion;
import com.akjava.bvh.client.BVHParser;
import com.akjava.bvh.client.BVHParser.InvalidLineException;
import com.akjava.bvh.client.BVHWriter;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileHandler;
import com.akjava.gwt.html5.client.file.FileReader;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MergeTool extends AbstractTool {

	public MergeTool(VerticalPanel panel){
		super(panel);

		
		FileUpload upload=new FileUpload();
		upload.getElement().setAttribute("multiple", "multiple");
		upload.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				JsArray<File> files=FileUtils.toFile(event.getNativeEvent());
				
				setFile(files);
				
			}
		});
		panel.add(upload);
		
		skipFirst = new CheckBox("Skip First frame after second bvh");
		skipFirst.setValue(true);
		panel.add(skipFirst);
		
		HorizontalPanel buttons=new HorizontalPanel();
		panel.add(buttons);
		buttons.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		buttons.setWidth("400px");
		Button selectAll=new Button("Select Text");
		buttons.add(selectAll);
		textArea = new TextArea();
	
		textArea.setSize("400px", "100px");
		textArea.setReadOnly(true);
		//textArea.setStylePrimaryName("nowrap"); ,wrap  replace some char and make a problem
		panel.add(textArea);
		selectAll.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				textArea.selectAll();
			}
		});
		
		panel.add(createPlayButton("Merged Data", textArea));
	}
	

	
	
	
	
	List<BVH> bvhList;

	private TextArea textArea;

	private CheckBox skipFirst;

	private void parseBVH(String bvhText){
		BVHParser parser=new BVHParser();
		try {
			BVH bvh=parser.parse(bvhText);
			GWT.log("parsed");
			//calcurate
			//List<ResultData> datas=new ArrayList<ResultData>();
			
			
			
			bvhList.add(bvh);
			
			
		} catch (InvalidLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setFile(JsArray<File> files){
		GWT.log("set-file:"+files.get(0));
		
		bvhList=new ArrayList<BVH>();
		
		
		List<File> fileList=new ArrayList<File>();
		for(int i=0;i<files.length();i++){
			fileList.add(files.get(i));
		}
		
		loadFiles(fileList);
	}
	
	private void loadFiles(final List<File> files){
		final FileReader reader=FileReader.createFileReader();
		reader.setOnLoad(new FileHandler() {
			@Override
			public void onLoad() {
				parseBVH(reader.getResultAsString());
				if(files.size()>0){
					File f=files.remove(0);
				
					reader.readAsText(f,"utf-8");
				}else{//last
					doAllFileAction();
				}
			}
		});
		reader.readAsText(files.remove(0),"utf-8");
	}
	
	
	private boolean skipFirstValue;

	protected void doAllFileAction() {
		skipFirstValue=skipFirst.getValue();
		BVH result=new BVH();
		result.setMotion(new BVHMotion());
		result.getMotion().setFrameTime(bvhList.get(0).getFrameTime());
		result.setHiearchy(bvhList.get(0).getHiearchy());
		
		for(BVH bvh:bvhList){
			BVHMotion motion=bvh.getMotion();
			boolean first=true;
			for(double[] mvalue:motion.getMotions()){
				
				if(first){
					first=false;
					if(skipFirstValue){
						continue;
					}
					
				}
				result.getMotion().add(mvalue);
			}
		}
		
		result.getMotion().syncFrames();

		BVHWriter writer=new BVHWriter();
		
		String text=writer.writeToString(result);
		textArea.setText(text);
	}
}
