package net.yazeed44.groceryshopping.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.easyandroidanimations.library.Animation;
import com.easyandroidanimations.library.AnimationListener;
import com.easyandroidanimations.library.BlinkAnimation;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import net.yazeed44.groceryshopping.R;
import net.yazeed44.groceryshopping.requests.CheckAppStatusRequest;
import net.yazeed44.groceryshopping.requests.CheckForAdUpdatesRequest;
import net.yazeed44.groceryshopping.requests.CheckForDbUpdatesRequest;
import net.yazeed44.groceryshopping.utils.Category;
import net.yazeed44.groceryshopping.utils.DBUtil;
import net.yazeed44.groceryshopping.utils.Item;
import net.yazeed44.groceryshopping.utils.ViewUtil;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends BaseActivity implements CategoriesFragment.OnClickCategoryListener, ItemsFragment.OnCheckItemListener, DBUtil.OnInstallingDbListener {


    public static final String KEY_CATEGORY_INDEX = "categoryIndexKey";
    public static final ArrayList<Item> CHOSEN_ITEMS = new ArrayList<>();
    @InjectView(R.id.items_search_view)
    SearchView mItemsSearchView;

    private CategoriesFragment mCategoriesFragment;
    private ItemsTabsFragment mItemsFragment;
    private SearchItemsFragment mSearchItemsFragment;


    private ImageView mShoppingCartImageView;
    private TextView mShoppingCartCounterView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(null);

        ButterKnife.inject(this);


        initUtils();

        if (savedInstanceState == null) {
            executeCheckDbUpdate();
            executeCheckAdUpdate();
            executeCheckAppStatus();

        }

        showCategories(savedInstanceState);


        setupItemsSearch();


    }


    private void initUtils() {

        ViewUtil.init(this);
        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this).build());

        DBUtil.initInstance(this);

    }

    private void executeCheckDbUpdate() {

        final CheckForDbUpdatesRequest checkRequest = new CheckForDbUpdatesRequest(this);
        spiceManager.execute(checkRequest, new RequestListener<CheckForDbUpdatesRequest.Result>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                //TODO Handle exception

                Log.e("CheckForDbUpdate", spiceException.getMessage());

            }

            @Override
            public void onRequestSuccess(CheckForDbUpdatesRequest.Result result) {

                Log.i("CheckForDBUpdate", "Result is  " + result.toString());

                switch (result) {
                    case NEW_UPDATE:
                        showUpdateDialog();
                        break;
                    case NO_DB:
                        DBUtil.installNewDb(MainActivity.this, MainActivity.this);
                        break;

                    case NO_NEW_UPDATE:
                        break;

                }

            }
        });
    }

    private void executeCheckAdUpdate() {
        final CheckForAdUpdatesRequest checkRequest = new CheckForAdUpdatesRequest(this);
        spiceManager.execute(checkRequest, new RequestListener<CheckForAdUpdatesRequest.Result>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {

                Log.e("CheckAdUpdateException", spiceException.getMessage());
            }

            @Override
            public void onRequestSuccess(CheckForAdUpdatesRequest.Result result) {
                Log.i(CheckForAdUpdatesRequest.TAG, "Result is  " + result);

                switch (result) {

                    case NEW_UPDATE:
                        DBUtil.resetAds();
                        onResume();
                        break;

                    case NO_NEW_UPDATE:
                        break;
                }

            }
        });

    }

    private void executeCheckAppStatus() {
        final CheckAppStatusRequest checkAppStatusRequest = new CheckAppStatusRequest(this);

        spiceManager.execute(checkAppStatusRequest, new RequestListener<CheckAppStatusRequest.Result>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Log.e("AppStatusException", spiceException.getMessage());

            }

            @Override
            public void onRequestSuccess(CheckAppStatusRequest.Result result) {

                if (result == null) {
                    return;
                }

                Log.i(CheckAppStatusRequest.TAG, "Result is  " + result);

                switch (result) {

                    case SHOULD_CONTINUE:
                        break;

                    case SHOULD_STOP:
                        throw new NullPointerException("This app isn't supposed to work");
                        // finish();
                        // break;

                }

            }
        });
    }


    private void showUpdateDialog() {
        ViewUtil.createDialog(this)
                .negativeText(R.string.neg_btn_update_dialog)
                .content(R.string.content_new_update)
                .title(R.string.title_new_update)
                .positiveText(R.string.pos_btn_update_dialog)

                .callback(new MaterialDialog.ButtonCallback() {

                              @Override
                              public void onNegative(MaterialDialog materialDialog) {
                                  materialDialog.dismiss();
                              }

                              @Override
                              public void onPositive(MaterialDialog materialDialog) {
                                  materialDialog.dismiss();
                                  DBUtil.installNewDb(MainActivity.this, MainActivity.this);


                              }
                          }
                ).show();

    }


    private void showCategories(final Bundle bundle) {


        if (bundle == null) {
            mCategoriesFragment = new CategoriesFragment();
            mCategoriesFragment.setCategoryListener(this);
            Log.d("initialize categories", "Categories fragment and listener is initialized");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, mCategoriesFragment)
                    .commit();

        }


    }


    private void showSearchFragment() {
        //TODO Add animations
        if (mSearchItemsFragment == null) {
            mSearchItemsFragment = new SearchItemsFragment();
            mSearchItemsFragment.setListener(this);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, mSearchItemsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupItemsSearch() {


        mItemsSearchView.setQueryHint(getResources().getString(R.string.hint_search_items));

        mItemsSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchFragment();
            }
        });


        mItemsSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            private void query(final String query) {

                if (mSearchItemsFragment != null)
                    mSearchItemsFragment.query(query);
            }

            @Override
            public boolean onQueryTextSubmit(String s) {
                query(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                query(s);
                return false;
            }
        });


        mItemsSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mItemsSearchView.clearFocus();
                getSupportFragmentManager().popBackStack();
                return false;
            }
        });

        mItemsSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemsSearchView.onActionViewExpanded();
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_shopping_cart, menu);
        getMenuInflater().inflate(R.menu.menu_add_item_manually, menu);

        initShoppingCart(MenuItemCompat.getActionView(menu.findItem(R.id.action_shopping_cart)));
        setupShoppingCart();
        updateShoppingCart();
        return true;
    }

    private void initShoppingCart(final View cartLayout) {
        mShoppingCartImageView = ButterKnife.findById(cartLayout, R.id.action_shopping_cart_image);
        mShoppingCartCounterView = ButterKnife.findById(cartLayout, R.id.action_shopping_cart_counter);
    }

    private void setupShoppingCart() {

        mShoppingCartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReviewItemsActivity();
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {

            case R.id.action_add_item_manually:
                showAddItemDialog();
                break;

            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void showAddItemDialog() {
        final EditText itemTxt = new EditText(this);
        itemTxt.setHint(R.string.hint_add_item);

        ViewUtil.createDialog(this)
                .title(R.string.title_add_item)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .customView(itemTxt, false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        final String name = itemTxt.getText().toString();
                        onAddItem(new Item(name));
                    }
                })
                .show();
    }


    private void openReviewItemsActivity() {
        final Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClickCategory(Category category) {
        //TODO add animation

        showItems(category);
    }


    private void showItems(final Category category) {

        if (mItemsFragment == null) {
            mItemsFragment = new ItemsTabsFragment();
            mItemsFragment.setCheckListener(this);
            Log.d("showItems", "Items Fragment has been initialized");
        }


        final Bundle chosenCategoryBundle = new Bundle();
        chosenCategoryBundle.putInt(KEY_CATEGORY_INDEX, DBUtil.getCategories().indexOf(category));

        mItemsFragment.setArguments(chosenCategoryBundle);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, mItemsFragment)
                .addToBackStack(null)
                .commit();


    }

    @Override
    public void onBackPressed() {


        if (mItemsFragment != null && mItemsFragment.isVisible()) {
            getSupportFragmentManager().popBackStack();
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        } else if (mSearchItemsFragment != null && mSearchItemsFragment.isVisible()) {

            mItemsSearchView.onActionViewCollapsed();
            getSupportFragmentManager().popBackStack();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        } else {
            super.onBackPressed();
        }
    }

    void showSearchView() {
        ((View) mItemsSearchView.getParent()).setVisibility(View.VISIBLE);
    }

    void hideSearchView() {
        ((View) mItemsSearchView.getParent()).setVisibility(View.GONE);
    }

    @Override
    public void onAddItem(Item item) {
        CHOSEN_ITEMS.add(item);
        Log.d("onAddItem", item.name + "  has been added");
        updateShoppingCart();

    }

    @Override
    public void onRemoveItem(Item item) {
        CHOSEN_ITEMS.remove(item);
        Log.d("onRemoveItem", item.name + "  has been removed");
        updateShoppingCart();


    }

    private void updateShoppingCart() {

        new BlinkAnimation(mShoppingCartImageView)
                .setListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mShoppingCartCounterView.setText(CHOSEN_ITEMS.size() + "");
                    }
                })
                .animate();


    }

    public void updateItemsFragment(final Category chosenCategory) {

        DBUtil.resetCategoriesItems();
        if (mItemsFragment != null && !mItemsFragment.isVisible()) {
            return;
        }

        getSupportFragmentManager().beginTransaction().detach(mItemsFragment)
                .attach(mItemsFragment)
                .commit();
        mItemsFragment.setCurrentPage(chosenCategory);
    }

    private void restartActivity() {

        final Intent restartIntent = new Intent(this, MainActivity.class);
        startActivity(restartIntent);
    }

    @Override
    protected AdRecyclerView onCreateAd() {
        return null;
    }


    @Override
    public void onDbInstalledSuccessful(MainActivity activity) {
        ViewUtil.toastShort(this, R.string.toast_db_installed_successfully);

    }
}
