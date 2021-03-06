package cn.crepusculo.subway_ticket_android.ui.fragment.orders;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.subwayticket.database.model.TicketOrder;
import com.subwayticket.model.result.OrderListResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cn.crepusculo.subway_ticket_android.R;
import cn.crepusculo.subway_ticket_android.preferences.Info;
import cn.crepusculo.subway_ticket_android.ui.activity.DisplayActivity;
import cn.crepusculo.subway_ticket_android.ui.activity.TicketManagerActivity;
import cn.crepusculo.subway_ticket_android.ui.adapter.TicketRecyclerAdapter;
import cn.crepusculo.subway_ticket_android.ui.fragment.BaseFragment;
import cn.crepusculo.subway_ticket_android.util.CalendarUtils;
import cn.crepusculo.subway_ticket_android.util.GsonUtils;
import cn.crepusculo.subway_ticket_android.util.NetworkUtils;

public class TicketHistoryFragment extends BaseFragment {
    private ArrayList<TicketOrder> serverResult = new ArrayList<>();
    private EditText editStartDate;
    private EditText editEndDate;
    private Button searchButton;
    private TextView textView;
    private RecyclerView recyclerView;
    private boolean refreshFlag = false;
    private boolean searchFlag = false;
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_ticket_history;
    }

    private void updateDate(){

    }

    @Override
    protected void initView() {
        editStartDate = (EditText) mRootView.findViewById(R.id.edit_start_date);
        editStartDate.setText(dateFormat.format(startDate.getTime()));
        editStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        startDate.set(year, month, date);
                        editStartDate.setText(dateFormat.format(startDate.getTime()));
                    }
                }, startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH),
                        startDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        editEndDate = (EditText) mRootView.findViewById(R.id.edit_end_date);
        editEndDate.setText(dateFormat.format(endDate.getTime()));
        editEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                        endDate.set(year, month, date);
                        editEndDate.setText(dateFormat.format(endDate.getTime()));
                    }
                }, endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH),
                        endDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        searchButton = (Button) mRootView.findViewById(R.id.btn_search_history_order);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchFlag = true;
                refreshDataFromServer();
            }
        });
        recyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        recyclerView.setNestedScrollingEnabled(false);
        textView = (TextView) mRootView.findViewById(R.id.textView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(refreshFlag){
            ((TicketManagerActivity)getActivity()).setRefreshNotExtract(true);
            ((TicketManagerActivity)getActivity()).setRefreshNotPay(true);
            ((TicketManagerActivity)getActivity()).setRefreshHistory(false);
            refreshDataFromServer();
            refreshFlag = false;
        }
    }

    public void refreshDataFromServer() {
        if(!searchFlag)
            return;
        Log.e("HistoryFragment", "refreshDataFromServer");
        NetworkUtils.ticketOrderGetOrderListByStartTimeAndEndTime(
                "" + startDate.getTimeInMillis(),
                "" + endDate.getTimeInMillis(),
                Info.getInstance().getToken(),
                new Response.Listener<OrderListResult>() {
                    @Override
                    public void onResponse(OrderListResult response) {
                        Log.e("HistoryFragment", "Success get TicketOrder" + response.getTicketOrderList().size());
                        serverResult.clear();
                        serverResult.addAll(response.getTicketOrderList());
                        convertData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        serverResult.clear();
                        try {
                            GsonUtils.Response r = GsonUtils.resolveErrorResponse(error);
                            Log.e("HistoryFragment", "Error" + r.result_description);
                            textView.setText(r.result_description);
                        } catch (NullPointerException e) {
                            textView.setText(R.string.network_error);
                        }
                        convertData();
                    }
                });
    }

    private void showDisplayView(int mode) {
        if (mode == Mode.PROGRESS) {
            recyclerView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        } else if (mode == Mode.RECYCLE) {
            recyclerView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }
    }

    private void convertData() {
        if (!serverResult.isEmpty()) {
            /**
             * Get Data from Sever Successful
             */
            RecyclerView.LayoutManager layoutManager;
            layoutManager = new LinearLayoutManager(getActivity());

            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);

            TicketRecyclerAdapter adapter = new TicketRecyclerAdapter(this.getActivity(), serverResult,
                    new TicketRecyclerAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(TicketOrder item, TicketRecyclerAdapter.Holder holder) {
                            Bundle b = new Bundle();
                            b.putString(DisplayActivity.BUNDLE_KEY_ORDER, new Gson().toJson(item));
                            Intent intent = new Intent(getActivity(), DisplayActivity.class);
                            intent.putExtras(b);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                            refreshFlag = true;
                        }
                    });
            recyclerView.setAdapter(adapter);
            showDisplayView(Mode.RECYCLE);
        } else {
            /**
             * Else load cache text view
             */
            showDisplayView(Mode.PROGRESS);
        }
    }

    private class Mode {
        public static final int PROGRESS = 1;
        public static final int RECYCLE = 2;
    }
}
