package com.giga.ehospital.reservation.controller;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.giga.ehospital.reservation.R;
import com.giga.ehospital.reservation.base.inter.ControllerClickHandler;
import com.giga.ehospital.reservation.container.NormalContainer;
import com.giga.ehospital.reservation.fragment.doctor.CalendarManageFragment;
import com.giga.ehospital.reservation.fragment.doctor.DoctorInfoManageFragment;
import com.giga.ehospital.reservation.fragment.home.HealthArticleFragment;
import com.giga.ehospital.reservation.helper.DialogHelper;
import com.giga.ehospital.reservation.helper.TipDialogHelper;
import com.giga.ehospital.reservation.model.hospital.Doctor;
import com.giga.ehospital.reservation.model.vo.HealthArticle;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.QMUIWindowInsetLayout;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.tmall.ultraviewpager.UltraViewPager;

import java.util.HashMap;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class DoctorHomeController extends QMUIWindowInsetLayout {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.pull_to_refresh)
    QMUIPullRefreshLayout mPullRefreshLayout;
    @BindView(R.id.controller_pager)
    ViewPager mViewPager;
    @BindView(R.id.controller_tabs)
    QMUITabSegment mTabSegment;
    @BindView(R.id.ultraview_pager)
    UltraViewPager mDoctorActivityUltraViewPager;
    @BindView(R.id.doctor_reserdate_manage_linearLayout)
    LinearLayout llt1;
    @BindView(R.id.doctor_selfinfo_manage_linearLayout)
    LinearLayout llt2;
    @BindView(R.id.doctor_reserinfo_manage_linearLayout)
    LinearLayout llt3;
    @BindView(R.id.doctor_answer_linearLayout)
    LinearLayout llt4;
    @BindString(R.string.wait_please)
    String WAIT_PLEASE;

    private Context mContext;
    private ControllerClickHandler mHandler;
    private TipDialogHelper mTipDialogHelper;
    private DialogHelper mDialogHelper;

    public DoctorHomeController(Context context, ControllerClickHandler mHandler) {
        super(context);
        this.mContext = context;
        this.mHandler = mHandler;
        LayoutInflater.from(context).inflate(R.layout.controller_doctor_home, this);
        ButterKnife.bind(this);
        init(context);
    }

    @OnClick({R.id.doctor_reserdate_manage_linearLayout, R.id.doctor_selfinfo_manage_linearLayout,
            R.id.doctor_reserinfo_manage_linearLayout, R.id.doctor_answer_linearLayout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doctor_reserdate_manage_linearLayout:
                Toasty.info(getContext(), WAIT_PLEASE, Toasty.LENGTH_SHORT, true).show();
                break;
            case R.id.doctor_selfinfo_manage_linearLayout:
                mHandler.startFragment(new DoctorInfoManageFragment());
                break;
            case R.id.doctor_reserinfo_manage_linearLayout:
                mHandler.startFragment(new CalendarManageFragment());
                break;
            case R.id.doctor_answer_linearLayout:
                Toasty.info(getContext(), WAIT_PLEASE, Toasty.LENGTH_SHORT, true).show();
                break;
        }
    }

    protected void init(Context context) {
        initBasic(context);
        initTopBar();
        initRefreshLayout();
        initUltraViewPager();
        initTabs();
        initPagers();
        checkLocalDoctor();
    }

    private void checkLocalDoctor() {
        Doctor doctor = NormalContainer.get(NormalContainer.DOCTOR);
        if (doctor == null || doctor.getDoctorId() == null) {
            Toasty.warning(getContext(), "数据同步失败，请重新登录", Toasty.LENGTH_SHORT, true).show();
            llt1.setEnabled(false);
            llt2.setEnabled(false);
            llt3.setEnabled(false);
            llt4.setEnabled(false);
        }
    }

    HashMap<HealthArticleFragment.Pager, View> mPages;
    HealthArticleFragment.HeadlineRecycleViewAdapter mHeadlineRecycleViewAdapter;
    HealthArticleFragment.DoctorLectureRecyclerViewAdapter mDoctorLectureRecyclerViewAdapter;
    List<HealthArticle> mHealthArticles;

    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        private int mChildCount = 0;

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            View page = mPages.get(HealthArticleFragment.Pager.getPagerFromPosition(position));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            container.addView(page, params);
            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
