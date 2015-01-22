package net.yazeed44.groceryshopping.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFlat;

import net.yazeed44.groceryshopping.R;
import net.yazeed44.groceryshopping.utils.Item;
import net.yazeed44.groceryshopping.utils.ViewUtil;

/**
 * Created by yazeed44 on 1/20/15.
 */
public class ReviewItemsActivity extends BaseActivity {


    private String mNotePrefix;
    private String mAmountPrefix;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_items);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.actionbar_title_review_items);

        initPrefixes();
        initRecycler();


    }

    private void initPrefixes() {
        mAmountPrefix = getResources().getString(R.string.amount_review_item) + " : ";
        mNotePrefix = getResources().getString(R.string.note_review_item) + " : ";
    }

    private void initRecycler() {
        final RecyclerView itemsRecycler = (RecyclerView) findViewById(R.id.review_items_recycler);
        itemsRecycler.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.review_items_column_num));
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        itemsRecycler.setLayoutManager(gridLayoutManager);
        itemsRecycler.setAdapter(new ReviewItemsAdapter(itemsRecycler));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_done:
                openItemsMsgDialog();
                break;

            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void openItemsMsgDialog() {
        final Intent msgIntent = new Intent(this, ItemsMsgActivity.class);
        startActivity(msgIntent);


    }

    private static interface ReviewItemListener {
        void onClickPutNote(final View putNoteBtn);

        void onClickPutAmount(final View putAmountBtn);

        void onClickClear(final View clearView);
    }

    private class ReviewItemsAdapter extends RecyclerView.Adapter<ReviewItemsHolder> implements ReviewItemListener {
        private RecyclerView mRecycler;

        private ReviewItemsAdapter(final RecyclerView recyclerView) {
            mRecycler = recyclerView;
        }

        @Override
        public ReviewItemsHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            final View reviewItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_review_item, parent, false);
            return new ReviewItemsHolder(reviewItemView, this);
        }

        @Override
        public void onBindViewHolder(ReviewItemsHolder holder, int position) {
            final Item item = MainActivity.sCheckedItems.get(position);


            holder.mTitle.setText(item.name);
            holder.mAmount.setText(mAmountPrefix + item.getAmount() + " " + item.combination);
            holder.mNote.setText(mNotePrefix + item.getNote());


        }

        @Override
        public int getItemCount() {
            return MainActivity.sCheckedItems.size();
        }


        private void showTypeAmountDialog(final Item item) {
            final EditText amountText = new EditText(ReviewItemsActivity.this);
            amountText.setHint(R.string.hint_type_amount);
            amountText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);


            createAskDialog(amountText)
                    .title(item.name)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            item.setAmount(Float.parseFloat(amountText.getText().toString()));
                            Log.i("edit Amount", item.name + "  has assigned new amount  " + item.getAmount());
                            updateChild(item);
                        }
                    }).show();

        }


        private void showTypeNoteDialog(final Item item) {
            final EditText noteText = new EditText(ReviewItemsActivity.this);
            noteText.setHint(R.string.hint_type_note);
            noteText.setInputType(InputType.TYPE_CLASS_TEXT);

            createAskDialog(noteText)
                    .title(item.name)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            item.setNote(noteText.getText().toString());
                            Log.i("Edit Note", item.name + "  has assigned new note   " + item.getNote());
                            updateChild(item);
                        }


                    }).show();

        }

        private MaterialDialog.Builder createAskDialog(final EditText editText) {
            editText.requestFocus();
            return ViewUtil.createDialog(ReviewItemsActivity.this)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .customView(editText, false);


        }

        private void updateChild(final Item item) {
            final int position = MainActivity.sCheckedItems.indexOf(item);

            notifyItemChanged(position);
            Log.d("updateChild", item.name + " should be updated by now");


        }


        @Override
        public void onClickPutNote(View view) {
            final Item item = getItemObject(view);
            showTypeNoteDialog(item);
        }

        @Override
        public void onClickPutAmount(View view) {
            final Item item = getItemObject(view);
            showTypeAmountDialog(item);
        }

        @Override
        public void onClickClear(View clearView) {
            final int itemIndex = MainActivity.sCheckedItems.indexOf(getItemObject(clearView));
            final Item item = MainActivity.sCheckedItems.get(itemIndex);


            ViewUtil.createDialog(ReviewItemsActivity.this)
                    .content(getClearContent(item))
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            notifyItemRemoved(itemIndex);
                            MainActivity.sCheckedItems.remove(item);
                        }
                    }).show();


            Log.d("CheckedItems", MainActivity.sCheckedItems.toString());

        }

        private String getClearContent(final Item item) {
            final String baseContent = getResources().getString(R.string.content_clear_item_dialog);

            return baseContent.replace("0", item.name);
        }


        private Item getItemObject(final View view) {


            View parent = (View) view.getParent();
            while (parent.getId() != R.id.review_item_card) {
                parent = (View) parent.getParent();
            }
            final int index = mRecycler.getChildPosition(parent);
            return MainActivity.sCheckedItems.get(index);
        }
    }

    private class ReviewItemsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitle;
        private TextView mAmount;
        private TextView mNote;
        private ButtonFlat mPutNote;
        private ButtonFlat mPutAmount;
        private ReviewItemListener mListener;
        private ImageView mClearImage;

        public ReviewItemsHolder(View itemView, final ReviewItemListener listener) {
            super(itemView);
            mListener = listener;
            mTitle = (TextView) itemView.findViewById(R.id.review_item_title);
            mAmount = (TextView) itemView.findViewById(R.id.review_item_amount);
            mNote = (TextView) itemView.findViewById(R.id.review_item_note);

            mPutNote = (ButtonFlat) itemView.findViewById(R.id.review_item_put_note);
            mPutNote.setOnClickListener(this);

            mPutAmount = (ButtonFlat) itemView.findViewById(R.id.review_item_put_amount);
            mPutAmount.setOnClickListener(this);

            setupBtns();

            mClearImage = (ImageView) itemView.findViewById(R.id.review_item_clear);
            mClearImage.setOnClickListener(this);
        }

        private void setupBtns() {
            final Typeface flatButtonTypeface = Typeface.create(ViewUtil.getMediumTypeface(), Typeface.BOLD);

            mPutAmount.getTextView().setTypeface(flatButtonTypeface);
            mPutNote.getTextView().setTypeface(flatButtonTypeface);

            final float textSize = getResources().getDimensionPixelSize(R.dimen.review_item_btns_text_size);
            mPutAmount.getTextView().setTextSize(textSize);
            mPutNote.getTextView().setTextSize(textSize);
        }

        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.review_item_put_note:
                    mListener.onClickPutNote(v);
                    break;

                case R.id.review_item_put_amount:
                    mListener.onClickPutAmount(v);
                    break;

                case R.id.review_item_clear:
                    mListener.onClickClear(v);
                    break;

                default:
                    break;
            }
        }


    }


}