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
package unidue.rc.ui.components;


import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.ioc.annotations.Inject;
import unidue.rc.dao.EntryDAO;
import unidue.rc.model.Headline;
import unidue.rc.model.IntPrimaryKey;
import unidue.rc.model.ReserveCollection;
import unidue.rc.ui.selectmodel.HeadlineSelectModel;
import unidue.rc.ui.valueencoder.BaseValueEncoder;

/**
 * @author Nils Verheyen
 * @since 07.01.14 14:24
 */
public class ArrangeToHeadlineFormGroup {

    @Parameter(required = true, allowNull = false)
    private ReserveCollection collection;

    @Parameter(required = true, name = "value", autoconnect = true)
    @Property
    private Headline headline;

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO dao;

    public ValueEncoder<IntPrimaryKey> getEntryEncoder() {
        return new BaseValueEncoder(Headline.class, dao);
    }

    public SelectModel getHeadlineSelectModel() {
        return new HeadlineSelectModel(collection);
    }
}
