package unidue.rc.ui.selectmodel;


import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.util.AbstractSelectModel;
import unidue.rc.model.Headline;
import unidue.rc.model.ReserveCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>HeadlineSelectModel</code> can be used as a {@link org.apache.tapestry5.SelectModel} that simply
 * returns {@link unidue.rc.model.Headline} objects as {@link org.apache.tapestry5.OptionModel}s.
 *
 * @author Nils Verheyen
 * @since 02.12.13 09:21
 */
public class HeadlineSelectModel extends AbstractSelectModel {

    private final ReserveCollection collection;

    public HeadlineSelectModel(ReserveCollection collection) {
        this.collection = collection;
    }

    @Override
    public List<OptionGroupModel> getOptionGroups() {
        return null;
    }

    @Override
    public List<OptionModel> getOptions() {
        List<Headline> headlines = collection.getHeadlines();
        List<OptionModel> result = new ArrayList<>();
        for (Headline headline : headlines) {
            result.add(new OptionModelImpl(headline.getText(), headline));
        }
        return result;
    }
}
