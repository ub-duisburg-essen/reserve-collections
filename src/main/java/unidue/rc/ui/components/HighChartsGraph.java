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
package unidue.rc.ui.components;


import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import unidue.rc.model.stats.HighChartsGraphDataSource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by marcus.koesters on 13.07.15.
 */
@Import(library = {
        "context:vendor/highcharts/highcharts.js","context:vendor/highcharts/exporting.js"
})
public class HighChartsGraph {

    @Inject
    private ComponentResources resources;

    @Inject
    private Logger log;


    @Environmental
    private JavaScriptSupport javaScriptSupport;


    @Parameter(required = true)
    private List<HighChartsGraphDataSource> datasources;

    @Parameter
    private String title;

    @Parameter
    private String subtitle;

    @Parameter
    private String xCategories;

    @Parameter
    private String yCategories;

    @Parameter
    private String yTitle;

    @Parameter
    private String xTitle;

    @Parameter
    private String xZoom;

    @Parameter
    private String xtype;

    @Parameter
    private String layout;

    @BeginRender
    void init() {


        String series = StringUtils.join(datasources, ",");
        VelocityContext context = new VelocityContext();
        String templateFile = "/vt/highcharts.graph.vm";
        context.put("layout",layout);
        context.put("yTitle", yTitle);
        context.put("xTitle", xTitle);
        context.put("xCategories", xCategories);
        context.put("yCategories", yCategories);
        context.put("title", title);
        context.put("subtitle", subtitle);
        context.put("series", series);
        context.put("xtype", xtype);
        context.put("xZoom", xZoom);
        StringWriter writer = new StringWriter();

        // create template
        Template template = Velocity.getTemplate(templateFile, "UTF-8");
        template.merge(context, writer);
        javaScriptSupport.addScript(writer.toString());
        try {
            writer.close();
        } catch (IOException e) {
            log.error("Unable to close StringWriter " +e);
        }

    }
}
