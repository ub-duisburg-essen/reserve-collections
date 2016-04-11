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


import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.LibraryLocation;

public class LibraryLocationValueEncoder implements ValueEncoder<LibraryLocation>, ValueEncoderFactory<LibraryLocation> {

    private LibraryLocationDAO dao;

    public LibraryLocationValueEncoder(LibraryLocationDAO dao) {
        this.dao = dao;
    }

    @Override
    public String toClient(LibraryLocation value) {
        return value.getId().toString();
    }

    @Override
    public LibraryLocation toValue(String clientValue) {
        return dao.getLocationById(Integer.valueOf(clientValue));
    }

    @Override
    public ValueEncoder<LibraryLocation> create(Class<LibraryLocation> type) {
        return this;
    }
}
