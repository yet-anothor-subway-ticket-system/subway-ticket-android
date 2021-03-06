package cn.crepusculo.subway_ticket_android.util;

import android.content.Context;
import android.graphics.PorterDuff;
import android.widget.ImageView;

import cn.crepusculo.subway_ticket_android.R;
import cn.crepusculo.subway_ticket_android.content.TicketOrder;


public class SubwayLineUtil {
    public final static int ONE = 1;
    public final static int TWO = 2;
    public final static int FOUR = 4;
    public final static int FIVE = 5;
    public final static int SIX = 6;
    public final static int SEVEN = 7;
    public final static int EIGHT = 8;
    public final static int NINE = 9;
    public final static int TEN = 10;
    public final static int THIRTEEN = 13;
    public final static int FOURTEEN = 14;
    public final static int FIFTEEN = 15;
    public final static int SIXTEEN = 16;
    public final static int BATONG = 17;
    public final static int CHANGPING = 18;
    public final static int DAXING = 19;
    public final static int FANGSHAN = 20;
    public final static int MENGOUTOU = 21;
    public final static int YANFANG = 22;
    public final static int YIZHUANG = 23;
    public final static int XIJIAO = 24;
    public final static int AIRPORT = 25;

    private SubwayLineUtil() {
    }

    public static int getLine(String station) {

        return 5;
    }

    public static int getColor(int line) {
        switch (line) {
            case ONE:
                return R.color.line_1;
            case TWO:
                return R.color.line_2;
            case FOUR:
                return R.color.line_4;
            case FIVE:
                return R.color.line_5;
            case SIX:
                return R.color.line_6;
            case SEVEN:
                return R.color.line_7;
            case EIGHT:
                return R.color.line_8;
            case NINE:
                return R.color.line_9;
            case TEN:
                return R.color.line_10;
            case THIRTEEN:
                return R.color.line_13;
            case FOURTEEN:
                return R.color.line_14;
            case FIFTEEN:
                return R.color.line_15;
            case SIXTEEN:
                return R.color.line_16;
            case BATONG:
                return R.color.line_ba;
            case CHANGPING:
                return R.color.line_chang;
            case DAXING:
                return R.color.line_da;
            case FANGSHAN:
                return R.color.line_fang;
            case MENGOUTOU:
                return R.color.line_men;
            case YANFANG:
                return R.color.line_yan;
            case YIZHUANG:
                return R.color.line_yi;
            case XIJIAO:
                return R.color.line_xi;
            case AIRPORT:
                return R.color.line_airport;
            default:
                return R.color.undefine;
        }
    }

    /**
     * @param context Context used to get resource
     * @param view    Target drawable view
     * @param line    Line Symbol Color
     */
    public static void setColor(Context context, ImageView view, int line) {
        view.getDrawable().setColorFilter(context.getResources().getColor(SubwayLineUtil.getColor(line)), PorterDuff.Mode.SRC_ATOP);

    }

    public static String ConnectLineNameStr(int line, String str) {
        return line + " " + str;
    }

    /**
     * @param line Client type line id
     * @return Server type line id
     */
    public static int ToServerTypeId(int line) {
        String s = "1" + line;
        return Integer.parseInt(s);
    }

    /**
     * @param line Server type line id
     * @return Client type line id
     */
    public static int ToClientTypeId(int line) {
        String s = "" + line;
        return Integer.parseInt(s.substring(1));
    }

    public static com.subwayticket.database.model.TicketOrder convertToServer(TicketOrder order) {
        com.subwayticket.database.model.TicketOrder result = new com.subwayticket.database.model.TicketOrder();
        result.setAmount(order.getAmount());
        result.setComment(order.getComment());
        result.setEndStation(order.getEndStation());
        result.setExtractAmount(order.getExtractAmount());
        result.setExtractCode(order.getExtractCode());
        result.setStartStation(order.getStartStation());
        result.setStatus(order.getStatus());
        result.setTicketOrderId(order.getTicketOrderId());
        result.setTicketOrderTime(order.getTicketOrderTime());
        result.setTicketPrice(order.getTicketPrice());
        result.setUser(order.getUser());
        return result;
    }
}
