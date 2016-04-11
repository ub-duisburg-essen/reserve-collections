package unidue.rc.model.stats;


/**
 * Created by marcus.koesters on 25.09.15.
 */
public class GenericDownloadDate implements DownloadDate {
   private String date;

    private Integer hitsum = 0;

    private Integer visitsum = 0;

    @Override
    public String getDate() {
    return date;
    }

    @Override
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public Integer getHitSum() {
        return hitsum;
    }

    @Override
    public Integer getVisitSum() {
        return visitsum;
    }

    public void setHitSum(int hitsum)  {
        this.hitsum = hitsum;
    }

    public void setVisitsum(int visitsum) {
        this.visitsum = visitsum;
    }

}
