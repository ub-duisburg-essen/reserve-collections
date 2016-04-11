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
package unidue.rc.ui.valueencoder;


import miless.model.MCRCategory;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import unidue.rc.dao.MCRCategoryDAO;

public class MCRCategoryValueEncoder implements ValueEncoder<MCRCategory>, ValueEncoderFactory<MCRCategory> {

    private MCRCategoryDAO dao;

    public MCRCategoryValueEncoder(MCRCategoryDAO dao) {
        this.dao = dao;
    }

    @Override
    public ValueEncoder<MCRCategory> create(Class<MCRCategory> type) {
        return this;
    }

    @Override
    public String toClient(MCRCategory value) {
        return value.getInternalId().toString();
    }

    @Override
    public MCRCategory toValue(String internalId) {
        return dao.getCategoryById(Integer.valueOf(internalId));
    }

}
