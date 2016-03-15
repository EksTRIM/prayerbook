package com.tshevchuk.prayer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tshevchuk.prayer.PrayerLoader;
import com.tshevchuk.prayer.PreferenceManager;
import com.tshevchuk.prayer.R;
import com.tshevchuk.prayer.ResponsiveScrollView;
import com.tshevchuk.prayer.ResponsiveScrollView.OnEndScrollListener;
import com.tshevchuk.prayer.UIUtils;
import com.tshevchuk.prayer.data.MenuItemPrayer;
import com.tshevchuk.prayer.data.MenuItemPrayer.Type;

public class TextViewFragment extends TextFragmentBase implements
		LoaderManager.LoaderCallbacks<CharSequence> {
	private final static int LOADER_ID_LOAD_PRAYER = 1;

	private MenuItemPrayer prayer;
	private CharSequence htmlContent;
	private Integer firstVisibleCharacterOffset = null;

	private TextView tvContent;
	private ResponsiveScrollView svScroll;

	public static TextViewFragment getInstance(MenuItemPrayer prayer) {
		TextViewFragment f = new TextViewFragment();
		Bundle b = new Bundle();
		b.putSerializable("prayer", prayer);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prayer = (MenuItemPrayer) getArguments().getSerializable("prayer");
		if (savedInstanceState != null) {
			firstVisibleCharacterOffset = savedInstanceState
					.getInt("firstVisibleCharOffset");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.f_text_view, container, false);

		tvContent = (TextView) v.findViewById(R.id.tv_content);
		svScroll = (ResponsiveScrollView) v.findViewById(R.id.svScroll);

		if (!getResources().getBoolean(R.bool.has_sw480)) {
			svScroll.setOnEndScrollListener(new OnEndScrollListener() {
				@Override
				public void onEndScroll(boolean moveContentUp, boolean isFling,
						int dy) {
					boolean show = false;
					boolean hide = false;

					if (svScroll.getScrollY() < UIUtils.dpToPx(80))
						show = true;
					if (!moveContentUp && dy < -UIUtils.dpToPx(30))
						show = true;
					if (!show && moveContentUp && isFling)
						hide = true;

					ActionBar ab = activity.getSupportActionBar();
					if (ab != null) {
						if (show && !ab.isShowing())
							ab.show();
						else if (hide && ab.isShowing())
							ab.hide();
					}
				}
			});

			svScroll.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
						// is bottom
						View view = svScroll.getChildAt(svScroll
								.getChildCount() - 1);
						ActionBar actionBar = activity.getSupportActionBar();
						if (actionBar != null
								&& (svScroll.getHeight() + svScroll.getScrollY())
								>= view.getBottom() - actionBar.getHeight()) {
							actionBar.show();
						}
					}
					return false;
				}
			});
		}

		activity.setProgressBarIndeterminateVisibility(true);
		if (htmlContent == null) {
			Bundle params = new Bundle();
			params.putString(PrayerLoader.PARAM_ASSET_FILE_NAME,
					prayer.getFileName());
			params.putBoolean(PrayerLoader.PARAM_IS_HTML,
					prayer.getType() == Type.HtmlInTextView);
			getLoaderManager().initLoader(LOADER_ID_LOAD_PRAYER, params, this);
		} else {
			updateHtmlContent();
		}

		return v;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		firstVisibleCharacterOffset = getFirstVisibleCharacterOffset();
	}

	@Override
	public void onResume() {
		super.onResume();
		int fontSizeSp = PreferenceManager.getInstance().getFontSizeSp();
		tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (firstVisibleCharacterOffset == null) {
			outState.putInt("firstVisibleCharOffset",
					getFirstVisibleCharacterOffset());
		} else {
			outState.putInt("firstVisibleCharOffset",
					firstVisibleCharacterOffset);
		}
	}

	private int getFirstVisibleCharacterOffset() {
		final int firstVisibleLineOffset = tvContent.getLayout()
				.getLineForVertical(svScroll.getScrollY());
		return tvContent.getLayout()
				.getLineStart(firstVisibleLineOffset);
	}

	@Override
	public boolean isSameScreen(Fragment f) {
		if (getClass().equals(f.getClass())) {
			MenuItemPrayer p1 = (MenuItemPrayer) getArguments()
					.getSerializable("prayer");
			MenuItemPrayer p2 = (MenuItemPrayer) f.getArguments()
					.getSerializable("prayer");
			return p1 != null && p2 != null && p1.getId() == p2.getId();
		}
		return false;
	}

	@Override
	public Loader<CharSequence> onCreateLoader(int id, Bundle args) {
		return new PrayerLoader(activity, args);
	}

	@Override
	public void onLoadFinished(Loader<CharSequence> loader, CharSequence data) {
		htmlContent = data;
		updateHtmlContent();
	}

	@Override
	public void onLoaderReset(Loader<CharSequence> loader) {
	}

	private void updateHtmlContent() {
		if (htmlContent != null && tvContent != null) {
			tvContent.setText(htmlContent);
			activity.setProgressBarIndeterminateVisibility(false);

			svScroll.post(new Runnable() {
				public void run() {
					if (firstVisibleCharacterOffset != null) {
						final int firstVisableLineOffset = tvContent
								.getLayout().getLineForOffset(
										firstVisibleCharacterOffset);
						final int pixelOffset = tvContent.getLayout()
								.getLineTop(firstVisableLineOffset);
						svScroll.scrollTo(0, pixelOffset);
						firstVisibleCharacterOffset = null;
					}
				}
			});
		}
	}

	@Override
	public MenuItemPrayer getMenuItem() {
		return prayer;
	}
}
