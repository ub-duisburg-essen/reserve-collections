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


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by nils on 13.07.15.
 */
public abstract class BaseCronJob implements Job {

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {

        CayenneService contextBuilder = (CayenneService) context.getMergedJobDataMap().get(CayenneService.CONTEXT_BUILDER);
        if (contextBuilder == null)
            throw new JobExecutionException("no database context available");

        contextBuilder.createContext();

        run(context);
    }

    protected abstract void run(JobExecutionContext context) throws JobExecutionException;
}
