package unidue.rc.model;

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

/**
 * Created by nils on 25.06.15.
 */
public enum DefaultLocation {

    ONLINE("Online", false, 0),
    ESSEN("Essen", true, 10),
    DUISBURG("Duisburg", true, 11),
    GW_GSW(ESSEN, "GW/GSW", true, 1),
    MNT(ESSEN, "MNT", true, 3),
    MEDIZIN(ESSEN, "Medizin", true, 5),
    LK(DUISBURG, "LK", true, 6),
    BA(DUISBURG, "BA", true, 7),
    MC(DUISBURG, "MC", true, 8),
    ;

    private DefaultLocation parent;
    private String name;
    private boolean isPhysical;
    private int id;

    DefaultLocation(String name, boolean isPhysical, int id) {
        this.name = name;
        this.isPhysical = isPhysical;
        this.id = id;
    }

    DefaultLocation(DefaultLocation parent, String name, boolean isPhysical, int id) {
        this.parent = parent;
        this.name = name;
        this.isPhysical = isPhysical;
        this.id = id;
    }

    public DefaultLocation getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public boolean isPhysical() {
        return isPhysical;
    }

    public int getId() {
        return id;
    }
}