//            if (mChildCount == 0) {
//                return POSITION_NONE;
//            }
//            return super.getItemPosition(object);
            return POSITION_NONE;   // 修复数据刷新但是高度不刷新的异常
        }

        @Override
        public void notifyDataSetChanged() {
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }
    };

    private void initPagers() {
        mPages = new HashMap<>();
        RecyclerView mHeadlineRecycleView = new RecyclerView(getContext());
        RecyclerView mDoctorLectureRecyclerView = new RecyclerView(getContext());
        mHeadlineRecycleViewAdapter = new HealthArticleFragment.HeadlineRecycleViewAdapter(getContext(), null);
        mDoctorLectureRecyclerViewAdapter = new HealthArticleFragment.DoctorLectureRecyclerViewAdapter(getContext(), null);
//        mDoctorLectureRecyclerViewAdapter.setOnItemClickListener((v, pos) -> mContext.startActivity(new Intent(mContext, PlayVideoActivity.class)));
        mHeadlineRecycleView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        mHeadlineRecycleView.setAdapter(mHeadlineRecycleViewAdapter);
        mDoctorLectureRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        mDoctorLectureRecyclerView.setAdapter(mDoctorLectureRecyclerViewAdapter);

        mPages.put(HealthArticleFragment.Pager.HEADLINE, mHeadlineRecycleView);
        mPages.put(HealthArticleFragment.Pager.DOCTOR_LECTURE, mDoctorLectureRecyclerView);
        mViewPager.setAdapter(mPagerAdapter);
        mTabSegment.setupWithViewPager(mViewPager, false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                // 切换到当前页面，重置高度
                mViewPager.requestLayout();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
    }

    private void initTabs() {
        int normalColor = QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_gray_6);
        int selectColor = QMUIResHelper.getAttrColor(getContext(), R.attr.qmui_config_color_blue);
        mTabSegment.setDefaultNormalColor(normalColor);
        mTabSegment.setDefaultSelectedColor(selectColor);
        mTabSegment.setHasIndicator(true);
        mTabSegment.setIndicatorPosition(false);
        mTabSegment.setIndicatorWidthAdjustContent(true);
        QMUITabSegment.Tab healthArticle = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(getContext(), R.mipmap.ic_crown),
                ContextCompat.getDrawable(getContext(), R.mipmap.ic_crown_selected),
                "健康头条", false
        );
        QMUITabSegment.Tab doctorLecture = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(getContext(), R.mipmap.ic_hat),
                ContextCompat.getDrawable(getContext(), R.mipmap.ic_hat_selected),
                "名医讲堂", false
        );
        mTabSegment.addTab(healthArticle);
        mTabSegment.addTab(doctorLecture);
    }

    private void initBasic(Context context) {
        mTipDialogHelper = new TipDialogHelper(context);
        mDialogHelper = new DialogHelper(context);
    }

    protected void initTopBar() {
        mTopBar.setBackgroundDividerEnabled(false);
        mTopBar.setTitle("首页");
        mTopBar.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_transparent));
    }

    /**
     * 初始化轮播图
     */
    private void initUltraViewPager() {
        mDoctorActivityUltraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        mDoctorActivityUltraViewPager.setAdapter(mUltraPagerAdapter);
        mDoctorActivityUltraViewPager.setInfiniteLoop(true);
        mDoctorActivityUltraViewPager.setAutoScroll(4000);
    }

    private PagerAdapter mUltraPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View root = LayoutInflater.from(container.getContext()).inflate(R.layout.ultrapager_item, null);
//            if (mHealthArticles != null) {
            ImageView imageView = root.findViewById(R.id.imageview);
            if (position % 3 == 0) {
                Glide.with(getContext()).load(R.mipmap.bg_ulpager_t4).into(imageView);
            } else if (position % 3 == 1) {
                Glide.with(getContext()).load(R.mipmap.bg_ulpager_t2).into(imageView);
            } else {
                Glide.with(getContext()).load(R.mipmap.bg_ulpager_t3).into(imageView);
            }
//            }
            container.addView(root);
            return root;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    };

    protected void initRefreshLayout() {
        mPullRefreshLayout.setEnabled(false);
    }
}
