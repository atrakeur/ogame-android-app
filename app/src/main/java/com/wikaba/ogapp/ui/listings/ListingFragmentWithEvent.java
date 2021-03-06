/*
	Copyright 2015 Kevin Le Perf

	This file is part of Ogame on Android.

	Ogame on Android is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Ogame on Android is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wikaba.ogapp.ui.listings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.OgameAgent;
import com.wikaba.ogapp.agent.constants.ItemRepresentationConstant;
import com.wikaba.ogapp.agent.factories.ItemRepresentationFactory;
import com.wikaba.ogapp.events.abstracts.OnAbstractListInformationLoaded;
import com.wikaba.ogapp.events.contents.OnBuildingLoaded;
import com.wikaba.ogapp.events.contents.OnDefensesLoaded;
import com.wikaba.ogapp.events.contents.OnResearchsLoaded;
import com.wikaba.ogapp.events.contents.OnResourceRequestToLoadEvent;
import com.wikaba.ogapp.events.contents.OnResourcesLoaded;
import com.wikaba.ogapp.events.contents.OnShipyardsLoaded;
import com.wikaba.ogapp.ui.main.HomeActivity;
import com.wikaba.ogapp.utils.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class ListingFragmentWithEvent extends Fragment {
    private static final String TYPE = "TYPE";

    public static ListingFragmentWithEvent createInstance(int type) {

        ListingFragmentWithEvent instance = new ListingFragmentWithEvent();
        instance.setArguments(createBundleInstance(type));
        return instance;
    }

    public static Bundle createBundleInstance(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        return bundle;
    }

    private int _type;

    @Bind(R.id.refresh_layout)
    SwipeRefreshLayout _refresh_layout;

    @Bind(R.id.recycler)
    protected RecyclerView _recycler;

    public ListingFragmentWithEvent() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        _recycler.setLayoutManager(manager);
        _refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Activity activity = getActivity();

                if (activity != null) {
                    OgameAgent agent = ((HomeActivity) activity).getCurrentOgameAgent();
                    int t = getType();
                    ItemRepresentationConstant load;
                    if (getType() >= 0) {
                        switch (t) {
                            case Constants.BUILDING_INDEX:
                                load = ItemRepresentationFactory.getBuildingConstants();
                                break;
                            case Constants.RESEARCH_INDEX:
                                load = ItemRepresentationFactory.getResearchConstants();
                                break;
                            case Constants.SHIPYARD_INDEX:
                                load = ItemRepresentationFactory.getShipConstants();
                                break;
                            case Constants.DEFENSE_INDEX:
                                load = ItemRepresentationFactory.getDefenseConstants();
                                break;
                            default:
                                load = ItemRepresentationFactory.getResourceConstants();
                                break;
                        }
                        EventBus.getDefault().post(new OnResourceRequestToLoadEvent(agent, t, load));
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle args = getArguments();
        _type = args != null && args.containsKey(TYPE) ? args.getInt(TYPE) : 0;

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    protected RecyclerView getRecyclerView() {
        return _recycler;
    }

    private int getType() {
        return _type;
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void checkProperEvent(OnResourcesLoaded event) {
        onEventForRecycler(event);
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void checkProperEvent(OnBuildingLoaded event) {
        onEventForRecycler(event);
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void checkProperEvent(OnResearchsLoaded event) {
        onEventForRecycler(event);
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void checkProperEvent(OnShipyardsLoaded event) {
        onEventForRecycler(event);
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void checkProperEvent(OnDefensesLoaded event) {
        onEventForRecycler(event);
    }

    public void onEventForRecycler(OnAbstractListInformationLoaded event) {
        if (event != null && Constants.Status.LOADING.equals(event.getStatus())) {
            _refresh_layout.setRefreshing(true);
        } else {
            _refresh_layout.setRefreshing(false);
        }

        if (event.getType() == _type) {
            ListingRecyclerAdapter adapter = new ListingRecyclerAdapter(event);
            _recycler.setAdapter(adapter);
        }
    }
}