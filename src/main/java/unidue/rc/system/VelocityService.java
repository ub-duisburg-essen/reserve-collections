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
package unidue.rc.system;

import miless.model.MCRCategory;
import miless.model.User;
import org.apache.cayenne.di.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import unidue.rc.dao.*;
import unidue.rc.model.DefaultRole;
import unidue.rc.model.Participation;
import unidue.rc.model.ReserveCollection;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by nils on 27.06.16.
 */
public class VelocityService implements TemplateService {

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private MCRCategoryDAO categoryDAO;

    @Inject
    private OriginDAO originDAO;

    @Override
    public Builder builder() {
        return new VelocityBuilder();
    }

    @Override
    public String buildAuthors(ReserveCollection collection, String divider) {

        List<Participation> participations = participationDAO.getActiveParticipations(roleDAO.getRole(DefaultRole.DOCENT), collection);
        Object[] docentNames = participations.stream()
                .map(participation -> userDAO.getUserById(participation.getUserId()))
                .filter(user -> user != null)
                .map(user -> user.getRealname())
                .toArray();
        String authors = StringUtils.join(docentNames, divider);
        return authors;
    }

    @Override
    public String buildOrigin(ReserveCollection collection, String divider) {
        Integer originId = collection.getOriginId();
        if (originId != null) {
            MCRCategory category = categoryDAO.getCategoryById(originId);
            return buildOrigin(category, divider);
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String buildOrigin(User user, String divider) {
        String originId = user.getOrigin();
        if (originId != null) {
            MCRCategory category = originDAO.getOrigin(originId);
            return buildOrigin(category, divider);
        }
        return StringUtils.EMPTY;
    }

    private String buildOrigin(MCRCategory category, String divider) {

        List<MCRCategory> categories = new ArrayList<>();
        // add every category except the root
        do {
            categories.add(0, category);
            category = category.getParentCategory();
        } while (category != null && category.getParentCategory() != null);
        Object[] categoryLabels = categories.stream()
                .map(c -> originDAO.getOriginLabel(Locale.GERMAN, c.getInternalId()))
                .toArray();
        String origin = StringUtils.join(categoryLabels, divider);
        return origin;
    }

    class VelocityBuilder implements Builder {

        private VelocityContext context;

        public VelocityBuilder() {
            this.context = new VelocityContext();
            this.context.put("DateFormatter", new SimpleDateFormat("dd. MMMM yyyy "));
        }

        @Override
        public VelocityBuilder put(String key, Object value) {
            context.put(key, value);
            return this;
        }

        @Override
        public String build(String templateFile) throws IOException {
            StringWriter writer = new StringWriter();
            Template template = Velocity.getTemplate(templateFile, "UTF-8");
            template.merge(context, writer);
            String result = writer.toString();
            writer.close();
            return result;
        }
    }
}
