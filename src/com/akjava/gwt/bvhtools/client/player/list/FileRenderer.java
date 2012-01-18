package com.akjava.gwt.bvhtools.client.player.list;

import com.akjava.gwt.html5.client.file.File;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;

public class FileRenderer implements SafeHtmlRenderer<File>{

	  private static FileRenderer instance;

	  public static FileRenderer getInstance() {
	    if (instance == null) {
	      instance = new FileRenderer();
	    }
	    return instance;
	  }
	@Override
	public SafeHtml render(File object) {
		 return (object == null) ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromString(object.getFileName());
	}

	@Override
	public void render(File object, SafeHtmlBuilder builder) {
		builder.append(SafeHtmlUtils.fromString(object.getFileName()));
	}

}
