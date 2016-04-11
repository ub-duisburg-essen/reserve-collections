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
package unidue.rc.model;


import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nils on 25.06.15.
 */
public enum ScannableOrderAdmin {

    DEFAULT("semapp-scan@ub.uni-duisburg-essen.de",
            DefaultLocation.GW_GSW,
            DefaultLocation.MNT,
            DefaultLocation.BA,
            DefaultLocation.LK,
            DefaultLocation.MC,
            DefaultLocation.ONLINE),
    MEDIZIN("semapp-med@ub.uni-duisburg-essen.de", DefaultLocation.MEDIZIN);

    private final DefaultLocation[] locations;
    private String mail;

    ScannableOrderAdmin(String mail, DefaultLocation... locations) {
        this.locations = locations;
        this.mail = mail;
    }

    public static Set<String> mails(int locationID) {

        return Arrays.stream(values())
                .filter(admin -> Arrays.stream(admin.locations).anyMatch(location -> location.getId() == locationID))
                .map(admin -> admin.mail)
                .collect(Collectors.toSet());
    }
}
