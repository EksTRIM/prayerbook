package com.tshevchuk.prayer.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tshevchuk.prayer.HomeActivity;
import com.tshevchuk.prayer.PrayerBookApplication;
import com.tshevchuk.prayer.PreferenceManager;
import com.tshevchuk.prayer.R;
import com.tshevchuk.prayer.data.MenuItemBase;
import com.tshevchuk.prayer.data.MenuItemPrayer;
import com.tshevchuk.prayer.data.MenuItemPrayer.Type;

public class HtmlViewFragment extends FragmentBase {
	private List<MenuItemPrayer> prayers = new ArrayList<MenuItemPrayer>();
	private HomeActivity activity;

	private WebView wvContent;

	public static HtmlViewFragment getInstance(MenuItemPrayer prayer) {
		HtmlViewFragment f = new HtmlViewFragment();
		Bundle b = new Bundle();
		b.putSerializable("prayer", prayer);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (HomeActivity) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		prayers.add((MenuItemPrayer) getArguments().getSerializable("prayer"));
	};

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null) {
			WebSettings settings = wvContent.getSettings();
			settings.setDefaultTextEncodingName("utf-8");
			settings.setDefaultFontSize(PreferenceManager.getInstance()
					.getFontSizeSp());
			settings.setJavaScriptEnabled(true);
			wvContent.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					activity.setProgressBarIndeterminateVisibility(newProgress < 100);
				}
			});

			wvContent.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url.startsWith("prayerbook://")) {
						String params[] = url.substring(13).split("#");
						int id = Integer.valueOf(params[0]);
						MenuItemBase mi = PrayerBookApplication.getInstance()
								.getCatalog().getMenuItemById(id);
						if (mi instanceof MenuItemPrayer) {
							if (params.length == 2
									&& !TextUtils.isEmpty(params[1])) {
								((MenuItemPrayer) mi)
										.setHtmlLinkAnchor(params[1]);
							}
						}
						if (mi instanceof MenuItemPrayer
								&& ((MenuItemPrayer) mi).getType() == Type.HtmlInWebView) {
							prayers.add((MenuItemPrayer) mi);
							loadPrayer((MenuItemPrayer) mi);
						} else {
							activity.displayMenuItem(mi);
						}
						return false;
					}
					return super.shouldOverrideUrlLoading(view, url);
				}

				public void onPageFinished(WebView view, String url) {
					String anchor = null;
					if (url.startsWith("file:///android_asset/")) {
						anchor = Uri.parse(url).getEncodedFragment();
					}
					for (int i = prayers.size() - 1; i >= 0; --i) {
						MenuItemPrayer p = prayers.get(i);
						String u = "file:///android_asset/" + p.getFileName();
						if (url.equals(u) || url.startsWith(u + "#")) {
							while (prayers.size() > i + 1) {
								prayers.remove(prayers.size() - 1);
							}
							break;
						}
					}

					getActivity().getActionBar()
							.setTitle(getPrayer().getName());

					if (!TextUtils.isEmpty(anchor)) {
						StringBuilder sb = new StringBuilder();
						sb.append(
								"javascript:function prayerbook_scrollToElement(id) {")
								.append("var elem = document.getElementById(id);")
								.append("var x = 0; var y = 0;")
								.append("while (elem != null) {")
								// .append("  x += elem.offsetLeft;")
								.append("  y += elem.offsetTop;")
								.append("  elem = elem.offsetParent;  }")
								.append("window.scrollTo(x, y);  };")
								.append(" console.log ( '#someButton was clicked' );")
								.append("prayerbook_scrollToElement('")
								.append(anchor).append("');");

						view.loadUrl(sb.toString());
					}
				}

				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view,
						String url) {
					InputStream stream = inputStreamForAndroidResource(url);
					if (stream != null) {
						return new WebResourceResponse(null, null, stream);
					}
					return super.shouldInterceptRequest(view, url);
				}

				private InputStream inputStreamForAndroidResource(String url) {
					final String ANDROID_ASSET = "file:///android_asset/";

					if (url.startsWith(ANDROID_ASSET)) {
						url = url.replaceFirst(ANDROID_ASSET, "");
						try {
							AssetManager assets = activity.getAssets();
							Uri uri = Uri.parse(url);
							return assets.open(uri.getPath(),
									AssetManager.ACCESS_STREAMING);
						} catch (IOException e) {
						}
					}
					return null;
				}
			});

			activity.setProgressBarIndeterminateVisibility(true);
			loadPrayer(getPrayer());
		} else {
			wvContent.restoreState(savedInstanceState);
		}

	}

	private void loadPrayer(MenuItemPrayer p) {
		String url = "file:///android_asset/" + p.getFileName();
		if (!TextUtils.isEmpty(p.getHtmlLinkAnchor())) {
			url += "#" + p.getHtmlLinkAnchor();
		}
		wvContent.loadUrl(url);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.f_html_view, container, false);
		wvContent = (WebView) v.findViewById(R.id.wv_content);

		return v;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		wvContent = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		wvContent.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		wvContent.onResume();
		getActivity().getActionBar().setTitle(getPrayer().getName());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.actionbar_textviewfragment, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mi_about_prayer:
			((HomeActivity) activity).displayFragment(
					AboutPrayerFragment.getInstance(getPrayer()), 0, null);
			((HomeActivity) activity).sendAnalyticsOptionsMenuEvent("Опис",
					String.format("#%d %s", getPrayer().getId(), getPrayer()
							.getName()));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		wvContent.saveState(outState);
	}

	@Override
	public boolean goBack() {
		if (wvContent.canGoBack()) {
			if (prayers.size() > 1) {
				prayers.remove(prayers.size() - 1);
			}
			wvContent.goBack();
			return true;
		}
		return false;
	}

	@Override
	public boolean isSameScreen(Fragment f) {
		if (getClass().equals(f.getClass())) {
			MenuItemPrayer p1 = getPrayer();
			if (p1 == null) {
				p1 = (MenuItemPrayer) getArguments().getSerializable("prayer");
			}
			MenuItemPrayer p2 = (MenuItemPrayer) f.getArguments()
					.getSerializable("prayer");
			return p1.getId() == p2.getId();
		}
		return false;
	}

	private MenuItemPrayer getPrayer() {
		return prayers.get(prayers.size() - 1);
	}
}
