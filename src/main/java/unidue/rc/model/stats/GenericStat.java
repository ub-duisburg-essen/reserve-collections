package unidue.rc.model.stats;


/**
 * Created by marcus.koesters on 04.08.15.
 */
public class GenericStat {

    private String date;

    private Integer count;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        String[] dateParts = date.split("-");

        int month = Integer.valueOf(dateParts[1]) - 1;
        if (count == null) count = 0;
        return "[Date.UTC(" + dateParts[0] + "," + month + "," + dateParts[2].substring(0, 2) + ")," + count + " ]";

    }

}
