/*
package com.veeps.app.feature.browse.ui;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.dct.dcafe.R;
import com.dct.dcafe.data.models.BasicResponse;
import com.dct.dcafe.data.models.NavigationInnerData;
import com.dct.dcafe.data.models.NavigationResponse;
import com.dct.dcafe.data.models.ProfileResponse;
import com.dct.dcafe.data.remote.APIClient;
import com.dct.dcafe.databinding.ActivityHomeScreenBinding;
import com.dct.dcafe.fragments.AssetDetailFragment;
import com.dct.dcafe.fragments.AssetGroupDetailFragment;
import com.dct.dcafe.fragments.ListingFragment;
import com.dct.dcafe.fragments.LivePrimaryNavigationFragment;
import com.dct.dcafe.fragments.PrimaryNavigationFragment;
import com.dct.dcafe.fragments.ProfileFragment;
import com.dct.dcafe.fragments.SearchFragment;
import com.dct.dcafe.utils.AppManageInterface;
import com.dct.dcafe.utils.AppUtils;
import com.dct.dcafe.utils.Const;
import com.dct.dcafe.utils.NetworkWatcher;
import com.dct.dcafe.utils.Prefs;
import com.dct.dcafe.widgets.NavigationMenu;
import com.google.common.collect.ImmutableList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeScreen extends FragmentActivity implements AppManageInterface, NavigationMenu.NavigationMenuItemClickListener, PurchasingListener {

    private static final int DEFAULT_SELECTED_POSITION = 3;
    public static BillingClient billingClient;
    public static PurchasesUpdatedListener purchasesUpdatedListener;
    String paymentString = "";
    CountDownTimer paymentTimer;
    boolean reset = false;
    private List<ProductDetails> productDetails = new ArrayList<>();
    private ActivityHomeScreenBinding binding;
    private NetworkWatcher networkWatcher;
    private Observer<Boolean> networkObserver;
    private boolean isNavigationMenuVisible = false;
    private int expandWidth;
    private int collapsedWidth;
    private ArrayList<NavigationInnerData> navigationMenuData = new ArrayList<>();
    private Call<NavigationResponse> primaryNavigationCall;
    private Call<ProfileResponse> userProfileCall;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.errorContainer.setVisibility(View.GONE);
        getProfile();
    }

    private void getNavigation() {
        if (Prefs.getPrefInstance().getValue(Const.IS_INTERNET_AVAILABLE, true)) {
            binding.loader.setVisibility(View.VISIBLE);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("deviceTypeId", Const.DEVICE_TYPE);
                jsonObject.put("langId", Prefs.getPrefInstance().getValue(Const.SELECTED_LANGUAGE, "1"));
            } catch (JSONException e) {
                binding.loader.setVisibility(View.GONE);
                e.printStackTrace();
            }
            String params = jsonObject.toString();
            primaryNavigationCall = APIClient.getAPIService().get_primary_navigation(params);
            primaryNavigationCall.enqueue(new Callback<NavigationResponse>() {
                @Override
                public void onResponse(@NonNull Call<NavigationResponse> call, @NonNull Response<NavigationResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getSuccess()) {
                            if (response.body().getData() != null) {
                                if (response.body().getData().getData() != null && !response.body().getData().getData().isEmpty()) {
                                    navigationMenuData = response.body().getData().getData();
                                    binding.navigationMenu.generateViews(HomeScreen.this, navigationMenuData);
                                    binding.navigationMenu.setupDefaultNavigationMenu(DEFAULT_SELECTED_POSITION);
                                    NavigationInnerData defaultNavigation = navigationMenuData.get(DEFAULT_SELECTED_POSITION - 3);
                                    replaceSelectedFragment(PrimaryNavigationFragment.newInstance(defaultNavigation.getPath(), defaultNavigation.getPrimaryNavigationType(), defaultNavigation.getLabel()), defaultNavigation.getLabel());
                                    setupFocusListener();
                                    binding.loader.setVisibility(View.GONE);
                                } else {
                                    binding.loader.setVisibility(View.GONE);
                                    if (response.body().getMessage() != null && !response.body().getMessage().isEmpty())
                                        showError(response.body().getMessage(), "MAIN");
                                    else
                                        showError(getResources().getString(R.string.something_went_wrong), "MAIN");
                                }
                            } else {
                                binding.loader.setVisibility(View.GONE);
                                if (response.body().getMessage() != null && !response.body().getMessage().isEmpty())
                                    showError(response.body().getMessage(), "MAIN");
                                else
                                    showError(getResources().getString(R.string.something_went_wrong), "MAIN");
                            }
                        } else {
                            binding.loader.setVisibility(View.GONE);
                            if (response.body().getMessage() != null && !response.body().getMessage().isEmpty())
                                showError(response.body().getMessage(), "MAIN");
                            else
                                showError(getResources().getString(R.string.something_went_wrong), "MAIN");
                        }
                    } else {
                        binding.loader.setVisibility(View.GONE);
                        String errorMessage = getResources().getString(R.string.something_went_wrong);
                        if (response.errorBody() != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                errorMessage = Html.fromHtml(jsonObject.getString("message"), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                        showError(errorMessage, "MAIN");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NavigationResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                    showError(getResources().getString(R.string.something_went_wrong), "MAIN");
                }
            });
        } else {
            showError(getResources().getString(R.string.no_internet_connection), "MAIN");
        }
    }

    public void getProfile() {
        if (Prefs.getPrefInstance().getValue(Const.LOGIN_ACCESS, "").equals(getResources().getString(R.string.logged_in))) {
            if (Prefs.getPrefInstance().getValue(Const.IS_INTERNET_AVAILABLE, true)) {
                binding.loader.setVisibility(View.VISIBLE);
                userProfileCall = APIClient.getAPIService().get_user_profile(Prefs.getPrefInstance().getValue(Const.USER_ID, ""));
                userProfileCall.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getSuccess()) {
                                if (response.body().getData() != null && !response.body().getData().isEmpty()) {
                                    // USER ID
                                    Prefs.getPrefInstance().setValue(Const.USER_ID, response.body().getData().get(0).getId().toString());

                                    // USER EMAIL / MOBILE
                                    if (response.body().getData().get(0).getEmailId() != null && !response.body().getData().get(0).getEmailId().isEmpty()) {
                                        Prefs.getPrefInstance().setValue(Const.USER_NAME, response.body().getData().get(0).getEmailId());
                                        Prefs.getPrefInstance().setValue(Const.USER_EMAIL, response.body().getData().get(0).getEmailId());
                                    } else {
                                        Prefs.getPrefInstance().setValue(Const.USER_NAME, response.body().getData().get(0).getMobile());
                                        Prefs.getPrefInstance().setValue(Const.USER_EMAIL, response.body().getData().get(0).getMobile());
                                    }

                                    // FIRST NAME / LAST NAME
                                    if (response.body().getData().get(0).getFirstName() != null && !response.body().getData().get(0).getFirstName().isEmpty()) {
                                        Prefs.getPrefInstance().setValue(Const.USER_FIRST_NAME, response.body().getData().get(0).getFirstName());
                                        if (response.body().getData().get(0).getLastName() != null && !response.body().getData().get(0).getLastName().isEmpty()) {
                                            Prefs.getPrefInstance().setValue(Const.USER_LAST_NAME, response.body().getData().get(0).getLastName());
                                            String userName = response.body().getData().get(0).getFirstName() + " " + response.body().getData().get(0).getLastName();
                                            Prefs.getPrefInstance().setValue(Const.USER_NAME, userName);
                                        } else {
                                            Prefs.getPrefInstance().setValue(Const.USER_LAST_NAME, "");
                                            Prefs.getPrefInstance().setValue(Const.USER_NAME, response.body().getData().get(0).getFirstName());
                                        }
                                    } else {
                                        Prefs.getPrefInstance().setValue(Const.USER_FIRST_NAME, "");
                                        Prefs.getPrefInstance().setValue(Const.USER_LAST_NAME, "");
                                    }

                                    // USER PICTURE
                                    Prefs.getPrefInstance().setValue(Const.USER_PROFILE_IMAGE, response.body().getData().get(0).getPicture());

                                    binding.loader.setVisibility(View.GONE);
                                    getNavigation();
                                } else {
                                    binding.loader.setVisibility(View.GONE);
                                    if (response.body().getMessage() != null && !response.body().getMessage().isEmpty())
                                        showError(response.body().getMessage(), "MAIN");
                                    else
                                        showError(getResources().getString(R.string.something_went_wrong), "MAIN");
                                }
                            } else {
                                binding.loader.setVisibility(View.GONE);
                                if (response.body().getMessage() != null && !response.body().getMessage().isEmpty())
                                    showError(response.body().getMessage(), "MAIN");
                                else
                                    showError(getResources().getString(R.string.something_went_wrong), "MAIN");
                            }
                        } else {
                            String errorMessage = getResources().getString(R.string.something_went_wrong);
                            if (response.errorBody() != null) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                    errorMessage = Html.fromHtml(jsonObject.getString("message"), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            binding.loader.setVisibility(View.GONE);
                            showError(errorMessage, "MAIN");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        binding.loader.setVisibility(View.GONE);
                        showError(getResources().getString(R.string.something_went_wrong), "MAIN");
                    }
                });
            } else {
                binding.loader.setVisibility(View.GONE);
                showError(getResources().getString(R.string.no_internet_connection), "MAIN");
            }
        } else {
            getNavigation();
        }
    }

    @Override
    public void showError(String errorMessage, String typeCode) {
        AppUtils.logger("TypeCode --> " + typeCode);
        binding.errorContainer.setVisibility(View.VISIBLE);
        binding.buttonPositive.requestFocus();
        binding.errorText.setText(errorMessage);
        if (typeCode.equals("PROFILE_FAVORITES_DELETE") || typeCode.equals("LISTING_FAVORITES_DELETE") || typeCode.equals("PROFILE_WATCHLIST_DELETE") || typeCode.equals("LISTING_WATCHLIST_DELETE")) {
            binding.buttonPositive.setText(getResources().getString(R.string.ok));
        } else {
            binding.buttonPositive.setText(getResources().getString(R.string.retry));
        }
        binding.buttonPositive.setOnClickListener(v -> {
            if (typeCode.equals("MAIN")) {
                getProfile();
            } else if (typeCode.equals(NavigationMenu.MY_ACCOUNT)) {
                ProfileFragment.getInstance().getProfile();
            } else if (typeCode.equals(getResources().getString(R.string.watch_list))) {
                ProfileFragment.getInstance().getWatchList();
            } else if (typeCode.equals(getResources().getString(R.string.billing))) {
                ProfileFragment.getInstance().getOrders();
            } else if (typeCode.equals(getResources().getString(R.string.contact_us))) {
                ProfileFragment.getInstance().sendMessage();
            } else if (typeCode.equals(getResources().getString(R.string.favourites))) {
                ProfileFragment.getInstance().getFavorites();
            } else if (typeCode.equals(getResources().getString(R.string.view_all))) {
                ListingFragment.getInstance().getPageCategoryDetails();
            } else if (typeCode.equals("FOOTER_LINKS")) {
                ProfileFragment.getInstance().getFooterDetails();
            } else if (typeCode.equals("FOOTER_DATA")) {
                ProfileFragment.getInstance().getFooterDescription();
            } else if (typeCode.equals("RECENT_SEARCH")) {
                SearchFragment.getInstance().getSearchResults();
            } else if (typeCode.equals("SEARCH")) {
                SearchFragment.getInstance().getSearchResults();
            } else if (typeCode.equals("PAGE_CATEGORIES")) {
                PrimaryNavigationFragment.getInstance().getCarousal();
            } else if (typeCode.equals("ASSET_DETAIL_FAVORITES")) {
                AssetDetailFragment.getInstance().setFavorites();
            } else if (typeCode.equals("ASSET_DETAIL")) {
                AssetDetailFragment.getInstance().getAssetDetails();
            } else if (typeCode.equals("ASSET_GROUP_DETAIL_FAVORITES")) {
                AssetGroupDetailFragment.getInstance().setFavorites();
            } else if (typeCode.equals("ASSET_GROUP_ASSET_DETAIL")) {
                AssetGroupDetailFragment.getInstance().getAssetDetails();
            } else if (typeCode.equals("ASSET_GROUP_DETAIL")) {
                AssetGroupDetailFragment.getInstance().getAssetGroupDetails();
            } else if (typeCode.equals("ASSET_CATEGORY_DETAIL")) {
                AssetGroupDetailFragment.getInstance().loadAssetCategoryDetails();
            } else if (typeCode.equals("ASSET_GROUP_ASSET_LISTING")) {
                AssetGroupDetailFragment.getInstance().getAssetListing();
            } else if (typeCode.equals("checkPlanValidity - Listing")) {
                ListingFragment.getInstance().checkUserPlanValidity();
            } else if (typeCode.equals("LISTING_ALL_FAVORITES_DELETE")) {
                ListingFragment.getInstance().removeAllFromFavorites();
            } else if (typeCode.equals("LISTING_ALL_WATCH_LIST_DELETE")) {
                ListingFragment.getInstance().removeAllFromWishList();
            } else if (typeCode.equals(getResources().getString(R.string.watch_list_horizontal))) {
                ListingFragment.getInstance().getWatchList();
            } else if (typeCode.equals(getResources().getString(R.string.favourites_horizontal))) {
                ListingFragment.getInstance().getFavourites();
            } else if (typeCode.equals(getResources().getString(R.string.plans))) {
                ListingFragment.getInstance().getSubscriptionPlans();
            } else if (typeCode.equals("LIVE_ASSET_DETAIL")) {
                LivePrimaryNavigationFragment.getInstance().getAssetDetails();
            } else if (typeCode.equals("GET_LIVE_ASSETS")) {
                LivePrimaryNavigationFragment.getInstance().getLiveAssets();
            } else if (typeCode.equals("LIVE_CHANNEL_SCHEDULING")) {
                LivePrimaryNavigationFragment.getInstance().getLiveAssets();
            }
            binding.errorContainer.setVisibility(View.GONE);
        });
        binding.buttonNegative.setText(getResources().getString(R.string.cancel));
        binding.buttonNegative.setOnClickListener(v -> {
            switch (typeCode) {
                case "MAIN":
                    finish();
                    break;
                case "ASSET_DETAIL":
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    binding.navigationMenu.setOnFocusChangeListener(null);
                    binding.navigationMenu.setupDefaultNavigationMenu(DEFAULT_SELECTED_POSITION);
                    NavigationInnerData defaultNavigation = navigationMenuData.get(DEFAULT_SELECTED_POSITION - 3);
                    replaceSelectedFragment(PrimaryNavigationFragment.newInstance(defaultNavigation.getPath(), defaultNavigation.getPrimaryNavigationType(), defaultNavigation.getLabel()), defaultNavigation.getLabel());
                    new Handler(Looper.getMainLooper()).postDelayed(this::setupFocusListener, 1000);
                    binding.errorContainer.setVisibility(View.GONE);
                    break;
                default:
                    binding.errorContainer.setVisibility(View.GONE);
                    break;
            }
        });
    }

    @Override
    public void menuItemClick(int selectedItem, String navigationMenuItemText) {
        if (navigationMenuItemText.equals(NavigationMenu.EXIT_APP)) {
            View dialog_view = LayoutInflater.from(this).inflate(R.layout.dialog_view, null);
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.dialog).setCancelable(false).setView(dialog_view).create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(HomeScreen.this, R.drawable.black_background));
                dialog.getWindow().setDimAmount(0.75f);
            }
            ((TextView) dialog_view.findViewById(R.id.error_text)).setText(getResources().getString(R.string.exit_app_text));
            ((Button) dialog_view.findViewById(R.id.button_positive)).setText(getResources().getString(R.string.yes));
            ((Button) dialog_view.findViewById(R.id.button_negative)).setText(getResources().getString(R.string.cancel));
            dialog_view.findViewById(R.id.button_positive).setOnClickListener(v1 -> {
                dialog.dismiss();
                finish();
            });
            dialog_view.findViewById(R.id.button_negative).setOnClickListener(v1 -> dialog.dismiss());
            dialog_view.findViewById(R.id.button_positive).requestFocus();
            dialog.show();
            return;
        }
        if (binding.navigationMenu.getCurrentSelected() == selectedItem) {
            if (navigationMenuItemText.equals(NavigationMenu.MY_ACCOUNT)) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
                if ((fragment instanceof ProfileFragment)) return;
            } else {
                return;
            }
        }
        Fragment fragment;
        String tag;
        switch (navigationMenuItemText) {
            case NavigationMenu.HEADER:
            case NavigationMenu.MY_ACCOUNT:
                if (Prefs.getPrefInstance().getValue(Const.LOGIN_ACCESS, "").equals(getResources().getString(R.string.logged_in))) {
                    binding.navigationMenu.setCurrentSelected(selectedItem);
                    tag = getResources().getString(R.string.my_account);
                    fragment = ProfileFragment.newInstance();
                } else {
                    startActivity(new Intent(HomeScreen.this, UserAuthenticationScreen.class));
                    return;
                }
                break;
            case NavigationMenu.SEARCH:
                binding.navigationMenu.setCurrentSelected(selectedItem);
                tag = getResources().getString(R.string.search);
                fragment = SearchFragment.newInstance(tag);
                break;
            case NavigationMenu.WATCHLIST:
                binding.navigationMenu.setCurrentSelected(selectedItem);
                tag = getResources().getString(R.string.watch_list);
                fragment = ListingFragment.newInstance(tag, tag, tag, getResources().getString(R.string.horizontal));
                break;
            case NavigationMenu.FAVORITES:
                binding.navigationMenu.setCurrentSelected(selectedItem);
                tag = getResources().getString(R.string.favourites);
                fragment = ListingFragment.newInstance(tag, tag, tag, getResources().getString(R.string.horizontal));
                break;
            case NavigationMenu.PLANS:
                binding.navigationMenu.setCurrentSelected(selectedItem);
                tag = getResources().getString(R.string.plans);
                fragment = ListingFragment.newInstance(tag, tag, tag, getResources().getString(R.string.horizontal));
                break;
            case NavigationMenu.DEVICE_MANAGER:
                binding.navigationMenu.setCurrentSelected(selectedItem);
                tag = getResources().getString(R.string.device_manager);
                fragment = ListingFragment.newInstance(tag, tag, tag, getResources().getString(R.string.horizontal));
                break;
            default:
                binding.navigationMenu.setCurrentSelected(selectedItem);
                tag = navigationMenuData.get(selectedItem - 3).getLabel();
                if (navigationMenuData.get(selectedItem - 3).getPrimaryNavigationType().equalsIgnoreCase("normal")) {
                    fragment = PrimaryNavigationFragment.newInstance(navigationMenuData.get(selectedItem - 3).getPath(), navigationMenuData.get(selectedItem - 3).getPrimaryNavigationType(), tag);
                } else {
                    fragment = LivePrimaryNavigationFragment.newInstance(navigationMenuData.get(selectedItem - 3).getPath());
                }
                break;
        }

        isNavigationMenuVisible = false;
        hideNavigationMenu(binding.navigationMenu);
        new Handler(Looper.getMainLooper()).postDelayed(() -> replaceSelectedFragment(fragment, tag), 200);
        new Handler(Looper.getMainLooper()).postDelayed(this::setupFocusListener, 1000);
    }

    @Override
    public void scroll(boolean isTop) {
        if (binding != null) {
            if (isTop)
                binding.scrollView.smoothScrollTo(0, 0);
            else {
                binding.scrollView.smoothScrollTo(0, binding.scrollView.getHeight());
            }
        }
    }

    private void replaceSelectedFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        binding.frameContainer.removeAllViewsInLayout();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.replace(R.id.frameContainer, fragment, tag);
        transaction.disallowAddToBackStack();
        transaction.commit();
        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.frameContainer.requestFocus(), 100);
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        binding.navigationMenu.setOnFocusChangeListener(null);
        binding.frameContainer.removeAllViewsInLayout();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.replace(R.id.frameContainer, fragment, tag);
        transaction.disallowAddToBackStack();
        transaction.commit();
        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.frameContainer.requestFocus(), 100);
        new Handler(Looper.getMainLooper()).postDelayed(this::setupFocusListener, 1000);
    }

    @Override
    public void addFragment(int selectedItem, Fragment fragment, String tag) {
        binding.navigationMenu.setOnFocusChangeListener(null);
        binding.navigationMenu.setCurrentSelectedItem(selectedItem);
        binding.frameContainer.removeAllViewsInLayout();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.add(R.id.frameContainer, fragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.frameContainer.requestFocus(), 100);
        new Handler(Looper.getMainLooper()).postDelayed(this::setupFocusListener, 1000);
    }

    @Override
    public void showNavigationMenu() {
        if (!isNavigationMenuVisible) {
            binding.navigationMenu.setOnFocusChangeListener(null);
            isNavigationMenuVisible = true;
            showNavigationMenu(binding.navigationMenu);
        } else {
            clearNavigationMenuUI();
        }
    }

    @Override
    public void hideNavigationMenu() {
        if (isNavigationMenuVisible) {
            clearNavigationMenuUI();
        }
    }

    @Override
    public void loadImage(int adapterPosition, int position) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if (fragment instanceof PrimaryNavigationFragment) {
            PrimaryNavigationFragment.getInstance().loadImage(adapterPosition, position);
        }
    }

    @Override
    public void setFocus() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if (fragment instanceof PrimaryNavigationFragment) {
            PrimaryNavigationFragment.getInstance().setFocus();
        }
        if (fragment instanceof ProfileFragment) {
            ProfileFragment.getInstance().setFocus();
        }

        if (fragment instanceof ListingFragment) {
            ListingFragment.getInstance().setFocus();
        }

        if (fragment instanceof AssetDetailFragment) {
            AssetDetailFragment.getInstance().setFocus();
        }

        if (fragment instanceof AssetGroupDetailFragment) {
            AssetGroupDetailFragment.getInstance().setFocus();
        }
    }

    @Override
    public void loadCarousalImage(int position) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if (fragment instanceof PrimaryNavigationFragment) {
            PrimaryNavigationFragment.getInstance().loadCarousalImage(position);
        }
    }

    @Override
    public void setSelectedAssetCategory(String assetCategoryPath) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if (fragment instanceof AssetGroupDetailFragment) {
            AssetGroupDetailFragment.getInstance().loadAssetCategoryDetails(assetCategoryPath);
        }
    }

    @Override
    public void setTypeSelected(String path, String title, String type) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if (fragment instanceof ProfileFragment) {
            ProfileFragment.getInstance().loadSelectedType(path, title, type);
        }
    }

    @Override
    public void loadAssetDetails(String assetPath, int selectedAssetPosition) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if (fragment instanceof AssetGroupDetailFragment) {
            AssetGroupDetailFragment.getInstance().getAssetDetails(assetPath);
            AssetGroupDetailFragment.getInstance().setSelectedAssetPosition(selectedAssetPosition, false);
        }
    }

    private void setupFocusListener() {
        binding.navigationMenu.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                binding.navigationMenu.setOnFocusChangeListener(null);
                isNavigationMenuVisible = true;
                showNavigationMenu(view);
            } else {
                isNavigationMenuVisible = false;
                hideNavigationMenu(view);
            }
        });
    }

    private void showNavigationMenu(View view) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if (fragment instanceof PrimaryNavigationFragment) {
            PrimaryNavigationFragment.getInstance().checkPlayer(true);
        }
        int width = binding.navigationMenu.getMeasuredWidth();
        ValueAnimator animator = ValueAnimator.ofInt(width, expandWidth);
        binding.navigationMenu.setupNavigationMenuExpandedUI(this);
        binding.foregroundContainer.setVisibility(View.VISIBLE);
        binding.navigationMenu.animateView(view, animator, true, binding.frameContainer, fragment instanceof SearchFragment);
    }

    private void hideNavigationMenu(View view) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if (fragment instanceof PrimaryNavigationFragment) {
            PrimaryNavigationFragment.getInstance().checkPlayer(false);
        }
        int width = binding.navigationMenu.getMeasuredWidth();
        ValueAnimator animator = ValueAnimator.ofInt(width, collapsedWidth);
        binding.navigationMenu.setupNavigationMenuCollapsedUI();
        binding.foregroundContainer.setVisibility(View.GONE);
        binding.scrollView.scrollTo(0, 0);
        binding.navigationMenu.animateView(view, animator, false, binding.frameContainer, fragment instanceof SearchFragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        long currentTime = System.currentTimeMillis();
        if (currentTime - Const.LAST_KEY_DOWN_TIME < Const.DELAY_TIME) {
            return true;
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (binding.paymentLoader.getVisibility() == View.VISIBLE) {
                    if (binding.paymentLoaderText.getText() != null && !binding.paymentLoaderText.getText().toString().isEmpty()) {
                        Const.LAST_KEY_DOWN_TIME = currentTime;
                        return true;
                    } else {
                        Const.LAST_KEY_DOWN_TIME = currentTime;
                        binding.paymentLoader.setVisibility(View.GONE);
                        return true;
                    }
                } else {
                    if (isNavigationMenuVisible) {
                        Const.LAST_KEY_DOWN_TIME = currentTime;
                        return clearNavigationMenuUI();
                    } else {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            binding.navigationMenu.setOnFocusChangeListener(null);
                            isNavigationMenuVisible = true;
                            showNavigationMenu(binding.navigationMenu);
                            Const.LAST_KEY_DOWN_TIME = currentTime;
                            return false;
                        } else {
                            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            binding.navigationMenu.setOnFocusChangeListener(null);
                            binding.navigationMenu.setupDefaultNavigationMenu(DEFAULT_SELECTED_POSITION);
                            NavigationInnerData defaultNavigation = navigationMenuData.get(DEFAULT_SELECTED_POSITION - 3);
                            replaceSelectedFragment(PrimaryNavigationFragment.newInstance(defaultNavigation.getPath(), defaultNavigation.getPrimaryNavigationType(), defaultNavigation.getLabel()), defaultNavigation.getLabel());
                            new Handler(Looper.getMainLooper()).postDelayed(this::setupFocusListener, 1000);
                            Const.LAST_KEY_DOWN_TIME = currentTime;
                            return true;
                        }
                    }
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && isNavigationMenuVisible && binding.errorContainer.getVisibility() == View.GONE) {
                Const.LAST_KEY_DOWN_TIME = currentTime;
                return clearNavigationMenuUI();
            } else if (fragment instanceof LivePrimaryNavigationFragment) {
                Boolean returnValue = LivePrimaryNavigationFragment.getInstance().setKeyEvents(keyCode, event);
                Const.LAST_KEY_DOWN_TIME = currentTime;
                if (returnValue == null) return super.onKeyDown(keyCode, event);
                else return true;
            } else {
                Const.LAST_KEY_DOWN_TIME = currentTime;
                return keyCode == KeyEvent.KEYCODE_DPAD_LEFT && isNavigationMenuVisible && binding.errorContainer.getVisibility() == View.GONE;
            }
        }
    }

    private boolean clearNavigationMenuUI() {
        isNavigationMenuVisible = false;
        binding.frameContainer.requestFocus();
        hideNavigationMenu(binding.navigationMenu);
        setupFocusListener();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Const.isFireTV) {
            PurchasingService.getUserData();
            PurchasingService.getPurchaseUpdates(false);
            final Set<String> productSkus = new HashSet<>();
            productSkus.add(getPackageName() + ".consumable." + "one");
            PurchasingService.getProductData(productSkus);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (primaryNavigationCall != null && primaryNavigationCall.isExecuted()) {
            primaryNavigationCall.cancel();
            primaryNavigationCall = null;
        }
        if (userProfileCall != null && userProfileCall.isExecuted()) {
            userProfileCall.cancel();
            userProfileCall = null;
        }
        binding.navigationMenu.setOnFocusChangeListener(null);
        if (networkWatcher != null && networkObserver != null) {
            networkWatcher.removeObserver(networkObserver);
            networkWatcher.removeObservers(this);
        }
        super.onDestroy();
    }

    @Override
    public void onUserDataResponse(UserDataResponse userDataResponse) {
        AppUtils.logger("Fire TV -- IAP -- On User Data Response");
        try {
            AppUtils.logger("Fire TV -- IAP -- On User Data Response - " + userDataResponse.toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final UserDataResponse.RequestStatus status = userDataResponse.getRequestStatus();
        AppUtils.logger("Fire TV -- IAP -- On User Data Response - status - " + status);
        switch (status) {
            case SUCCESSFUL:
                String currentUserId = userDataResponse.getUserData().getUserId();
                String currentMarketplace = userDataResponse.getUserData().getMarketplace();
                AppUtils.logger("Fire TV -- IAP -- On User Data Response - status - successful - user id - " + currentUserId);
                AppUtils.logger("Fire TV -- IAP -- On User Data Response - status - successful - user market place - " + currentMarketplace);
                break;
            case FAILED:
            case NOT_SUPPORTED:
                AppUtils.logger("Fire TV -- IAP -- On User Data Response - status failed or not supported ");
                // Fail gracefully.
                break;
        }
    }

    @Override
    public void onProductDataResponse(ProductDataResponse productDataResponse) {
        AppUtils.logger("Fire TV -- IAP -- On Product Data Response");
        try {
            AppUtils.logger("Fire TV -- IAP -- On Product Data Response - " + productDataResponse.toJSON().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (productDataResponse.getRequestStatus()) {
            case SUCCESSFUL:
                for (final String s : productDataResponse.getUnavailableSkus()) {
                    AppUtils.logger("Fire TV -- IAP -- Unavailable SKU:" + s);
                }

                final Map<String, Product> products = productDataResponse.getProductData();
                for (final String key : products.keySet()) {
                    Product product = products.get(key);
                    if (product != null)
                        AppUtils.logger("Fire TV -- IAP -- " + String.format("Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n", product.getTitle(), product.getProductType(), product.getSku(), product.getPrice(), product.getDescription()));
                    else
                        AppUtils.logger("Fire TV -- IAP -- product is null");
                }
                break;

            case FAILED:
                AppUtils.logger("Fire TV -- IAP -- ProductDataRequestStatus: FAILED");
                break;
        }
    }

    @Override
    public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
        AppUtils.logger("Fire TV -- IAP -- On Purchase Response");
        try {
            AppUtils.logger("Fire TV -- IAP -- On Purchase Response - " + purchaseResponse.toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (purchaseResponse.getRequestStatus()) {
            case SUCCESSFUL:
                final Receipt receipt = purchaseResponse.getReceipt();
                AppUtils.logger("Fire TV -- IAP -- onPurchaseResponse: receipt json:" + receipt.toJSON());
                if (receipt.isCanceled()) {
                    AppUtils.logger("Fire TV -- IAP --  receipt is cancelled");
                    return;
                } else {
                    AppUtils.logger("Fire TV -- IAP --  receipt is not cancelled -- need to verify and need to check that its already fulfilled or not");
                    */
