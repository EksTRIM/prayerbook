package com.tshevchuk.prayer.presentation.home;

import com.tshevchuk.prayer.presentation.base.BaseView;

/**
 * Created by taras on 10.04.16.
 */
public interface HomeView extends BaseView {
    boolean handleUpAction();
    boolean handleBackAction();
    void updateAppRater();
}
