package com.akjava.gwt.bvhtools.client.tools;

import java.util.ArrayList;
import java.util.List;

import com.akjava.bvh.client.BVH;
import com.akjava.bvh.client.BVHMotion;
import com.akjava.bvh.client.BVHParser;
import com.akjava.bvh.client.BVHParser.InvalidLineException;
import com.akjava.bvh.client.BVHWriter;
import com.akjava.gwt.bvhtools.client.BVHTools;
import com.akjava.gwt.bvhtools.client.file.TextAreaDataContainer;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileHandler;
import com.akjava.gwt.html5.client.file.FileReader;
import com.akjava.gwt.html5.client.file.FileUploadForm;
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

public class StripTool extends AbstractTool{

	public StripTool(VerticalPanel panel){
		super(panel);

		
		final FileUploadForm upload=new FileUploadForm();
		//upload.getElement().setAttribute("multiple", "multiple");
		upload.getFileUpload().addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				JsArray<File> files=FileUtils.toFile(event.getNativeEvent());
				
				setFile(files);
				upload.reset();
			}
		});
		panel.add(upload);
		
		keepFirst = new CheckBox("keep first frame(usually pose frame)");
		keepFirst.setValue(true);
		panel.add(keepFirst);
		
		Button restrip=new Button("Re Strip");
		
		restrip.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doStrip();
			}
		});
		panel.add(restrip);
		
		
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
		
		panel.add(createPlayButton("Striped Data", textArea));
		
	}
	
	
	
	
	
	
	protected void doStrip() {
		BVH bvh=bvhList.get(0);
		BVHMotion motion=new BVHMotion();
		motion.setFrameTime(bvh.getFrameTime());
		double[] value=bvh.getFrameAt(0);
		if(!keepFirst.getValue()){
			for(int i=0;i<value.length;i++){
				value[i]=0;
			}
		}
		motion.add(value);
		
		motion.syncFrames();

		BVH result=new BVH();
		result.setHiearchy(bvh.getHiearchy());
		result.setMotion(motion);
		
		BVHWriter writer=new BVHWriter();
		
		String text=writer.writeToString(result);
		textArea.setText(text);
	}






	List<BVH> bvhList;

	private TextArea textArea;

	private CheckBox keepFirst;

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
	
	


	protected void doAllFileAction() {
		doStrip();
	}
}
