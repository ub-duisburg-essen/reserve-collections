package unidue.rc.plugins.moodle;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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

import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.plugins.moodle.model.Collection;
import unidue.rc.plugins.moodle.model.CollectionList;
import unidue.rc.plugins.moodle.model.File;
import unidue.rc.workflow.ResourceService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nils on 15.06.15.
 */
public class CollectionMapper {

    public static CollectionList map(ResourceDAO resourceDAO, List<ReserveCollection> collections) {
        CollectionList result = new CollectionList();
        result.setCollections(collections.stream()
                .map(collection -> map(collection, resourceDAO))
                .collect(Collectors.toList()));
        return result;
    }

    public static File map(Resource resource) {
        File f = new File();
        f.setExtension(resource.getExtension());
        f.setName(resource.getFileName());
        f.setMimeType(resource.getMimeType());
        f.setId(resource.getId());
        return f;
    }

    private static Collection map(ReserveCollection collection, ResourceDAO resourceDAO) {
        Collection map = new Collection();
        map.setTitle(collection.getTitle());
        map.setId(collection.getId());
        map.setFiles(resourceDAO.getResourcesByCollection(collection)
                .stream()
                .filter(resource -> resource.getFileName() != null)
                .map(resource -> {
                    File file = new File();
                    file.setId(resource.getId());
                    file.setMimeType(resource.getMimeType());
                    file.setName(resource.getFileName());
                    file.setExtension(resource.getExtension());
                    return file;
                }).collect(Collectors.toList()));
        return map;
    }
}
