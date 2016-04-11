/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