/* Comment: To check receipt is fulfilled or not
                    if (receiptAlreadyFulfilled(receipt.getReceiptId(), userData)) {
                        // if the receipt was fulfilled before, just notify Amazon
                        // Appstore it's Fulfilled again.
                        PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
                        return;
                    }*//*

                }
                AppUtils.logger("Fire TV -- IAP -- grant consumable items ");
                */
/* TODO : Create Order*//*

                */
/*createOrder(receipt.getReceiptId(), purchaseResponse.getUserData().getUserId());*//*

                break;
            case NOT_SUPPORTED:
                Toast.makeText(HomeScreen.this, "Payment is not supported. Cancelling the payment.", Toast.LENGTH_SHORT).show();
                AppUtils.logger("Fire TV -- IAP -- onPurchaseResponse: not supported ");
                break;
            case INVALID_SKU:
                Toast.makeText(HomeScreen.this, "Something went wrong. Please contact " + getResources().getString(R.string.app_name) + " to report the issue.", Toast.LENGTH_SHORT).show();
                AppUtils.logger("Fire TV -- IAP -- onPurchaseResponse: invalid SKU ");
                break;
            case ALREADY_PURCHASED:
                showError(getResources().getString(R.string.plan_already_purchased), "PLANS_DIALOG");
                AppUtils.logger("Fire TV -- IAP -- onPurchaseResponse: failed ");
                break;
            case FAILED:
//                Toast.makeText(HomeScreen.this, "Payment failed. Something went wrong. Please contact " + getResources().getString(R.string.app_name) + " to report the issue.", Toast.LENGTH_SHORT).show();
                AppUtils.logger("Fire TV -- IAP -- onPurchaseResponse: failed / cancelled by user");
                break;
        }
    }

    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {
        AppUtils.logger("Fire TV -- IAP -- On Purchase update Response");
        try {
            AppUtils.logger("Fire TV -- IAP -- On Purchase update Response - " + purchaseUpdatesResponse.toJSON().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (purchaseUpdatesResponse.getRequestStatus()) {
            case SUCCESSFUL:
                for (final Receipt receipt : purchaseUpdatesResponse.getReceipts()) {
                    AppUtils.logger("Fire TV -- IAP --  receipts - " + receipt.toString());
                    // Process receipts
                }
                if (purchaseUpdatesResponse.hasMore()) {
                    PurchasingService.getPurchaseUpdates(reset);
                }
                break;
            case FAILED:
                break;
        }
    }
}*/
