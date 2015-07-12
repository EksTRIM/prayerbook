package com.tshevchuk.prayer.data;

import android.text.TextUtils;

public class MenuItemPrayer extends MenuItemBase {
	private final String fileName;
	private String source;
	private String fullName;
	private Type type = Type.HtmlInTextView;
	private String htmlLinkAnchor;
	public MenuItemPrayer(int id, String name, String fileName) {
		super(id, name);
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getAbout() {
		return "Джерело тексту: " + source;
	}

	public String getSource() {
		return source;
	}

	@SuppressWarnings("UnusedReturnValue")
	public MenuItemPrayer setSource(String source) {
		this.source = source;
		return this;
	}

	public String getFullName() {
		if (TextUtils.isEmpty(fullName)) {
			return getName();
		}
		return fullName;
	}

	public Type getType() {
		return type;
	}

	public MenuItemPrayer setType(Type type) {
		this.type = type;
		return this;
	}

	public String getHtmlLinkAnchor() {
		return htmlLinkAnchor;
	}

	public void setHtmlLinkAnchor(String htmlLinkAnchor) {
		this.htmlLinkAnchor = htmlLinkAnchor;
	}

	public enum Type {
		Text, HtmlInTextView, HtmlInWebView
	}
}