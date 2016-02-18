package unidue.rc.model.stats;

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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by marcus.koesters on 05.08.15.
 */
@Root
public class SemAppStatistics {

    @ElementList(name = "run", inline = true)
    private List<SemAppStatistic> semAppStatistics;

    public List<SemAppStatistic> getSemAppStatistics() {
        return semAppStatistics;
    }

    public void setSemAppStatistics(List<SemAppStatistic> semAppStatistics) {
        this.semAppStatistics = semAppStatistics;
    }


}
