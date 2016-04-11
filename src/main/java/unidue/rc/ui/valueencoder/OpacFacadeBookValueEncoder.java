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
import unidue.rc.model.OpacFacadeBook;
import unidue.rc.system.OpacFacadeService;

/**
 * @author Nils Verheyen
 * @since 20.09.13 15:05
 */
public class OpacFacadeBookValueEncoder implements ValueEncoder<OpacFacadeBook>, ValueEncoderFactory<OpacFacadeBook> {

    private final OpacFacadeService service;

    public OpacFacadeBookValueEncoder(OpacFacadeService service) {
        this.service = service;
    }

    @Override
    public String toClient(OpacFacadeBook value) {
        return value.getDocNumber();
    }

    @Override
    public OpacFacadeBook toValue(String clientValue) {
        return service.getDetails(clientValue);
    }

    @Override
    public ValueEncoder<OpacFacadeBook> create(Class<OpacFacadeBook> type) {
        return this;
    }
}
