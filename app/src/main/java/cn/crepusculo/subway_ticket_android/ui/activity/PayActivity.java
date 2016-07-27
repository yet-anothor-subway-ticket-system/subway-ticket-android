package cn.crepusculo.subway_ticket_android.ui.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.subwayticket.database.model.City;
import com.subwayticket.model.result.CityListResult;

import java.util.Calendar;
import java.util.List;

import cn.crepusculo.subway_ticket_android.R;
import cn.crepusculo.subway_ticket_android.content.BillsCardViewContent;
import cn.crepusculo.subway_ticket_android.content.Station;
import cn.crepusculo.subway_ticket_android.preferences.Info;
import cn.crepusculo.subway_ticket_android.utils.CalendarUtils;
import cn.crepusculo.subway_ticket_android.utils.NetworkUtils;
import cn.crepusculo.subway_ticket_android.utils.SharedPreferencesUtils;
import cn.crepusculo.subway_ticket_android.utils.SubwayLineUtil;

public class PayActivity extends BaseActivity {
    private ImageButton startPic;
    private ImageButton destinationPic;

    private TextView startTitle;
    private TextView start;
    private TextView destinaionTitle;
    private TextView destination;

    private TextView date;
    private TextView dateLimit;

    private EditText editCount;
    private EditText editBills;
    private EditText editPrice;

    private BillsCardViewContent payRequest;

    private Activity activity;

    private Button checkButton;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_pay;
    }

    @Override
    protected void initView() {
        activity = this;
        payRequest = new BillsCardViewContent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        buildBills();
        loadCompacts();
        setCardInfo(payRequest);
    }

    private void loadCompacts() {
        startPic = (ImageButton) findViewById(R.id.start_pic);
        destinationPic = (ImageButton) findViewById(R.id.destination_pic);
        startTitle = (TextView) findViewById(R.id.start_title);
        start = (TextView) findViewById(R.id.start);
        destinaionTitle = (TextView) findViewById(R.id.destination_title);
        destination = (TextView) findViewById(R.id.destination);
        date = (TextView) findViewById(R.id.date);
        dateLimit = (TextView) findViewById(R.id.date_limit);

        destinationPic.setOnClickListener(new ImageButtonOnClickListener());
        startPic.setOnClickListener(new ImageButtonOnClickListener());

        editCount = (EditText) findViewById(R.id.count);
        editPrice = (EditText) findViewById(R.id.price);
        editBills = (EditText) findViewById(R.id.show_money);

        editPrice.setText(String.valueOf(payRequest.price));

        editCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String result = "";
                if (editCount.getText().toString().trim().length() != 0) {
                    Log.e("233", "num" + editCount.getText().toString().trim());
                    double count = payRequest.price * Integer.parseInt(editCount.getText().toString().trim());
                    result += count;
                } else {
                    result = "0.0";
                }
                editBills.setText(result);
            }
        });

        checkButton = (Button) findViewById(R.id.check_button);
//        checkButton.setBackgroundColor(getResources().getColor(R.color.primary));
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Info info  = Info.getInstance();
                String id = info.user.getId();
                String psw = info.user.getPassword();
                Toast.makeText(PayActivity.this, id+psw, Toast.LENGTH_LONG).show();
//                NetworkUtils.subwayGetCityList(
//                        new Response.Listener<CityListResult>() {
//                            @Override
//                            public void onResponse(CityListResult response) {
//                                List<City> list = response.getCityList();
//                                String result = "" + list.size() + "  ";;
//                                for (City c : list
//                                        ) {
//                                    result += c.getCityName();
//                                }
//                                Toast.makeText(PayActivity.this, result, Toast.LENGTH_LONG).show();
//                            }
//                        }
//                        , new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                            }
//                        }
//                );
            }
        });
    }


    private void buildBills() {
        Bundle b = getBundle();
        if (b != null) {
            String start_str = b.getString("route_start");
            String destination_str = b.getString("route_end");
            Log.e("PayActivity", start_str + destination_str);
            Station start = SubwayLineUtil.CutLineNameStr(start_str);
            Station end = SubwayLineUtil.CutLineNameStr(destination_str);
            payRequest.start.setName(start.getName());
            payRequest.start.setLine(start.getLine());
            payRequest.end.setName(end.getName());
            payRequest.end.setLine(end.getLine());
            /* default set date with system date */
            Calendar c = Calendar.getInstance();
            payRequest.date = CalendarUtils.format(c);
            payRequest.status = BillsCardViewContent.TICKET_UNPAID;
            payRequest.price = 3.0;
        } else {
            /* Failure to get tickle info */
            new MaterialDialog.Builder(this)
                    .title(R.string.error)
                    .titleColor(getResources().getColor(R.color.primary))
                    .content(R.string.error_failure_to_get_ticket_info)
                    .autoDismiss(true)
                    .show();
        }

    }

    private void setCardInfo(BillsCardViewContent info) {
        Calendar c = Calendar.getInstance();
        start.setText(info.start.getName());
        destination.setText(info.end.getName());
        date.setText(CalendarUtils.format(c));
        BillsCardViewContent.setTagColor(this,
                startPic, SubwayLineUtil.getColor(info.start.getLine()),
                destinationPic, SubwayLineUtil.getColor(info.end.getLine()));
        dateLimit.setText(CalendarUtils.format_limit(c));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in_center, R.anim.fade_out_center);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private class ImageButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Station swap = payRequest.start;
            payRequest.start = payRequest.end;
            payRequest.end = swap;
            start.setText(payRequest.start.getName());
            destination.setText(payRequest.end.getName());

            BillsCardViewContent.setTagColor(activity,
                    startPic, SubwayLineUtil.getColor(payRequest.start.getLine()),
                    destinationPic, SubwayLineUtil.getColor(payRequest.end.getLine()));
        }
    }
}
