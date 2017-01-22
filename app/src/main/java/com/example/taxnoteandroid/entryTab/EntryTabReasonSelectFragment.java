package com.example.taxnoteandroid.entryTab;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.taxnoteandroid.AccountSelectActivity;
import com.example.taxnoteandroid.DatePickerDialogFragment;
import com.example.taxnoteandroid.DividerDecoration;
import com.example.taxnoteandroid.Library.DialogManager;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.databinding.FragmentEntryTabReasonSelectBinding;
import com.example.taxnoteandroid.databinding.ListviewFooterBinding;
import com.example.taxnoteandroid.databinding.RowListWithDetailsItemBinding;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.util.KeyboardUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class EntryTabReasonSelectFragment extends Fragment {

  private static final String EXTRA_IS_EXPENSE = "isExpense";

  private static final int TYPE_REASON = 0;
  private static final int TYPE_FOOTER = 1;

  public boolean isExpense = true;
  public long date;
  private Account account;
  private MyRecyclerViewAdapter adapter;
  private FragmentEntryTabReasonSelectBinding binding;
  private ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
  private List<Reason> reasonList;

  public EntryTabReasonSelectFragment() {
    // Required empty public constructor
  }

  public static EntryTabReasonSelectFragment newInstance(boolean isExpense) {

    EntryTabReasonSelectFragment fragment = new EntryTabReasonSelectFragment();

    // Set value on bundle
    Bundle args = new Bundle();
    args.putBoolean(EXTRA_IS_EXPENSE, isExpense);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set default values
    isExpense = getArguments().getBoolean(EXTRA_IS_EXPENSE);
    date = System.currentTimeMillis();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    binding = FragmentEntryTabReasonSelectBinding.inflate(inflater, container, false);
    binding.setIsExpense(isExpense);

    View view = binding.getRoot();

    setDateView();
    setAccountView(view);
    setReasonList(view);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    loadCurrentDate();
    loadCurrentAccount();
  }

  //--------------------------------------------------------------//
  //    -- Reason List --
  //--------------------------------------------------------------//

  private void setReasonList(View view) {

    // Adapter
    adapter = new MyRecyclerViewAdapter();

    // RecyclerView
    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.reason_list);
    recyclerView.addItemDecoration(new DividerDecoration(getContext()));
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setAdapter(adapter); // 2017/01/17 E.Nozaki recyclerView.setAdapter(reasonListAdapter);

    // Attach recyclerView to ItemTouchHelper so that you can drag and drop the items in order to change the order.
    ItemTouchHelper.Callback callback = new ItemTouchHelperCallback();
    ItemTouchHelper helper = new ItemTouchHelper(callback);
    helper.attachToRecyclerView(recyclerView);
  }

  public class ItemTouchHelperCallback extends ItemTouchHelper.Callback{
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder){
      int flags_drag  = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT; // Catch actions for moving an item toward up, down, left and right.
      int flags_swipe = 0; // Ignore actions for swiping an item.
      return makeMovementFlags(flags_drag, flags_swipe);
    }

    @Override
    public boolean isLongPressDragEnabled() {
      return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
      return false;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target){

      int position_from = viewHolder.getAdapterPosition();
      int position_to   = target.getAdapterPosition();

      Log.d(getClass().getSimpleName(), "---------------------------------");
      Log.d(getClass().getSimpleName(), "position_from = " + position_from);
      Log.d(getClass().getSimpleName(), "position_to = " + position_to);

      int size = reasonList.size();

      if(position_from<0 || size<=position_from ||
         position_to  <0 || size<=position_to){
        return false;
      }

      if(position_from==position_to){
        return false;
      }

      ArrayList<Reason> list = new ArrayList<Reason>();

      for (int i = 0; i < size; i++) {
        if(i==position_to)   list.add(reasonList.get(position_from));
        if(i!=position_from) list.add(reasonList.get(i));
      }

      reasonList = list;

      for ( int i = 0; i < size; i++ ) {
          reasonDataManager.updateOrder(reasonList.get(i).id, i); // Update database
      }

      adapter.notifyItemMoved(position_from, position_to);

      return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder holder, int action){

      super.onSelectedChanged(holder, action);

      try {
        if(holder!=null){
          if(action==ItemTouchHelper.ACTION_STATE_DRAG){
//            holder.itemView.setBackgroundColor(Color.YELLOW); // TODO Do something else to highlight the item that is dragged.
          }
        }
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }

    @Override
    public void clearView(RecyclerView view, RecyclerView.ViewHolder holder){

      super.clearView(view, holder);

      try {
        if(holder!=null){
//          holder.itemView.setBackgroundColor(Color.TRANSPARENT); // TODO Do something else to clear highlight the item that is dragged.
        }
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction){
      // Do nothing here since swiping action is not supported by this app.
    }
  }


  //--------------------------------------------------------------//
  //    -- Date Part --
  //--------------------------------------------------------------//

  private void setDateView() {

    binding.dateTextView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(date, null);
        fragment.setOnDateSetListener(new DatePickerDialogFragment.OnDateSetListener() {
          @Override
          public void onDateSet(Calendar calendar) {
            date = calendar.getTimeInMillis();
            loadCurrentDate();
          }
        });
        fragment.show(getFragmentManager(), DatePickerDialogFragment.class.getName());
      }
    });
  }

  private void loadCurrentDate() {

    String dateString = getResources().getString(R.string.date_string_today);

    // Show the date if it is not today
    if (!DateUtils.isToday(date)) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_string_format_to_year_month_day_weekday));
      dateString = simpleDateFormat.format(date);
    }

    ((TextView) getView().findViewById(R.id.date_text_view)).setText(dateString);
  }


  //--------------------------------------------------------------//
  //    -- Account Part --
  //--------------------------------------------------------------//

  private void setAccountView(View view) {

    view.findViewById(R.id.account_text_view).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(AccountSelectActivity.createIntent(getContext(), isExpense));
      }
    });
  }

  private void loadCurrentAccount() {

    AccountDataManager accountDataManager = new AccountDataManager(getContext());
    account = accountDataManager.findCurrentSelectedAccount(getContext(), isExpense);

    ((TextView) getView().findViewById(R.id.account_text_view)).setText(account.name);
  }


  //--------------------------------------------------------------//
  //    -- Reason List Adapter --
  //--------------------------------------------------------------//

  private class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public MyRecyclerViewAdapter(){
      onReasonDataManagerChanged();
    }

    @Override
    public int getItemViewType(int position){
      if(position<reasonList.size()){
        return TYPE_REASON;
      }else{
        return TYPE_FOOTER;
      }
    }

    private Reason getItem(int position){
      if(reasonList!=null && position<reasonList.size()){
        return reasonList.get(position);
      }
      return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
      Reason reason = getItem(position);
      MyViewHolder h = (MyViewHolder)holder;
      h.setReason(position, getItem(position));
    }

    @Override
    public int getItemCount(){
      if(reasonList!=null){
        return reasonList.size() + 1; // Add 1 for footer.
      }
      return 1;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int type){
      try{
        View view = null;

        if(type==TYPE_REASON){
          view = LayoutInflater.from(getContext()).inflate(R.layout.row_list_with_details_item, null);
        }else if(type==TYPE_FOOTER){
          view = LayoutInflater.from(getContext()).inflate(R.layout.listview_footer, null);
        }

        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
      }
      catch(Exception e){
        e.printStackTrace();
      }

      return null;
    }

    public void onReasonDataManagerChanged(){
      reasonList = reasonDataManager.findAllWithIsExpense(isExpense, getContext());
      this.notifyDataSetChanged();
    }
  }


  //--------------------------------------------------------------//
  //    -- Reason List View Holder --
  //--------------------------------------------------------------//

  public class MyViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

    public int position;
    public Reason reason;
    public View button_right;

    public MyViewHolder(View view){
      super(view);
    }

    public void setReason(final int position, final Reason reason){
      try{
        this.position = position;
        this.reason = reason;

        Object obj = DataBindingUtil.bind(this.itemView);

        if(obj instanceof RowListWithDetailsItemBinding){
          RowListWithDetailsItemBinding binding = (RowListWithDetailsItemBinding) obj;
          binding.title.setText(reason.name);
          binding.details.setText(reason.details);
          binding.details.setVisibility(TextUtils.isEmpty(reason.details) ? View.GONE : View.VISIBLE);
          this.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
              onClick_Reason(view);
            }
          });

          final PopupMenu popup = new PopupMenu(getContext(), binding.menuRight);
          popup.getMenuInflater().inflate(R.menu.menu_category_right, popup.getMenu());
          popup.setOnMenuItemClickListener(this);
          binding.menuRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              popup.show();
            }
          });
        }
        else if(obj instanceof ListviewFooterBinding){
          this.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
              onClick_Footer(view);
            }
          });
          ListviewFooterBinding binding = (ListviewFooterBinding) obj;
          binding.text.setText(getResources().getString(R.string.list_view_add_reason));
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }

    public void onClick_Reason(View view){
      startActivity(SummaryActivity.createIntent(getContext(), isExpense, date, account, reason));
    }

    public void onClick_Footer(View view){
      final Context context = getContext();
      final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);

      new AlertDialog.Builder(context)
        .setView(textInputView)
        .setTitle(getResources().getString(R.string.list_view_add_reason))
        .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {
            EditText editText = (EditText) textInputView.findViewById(R.id.edit);

            try{
              String newName = editText.getText().toString();

              // Check empty
              if(newName.isEmpty()){
                return;
              }

              ProjectDataManager projectDataManager = new ProjectDataManager(context);
              Project project = projectDataManager.findCurrentProjectWithContext(context);

              List<Reason> reasonList = reasonDataManager.findAllWithIsExpense(isExpense, context);

              Reason reason = new Reason();
              reason.name = newName;
              reason.uuid = UUID.randomUUID().toString();

              if(reasonList.isEmpty()){
                reason.order = 0;
              }
              else{
                reason.order = reasonList.get(reasonList.size() - 1).order + 1;
              }
              reason.isExpense = isExpense;
              reason.project = project;
              reason.details = "";

              long id = reasonDataManager.save(reason);

              // Success
              if(id!=-1){

                reason.id = id;
                adapter.notifyDataSetChanged();
                dialogInterface.dismiss();

                DialogManager.showToast(context, newName);
              }
            }finally{
              KeyboardUtil.hideKeyboard(getActivity(), editText);
            }
          }
        })

        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            EditText editText = (EditText) textInputView.findViewById(R.id.edit);
            KeyboardUtil.hideKeyboard(getActivity(), editText); // 2017/01/17 E.Nozaki Hide software keyboard.
          }
        })
        .show();

        KeyboardUtil.showKeyboard(getActivity(), textInputView); // 2017/01/17 E.Nozaki Show software keyboard.
    }

    @Override
    public boolean onMenuItemClick(MenuItem item){
      switch (item.getItemId()) {

        case R.id.rename:
          renameReason(reason, position);
          break;

        case R.id.delete:
          deleteReason(reason);
          break;
      }
      return true;
    }
  }


  //--------------------------------------------------------------//
  //    -- Rename --
  //--------------------------------------------------------------//

  private void renameReason(final Reason reason, final int position) {

    final Context context = getContext();
    final View textInputView = LayoutInflater.from(context).inflate(R.layout.dialog_text_input, null);

    final EditText editText = (EditText) textInputView.findViewById(R.id.edit);
    editText.setText(reason.name);

    new AlertDialog.Builder(context)
      .setView(textInputView)
      .setTitle(getResources().getString(R.string.list_view_rename))
      .setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {

          try{
            String reasonName = editText.getText().toString();

            ReasonDataManager reasonDataManager = new ReasonDataManager(getContext());
            reasonDataManager.updateName(reason.id, reasonName);

            Reason oldReason = adapter.getItem(position);
            if (oldReason != null) {
              oldReason.name = reasonName;
              reasonDataManager.updateName(reason.id, reasonName);
              adapter.onReasonDataManagerChanged();
            }
          }finally{
            KeyboardUtil.hideKeyboard(getActivity(), editText); // 2017/01/17 E.Nozaki Hide software keyboard.
          }
        }
      })
      .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialogInterface, int i){
          EditText editText = (EditText) textInputView.findViewById(R.id.edit);
          KeyboardUtil.hideKeyboard(getActivity(), editText) ; // 2017/01/17 E.Nozaki Hide software keyboard.
        }
      })
      .show();

    KeyboardUtil.showKeyboard(getActivity(), textInputView); // 2017/01/17 E.Nozaki Show software keyboard.
  }


  //--------------------------------------------------------------//
  //    -- Delete --
  //--------------------------------------------------------------//

  private void deleteReason(final Reason reason) {

    // Check if Entry data has this reason already
    EntryDataManager entryDataManager   = new EntryDataManager(getContext());
    Entry entry                         = entryDataManager.hasReasonInEntryData(reason);

    if (entry != null) {

      // Show error message
      new AlertDialog.Builder(getContext())
        .setTitle(getResources().getString(R.string.Error))
        .setMessage(getResources().getString(R.string.using_this_account_in_entry_already))
        .setPositiveButton("OK", null)
        .show();
      return;
    }

    // Confirm dialog
    new AlertDialog.Builder(getContext())
      .setTitle(reason.name)
      .setMessage(getResources().getString(R.string.delete_confirm_message))
      .setPositiveButton(getResources().getString(R.string.Delete), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          dialogInterface.dismiss();

          reasonDataManager.delete(reason.id);
          adapter.onReasonDataManagerChanged();

          String message = reason.name + getResources().getString(R.string.delete_done_after_title);
          DialogManager.showToast(getActivity(), message);
        }
      })
      .setNegativeButton(getResources().getString(R.string.cancel), null)
      .show();
    }
}
