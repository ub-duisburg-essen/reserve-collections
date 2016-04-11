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
package unidue.rc.model.stats;


import java.util.List;

/**
 * Created by marcus.koesters on 30.07.15. interface for using datasources / wrapper classes with
 * HighChartsGraph-Component
 */
public interface HighChartsGraphDataSource {

    /**
     * Returns a {@link String} of values with the following pattern: "name: 'name of graph', data :[value1, value2,
     * value3...]" which is used to compute the High Charts Graph
     *
     * @return json graph values
     */
    String getGraphValues();

    /**
     * Returns the {@link String} name / legend of the datasource to be displayed.
     *
     * @return legend for the graph
     */
    String getLegend();

    /**
     * Sets the {@link String} with target id if one exists, <code>null</code> otherwise.
     *
     * @param legend name of the datasource / legend which will be displayed in the High Charts Graph
     */
    void setLegend(String legend);

    /**
     * Returns the {@link List} with Categories for the X-Axis
     *
     * @return see description
     */
    List<String> getXCats();

    /**
     * Returns the {@link List} with Categories for the Y-Axis
     *
     * @return see description
     */
    List<String> getYCats();

    /**
     * Sets the {@link boolean} visibility (Whether if it is displayed as active or disabled) status of the Highcharts
     * Graph of this Datasource.
     *
     * @param visible <code>true</code> if the graph should be visible
     */
    void setIsVisible(boolean visible);

    /**
     * Returns a {@link boolean} which indicates whether or not the datasource will be activated/highlighted within the
     * High Charts Graph Window
     *
     * @return a {@link boolean}
     */
    boolean isVisible();

}