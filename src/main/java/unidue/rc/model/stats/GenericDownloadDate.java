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


/**
 * Created by marcus.koesters on 25.09.15.
 */
public class GenericDownloadDate implements DownloadDate {
   private String date;

    private Integer hitsum = 0;

    private Integer visitsum = 0;

    @Override
    public String getDate() {
    return date;
    }

    @Override
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public Integer getHitSum() {
        return hitsum;
    }

    @Override
    public Integer getVisitSum() {
        return visitsum;
    }

    public void setHitSum(int hitsum)  {
        this.hitsum = hitsum;
    }

    public void setVisitsum(int visitsum) {
        this.visitsum = visitsum;
    }

}
