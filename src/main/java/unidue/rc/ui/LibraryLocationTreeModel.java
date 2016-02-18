package unidue.rc.ui;

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

import org.apache.tapestry5.tree.TreeModelAdapter;
import unidue.rc.model.LibraryLocation;

import java.util.List;

/**
 * @author Nils Verheyen
 * @since 29.11.13 13:47
 */
public class LibraryLocationTreeModel implements TreeModelAdapter<LibraryLocation> {

    @Override
    public boolean isLeaf(LibraryLocation value) {
        return !value.hasChildren();
    }

    @Override
    public boolean hasChildren(LibraryLocation value) {
        return value.hasChildren();
    }

    @Override
    public List<LibraryLocation> getChildren(LibraryLocation value) {
        return value.getChildLocations();
    }

    @Override
    public String getLabel(LibraryLocation value) {
        return value.getName();
    }
}
