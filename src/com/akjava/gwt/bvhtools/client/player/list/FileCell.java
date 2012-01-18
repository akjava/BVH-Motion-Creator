package com.akjava.gwt.bvhtools.client.player.list;

import com.akjava.gwt.html5.client.file.File;
import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;

public class FileCell extends AbstractSafeHtmlCell<File>{

	public FileCell(){
		 super(FileRenderer.getInstance());
	}
	public FileCell(SafeHtmlRenderer<File> renderer) {
		super(renderer);
	}

	@Override
	protected void render(com.google.gwt.cell.client.Cell.Context context,
			SafeHtml value, SafeHtmlBuilder sb) {
		 if (value != null) {
		      sb.append(value);
		    }
	}

}
