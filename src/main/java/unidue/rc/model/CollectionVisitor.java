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


/**
 * Created by nils on 06.08.15.
 */
public interface CollectionVisitor {

    void visit(ReserveCollection collection);
    void didVisit(ReserveCollection collection);

    void visit(Participation participation);

    void visit(LibraryLocation location);

    void visit(Entry entry);
    void didVisit(Entry entry);

    // simple entries

    void visit(Html html);

    void visit(Headline headline);

    void visit(WebLink webLink);


    // resource container

    void visit(Reference reference);
    void didVisit(Reference reference);

    void visit(File file);
    void didVisit(File file);

    void visit(JournalArticle article);
    void didVisit(JournalArticle article);

    void visit(BookChapter chapter);
    void didVisit(BookChapter chapter);

    void visit(Book book);
    void didVisit(Book book);

    void visit(Resource resource);

    void startList(String fieldName);
    void endList();

}
