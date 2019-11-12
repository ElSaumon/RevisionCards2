package com.saumon.revisioncards2.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.injection.Injection;
import com.saumon.revisioncards2.injections.ViewModelFactory;
import com.saumon.revisioncards2.utils.CardViewModel;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {
    public CardViewModel cardViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(this.getLayoutContentViewID());
        ButterKnife.bind(this);
        configureToolbar();
        configureViewModel();
    }

    public abstract int getLayoutContentViewID();

    protected void configureToolbar() {
        Toolbar toolbar = getToolbar();
        toolbar.setTitle(getToolbarTitle());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected Toolbar getToolbar() {
        return findViewById(R.id.toolbar);
    }

    protected abstract String getToolbarTitle();

    private void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory(this);
        cardViewModel = ViewModelProviders.of(this, viewModelFactory).get(CardViewModel.class);
    }
}
