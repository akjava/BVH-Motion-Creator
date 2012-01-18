package com.akjava.gwt.bvhtools.client.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.akjava.bvh.client.BVH;
import com.akjava.bvh.client.BVHMotion;
import com.akjava.bvh.client.BVHNode;
import com.akjava.bvh.client.BVHParser;
import com.akjava.bvh.client.BVHParser.InvalidLineException;
import com.akjava.bvh.client.BVHWriter;
import com.akjava.bvh.client.NameAndChannel;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileHandler;
import com.akjava.gwt.html5.client.file.FileReader;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.lib.client.LogUtils;
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
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MixTool extends AbstractTool{

private File firstFile,secondFile;
	public MixTool(VerticalPanel panel){
		super(panel);

		
		FileUpload upload=new FileUpload();
		upload.getElement().setAttribute("multiple", "multiple");
		upload.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				JsArray<File> files=FileUtils.toFile(event.getNativeEvent());
				
				setFile(files,true);
				
			}
		});
		panel.add(upload);
		
		FileUpload upload2=new FileUpload();
		//upload.getElement().setAttribute("multiple", "multiple");
		upload2.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				JsArray<File> files=FileUtils.toFile(event.getNativeEvent());
				
				setFile(files,false);
				
			}
		});
		panel.add(upload2);
		HorizontalPanel labels=new HorizontalPanel();
		panel.add(labels);
		firstFileName=new Label();
		firstFileName.setWidth("200px");
		labels.add(firstFileName);
		secondFileName=new Label();
		secondFileName.setWidth("200px");
		labels.add(secondFileName);
		
		keepShort = new CheckBox("max frame is same as shorter one");
		keepShort.setValue(true);
		panel.add(keepShort);
		
		panel.add(new Label("Use BVH1 checks"));
		ScrollPanel scroll=new ScrollPanel();
		scroll.setSize("400px", "200px");
		boneCheckPanel=new VerticalPanel();
		scroll.setWidget(boneCheckPanel);
		panel.add(scroll);
		
		mixButton = new Button("Mix");
		
		mixButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				doMix();
			}
		});
		mixButton.setVisible(false);
		panel.add(mixButton);
		
		warnLabel = new Label("Error.bvh1 and bvh2 must have same bones");
		panel.add(warnLabel);
		warnLabel.setVisible(false);
		
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
		
		panel.add(createPlayButton("MixedData", textArea));
	}
	
	private Map<String,Boolean> useMap=new HashMap<String,Boolean>();
	
	VerticalPanel boneCheckPanel;
	
	
	protected void doMix() {
		BVH bvh1=bvhs.get(0);
		BVH bvh2=bvhs.get(1);
		
		BVH result=new BVH();
		result.setHiearchy(bvh1.getHiearchy());
		BVHMotion motion=new BVHMotion();
		motion.setFrameTime(bvh1.getFrameTime());
		result.setMotion(motion);
		
		int length=bvh1.getFrames();
		if(bvh2.getFrames()>length){
			length=bvh2.getFrames();
		}
		
		List<NameAndChannel> channel=bvh1.getNameAndChannels();
		
		for(int i=0;i<length;i++){
			double[] value1;
			double[] value2;
			if(i<bvh1.getFrames()){
				value1=bvh1.getFrameAt(i);
			}else{
				//TODO loop support;
				value1=new double[channel.size()];
			}
			
			if(i<bvh2.getFrames()){
				value2=bvh2.getFrameAt(i);
			}else{
				//TODO loop support;
				value2=new double[channel.size()];
			}
			
			double[] values=new double[channel.size()];
			for(int j=0;j<channel.size();j++){
				String name=channel.get(j).getName();
				if(useMap.get(name)){
					values[j]=value1[j];
				}else{
					values[j]=value2[j];
				}
			}
			motion.add(values);
		}
		
		
		
		
		motion.syncFrames();

		
		BVHWriter writer=new BVHWriter();
		
		String text=writer.writeToString(result);
		textArea.setText(text);
		
	}








	private TextArea textArea;

	private CheckBox keepShort;

	private void parseBVH(String bvhText){
		BVHParser parser=new BVHParser();
		try {
			BVH bvh=parser.parse(bvhText);
			bvhs.add(bvh);
		} catch (InvalidLineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setFile(JsArray<File> files,boolean isFirst){
		
		LogUtils.log("file-size:"+files.length());
		if(isFirst){
			firstFile=files.get(0);
			if(files.length()>1){
				secondFile=files.get(1);
			}
		}else{
			secondFile=files.get(0);
		}
		
		
		updateLabel();
		if(firstFile!=null && secondFile!=null){
		loadFiles();
		}
		
	}
	
	private void updateLabel(){
		if(firstFile!=null){
		firstFileName.setText(firstFile.getFileName());
		}
		if(secondFile!=null){
		secondFileName.setText(secondFile.getFileName());
		}
	}
	private Label firstFileName,secondFileName;
	private List<BVH> bvhs;
	private Label warnLabel;
	private Button mixButton;
	private void loadFiles(){
		bvhs=new ArrayList<BVH>();
		final List<File> files=new ArrayList<File>();
		files.add(firstFile);
		files.add(secondFile);
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
		//doStrip();
		//check same and
		boolean same=isSameNode(bvhs.get(0), bvhs.get(1));
		if(!same){
			warnLabel.setVisible(true);
			mixButton.setVisible(false);
		}else{
			warnLabel.setVisible(false);
			mixButton.setVisible(true);
		}
		useMap.clear();
		//create check
		boneCheckPanel.clear();
		BVH bvh=bvhs.get(0);
		for(BVHNode node:bvh.getNodeList()){
			final CheckBox check=new CheckBox();
			check.setText(node.getName());
			check.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					if(check.getValue()){
						useMap.put(check.getText(), true);
					}else{
						useMap.put(check.getText(), false);
					}
				}
			});
			check.setValue(true);
			useMap.put(node.getName(), true);
			boneCheckPanel.add(check);
		}
	}
	public static boolean isSameNode(BVH bvh1,BVH bvh2){
		boolean same=true;
		List<BVHNode> node1=bvh1.getNodeList();
		List<BVHNode> node2=bvh2.getNodeList();
		if(node1.size()!=node2.size()){
			return false;
		}
		for(int i=0;i<node1.size();i++){
			if(!node1.get(i).getName().equals(node1.get(i).getName())){
				return false;
			}
		}
		return same;
	}
}
