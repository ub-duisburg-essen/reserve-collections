package unidue.rc.ui.selectmodel;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import miless.model.MCRCategory;
import miless.model.MCRCategoryLabel;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.util.AbstractSelectModel;
import unidue.rc.dao.OriginDAO;

import java.util.*;
import java.util.stream.Collectors;

public class OriginSelectModel extends AbstractSelectModel {

    private OriginDAO dao;

    private Locale locale;


    public OriginSelectModel(OriginDAO dao, Locale locale) {
        this.dao = dao;
        this.locale = locale;
    }

    @Override
    public List<OptionGroupModel> getOptionGroups() {
        return null;
    }

    @Override
    public List<OptionModel> getOptions() {

        // get all origins
        List<MCRCategory> origins = dao.getOrigins();
        List<OptionModel> result = new ArrayList<OptionModel>();

        for (MCRCategory c : origins) {

            // labels contain text to display inside the client
            List<MCRCategoryLabel> labels = c.getLabels();

            // try to find label according to current locale
            Optional<MCRCategoryLabel> labelOptional = labels.stream()
                    .filter(label -> new Locale(label.getLang()).getLanguage().equals(locale.getLanguage()))
                    .findAny();

            // get label according to current locale, otherwise get default or null if no label is present
            MCRCategoryLabel label = labelOptional.isPresent()
                    ? labelOptional.get()
                    : c.getLabels().size() > 0
                            ? c.getLabels().get(0)
                            : null;

            if (label != null)
                result.add(new OptionModelImpl(label.getText(), c));
        }
        return result.stream()
                .sorted((o1, o2) -> o1.getLabel().compareTo(o2.getLabel()))
                .collect(Collectors.toList());
    }
}
