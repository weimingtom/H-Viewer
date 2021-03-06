package ml.puredark.hviewer.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.photodraweeview.PhotoDraweeView;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Picture;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.core.RuleParser;
import ml.puredark.hviewer.helpers.Logger;
import ml.puredark.hviewer.http.HViewerHttpClient;
import ml.puredark.hviewer.http.ImageLoader;
import ml.puredark.hviewer.ui.activities.PictureViewerActivity;
import ml.puredark.hviewer.ui.dataproviders.ListDataProvider;
import ml.puredark.hviewer.ui.fragments.SettingFragment;
import ml.puredark.hviewer.ui.listeners.OnItemLongClickListener;
import ml.puredark.hviewer.utils.SharedPreferencesUtil;

import static android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK;

public class PictureViewerAdapter extends RecyclerView.Adapter<PictureViewerAdapter.PictureViewHolder> {
    private PictureViewerActivity activity;
    private Site site;
    private ListDataProvider<Picture> mProvider;
    private OnItemLongClickListener mOnItemLongClickListener;

    public PictureViewerAdapter(PictureViewerActivity activity, Site site, ListDataProvider<Picture> provider) {
        setHasStableIds(true);
        this.activity = activity;
        this.site = site;
        this.mProvider = provider;
    }

    @Override
    public PictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_picture_viewer, parent, false);
        // 在这里对View的参数进行设置
        PictureViewHolder vh = new PictureViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(PictureViewHolder viewHolder, int position) {
        Picture picture = mProvider.getItem(position);
        if (picture.pic != null) {
            activity.loadImage(picture, viewHolder);
        } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null) {
            if(site.extraRule.pictureRule != null && site.extraRule.pictureRule.url != null)
                activity.getPictureUrl(viewHolder, picture, site.extraRule.pictureRule.url, site.extraRule.pictureRule.highRes);
            else if(site.extraRule.pictureUrl != null)
                activity.getPictureUrl(viewHolder, picture, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
        } else if (site.picUrlSelector != null) {
            activity.getPictureUrl(viewHolder, picture, site.picUrlSelector, null);
        } else {
            picture.pic = picture.url;
            activity.loadImage(picture, viewHolder);
        }
        viewHolder.btnRefresh.setOnClickListener(v -> {
            if (picture.pic != null) {
                activity.loadImage(picture, viewHolder);
            } else if (site.hasFlag(Site.FLAG_SINGLE_PAGE_BIG_PICTURE) && site.extraRule != null) {
                if(site.extraRule.pictureRule != null && site.extraRule.pictureRule.url != null)
                    activity.getPictureUrl(viewHolder, picture, site.extraRule.pictureRule.url, site.extraRule.pictureRule.highRes);
                else if(site.extraRule.pictureUrl != null)
                    activity.getPictureUrl(viewHolder, picture, site.extraRule.pictureUrl, site.extraRule.pictureHighRes);
            } else if (site.picUrlSelector == null) {
                picture.pic = picture.url;
                activity.loadImage(picture, viewHolder);
            } else {
                activity.getPictureUrl(viewHolder, picture, site.picUrlSelector, null);
            }
        });
        viewHolder.ivPicture.setOnLongClickListener(v -> {
            if (mOnItemLongClickListener != null)
                return mOnItemLongClickListener.onItemLongClick(v, position);
            else
                return false;
        });
    }

    @Override
    public int getItemCount() {
        return (mProvider == null) ? 0 : mProvider.getCount();
    }

    @Override
    public long getItemId(int position) {
        return (mProvider == null) ? 0 : mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    public ListDataProvider getDataProvider() {
        return mProvider;
    }

    public void setDataProvider(ListDataProvider mProvider) {
        this.mProvider = mProvider;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    public class PictureViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_picture)
        public PhotoDraweeView ivPicture;
        @BindView(R.id.progress_bar)
        public ProgressBarCircularIndeterminate progressBar;
        @BindView(R.id.btn_refresh)
        public ImageView btnRefresh;

        public PictureViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            ivPicture.setOrientation(LinearLayout.VERTICAL);
        }
    }
}