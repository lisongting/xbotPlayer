/*
 * Copyright 2017 lisongting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iscas.xlab.xbotplayer.mvp.rvizmap;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import cn.iscas.xlab.xbotplayer.App;
import cn.iscas.xlab.xbotplayer.Config;
import cn.iscas.xlab.xbotplayer.R;
import cn.iscas.xlab.xbotplayer.customview.MapView;

/**
 * Created by lisongting on 2017/10/9.
 */

public class MapFragment extends Fragment implements MapContract.View{

    public static final String TAG = "MapFragment";
    private MapView mapView;
    private MapContract.Presenter presenter ;
    private Button toggleMap;
    private Button resetButton;
    private boolean isMapOpened;
    private SwipeRefreshLayout refreshLayout;

    public MapFragment(){
        log("MapFragment() Created()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        log("onCreateView()");
        View view = inflater.inflate(R.layout.fragment_map,container,false);
        mapView = (MapView) view.findViewById(R.id.map_view);
        toggleMap = (Button) view.findViewById(R.id.toggleMap);
        resetButton = (Button) view.findViewById(R.id.reset);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        initView();
        initOnClickListener();
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        log("onResume()");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        log("onActivityCreate()");
    }

    @Override
    public void showLoading() {
        refreshLayout.setRefreshing(true);
        Toast.makeText(getContext(), "等待地图数据更新，请稍后", Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideLoading() {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void updateMap(Bitmap mapInfo) {
        mapView.updateMap(mapInfo);
    }

    @Override
    public void initView() {
        refreshLayout.setEnabled(false);
        refreshLayout.setColorSchemeResources(android.R.color.holo_purple
                ,android.R.color.holo_orange_light
                ,android.R.color.holo_blue_light);
    }
    private void initOnClickListener() {
        toggleMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMapOpened) {
                    if (!Config.isRosServerConnected) {
                        Toast.makeText(getContext(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                    } else{
                        showLoading();
                        isMapOpened = true;
                        toggleMap.setText("关闭地图");
                        presenter.subscribeMapData();
                    }
                } else {
                    isMapOpened = false;
                    toggleMap.setText("显示地图");
                    refreshLayout.setRefreshing(false);
                    presenter.abortLoadMap();
                    presenter.unsubscribeMapData();
                    mapView.updateMap(null);
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMapOpened) {
                    mapView.reset();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public boolean isHided() {
        return this.isHidden();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.i(TAG, "isHided:" + hidden);
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (Config.isRosServerConnected) {
                App app = (App) (getActivity().getApplication());
                if (presenter == null) {
                    presenter = new MapPresenter(getContext(), this);
                    presenter.setServiceProxy(app.getRosServiceProxy());
                    presenter.start();
                }
            }
        }
    }

    @Override
    public void setPresenter(MapContract.Presenter presenter) {
        this.presenter = presenter;
    }


    @Override
    public Size getMapRealSize() {
        return new Size(mapView.getWidth(), mapView.getHeight());
    }

    private void log(String string) {
        Log.i(TAG,TAG + " -- " + string);
    }
}
