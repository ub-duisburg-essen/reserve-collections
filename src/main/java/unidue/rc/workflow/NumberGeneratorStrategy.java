package unidue.rc.workflow;

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

import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollectionNumber;

/**
 * A <code>NumberGeneratorStrategy</code> can be used to generate {@linkplain ReserveCollectionNumber} objects that will
 * be used in reserve collections.
 *
 * @author Nils Verheyen
 * @see InsertNumberGenerator
 * @see AppendNumberGenerator
 */
public interface NumberGeneratorStrategy {

    ReserveCollectionNumber buildNumber(LibraryLocation location);
}
