CREATE TABLE ROLE (id integer NOT NULL, isDefault smallint NULL, name varchar(256) NULL, PRIMARY KEY (id))
;

CREATE TABLE RESERVE_COLLECTION_NUMBER (number integer NOT NULL, PRIMARY KEY (number))
;

CREATE TABLE JOB_COMMENT (authorId integer NOT NULL, comment varchar(2048) NULL, date timestamp with time zone NULL, id integer NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE SCAN_JOB_COMMENT (jobCommentId integer NOT NULL, scanJobId integer NOT NULL, PRIMARY KEY (jobCommentId, scanJobId))
;

CREATE TABLE ACTION (attribute varchar(256) NULL, id integer NOT NULL, name varchar(256) NULL, resource varchar(256) NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE SETTING (format varchar(256) NULL, key varchar(1024) NOT NULL, label varchar(512) NULL, type integer NULL, value varchar(2048) NULL, PRIMARY KEY (key))
;

CREATE TABLE LIBRARY_LOCATION (deleted boolean NULL, id integer NOT NULL, name varchar(512) NULL, parentId integer NULL, physical boolean NULL, PRIMARY KEY (id))
;

CREATE TABLE RESOURCE (copyrightReviewStatus integer NULL, filePath varchar(512) NULL, fullTextURL varchar(1024) NULL, id integer NOT NULL, mimeType varchar(128) NULL, PRIMARY KEY (id))
;

CREATE TABLE MAIL (fromAddress varchar(512) NULL, id integer NOT NULL, mailBody varchar(4096) NULL, numTries integer NULL, send boolean NULL, subject varchar(1024) NULL, PRIMARY KEY (id))
;

CREATE TABLE RESERVE_COLLECTION (alephSystemId varchar(64) NULL, alephUserId varchar(64) NULL, authorId integer NULL, comment varchar(512) NULL, doWarning boolean NULL, id integer NOT NULL, libraryLocationId integer NOT NULL, onlineOnly boolean NULL, originId integer NULL, permalink varchar(1024) NULL, piwikId integer NULL, rcNumberId integer NOT NULL, readKey varchar(256) NULL, status integer NOT NULL, title varchar(512) NOT NULL, validTo timestamp with time zone NULL, writeKey varchar(256) NULL, PRIMARY KEY (id))
;

CREATE TABLE MEMBERSHIP (roleId integer NOT NULL, userId integer NOT NULL, PRIMARY KEY (roleId, userId))
;

CREATE TABLE PERMISSION (actionID integer NOT NULL, id integer NOT NULL, instanceID integer NULL, userID integer NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE FILE_ACCESS (accessDate timestamp with time zone NULL, id bigint NOT NULL, realmhash varchar(64) NULL, resourceID integer NULL, userhash varchar(64) NULL, PRIMARY KEY (id))
;

CREATE TABLE PERMISSION_DEFINITION (actionID integer NULL, id integer NOT NULL, isInstanceBound boolean NULL, roleID integer NULL, PRIMARY KEY (id))
;

CREATE TABLE ENTRY (created timestamp with time zone NULL, deleted boolean NULL, id integer NOT NULL, modified timestamp with time zone NULL, position integer NULL, reserveCollectionId integer NULL, PRIMARY KEY (id))
;

CREATE TABLE NUMBER_RESERVATION (locationId integer NOT NULL, numberId integer NOT NULL, userId integer NOT NULL, PRIMARY KEY (locationId, numberId, userId))
;

CREATE TABLE HEADLINE (id integer NOT NULL, text varchar(512) NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE HTML (id integer NOT NULL, text text NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE PARTICIPATION (endDate timestamp with time zone NULL, id integer NOT NULL, reserveCollectionId integer NOT NULL, roleId integer NOT NULL, startDate timestamp with time zone NULL, userId integer NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE SCAN_JOB (id integer NOT NULL, locationId integer NULL, modified timestamp with time zone NULL, status integer NULL, PRIMARY KEY (id))
;

CREATE TABLE BOOK (authors varchar(2048) NULL, comment varchar(2048) NULL, edition varchar(256) NULL, id integer NOT NULL, isbn varchar(256) NULL, placeOfPublication varchar(512) NULL, publisher varchar(512) NULL, resourceType varchar(256) NULL, signature varchar(64) NULL, thumbnailURL varchar(256) NULL, title varchar(1024) NOT NULL, volume varchar(256) NULL, yearOfPublication integer NULL, PRIMARY KEY (id))
;

CREATE TABLE REFERENCE (authors varchar(2048) NULL, comment varchar(2048) NULL, edition varchar(256) NULL, fullTextURL varchar(1024) NULL, id integer NOT NULL, isbn varchar(24) NULL, placeOfPublication varchar(512) NOT NULL, publisher varchar(512) NULL, thumbnailURL varchar(256) NULL, title varchar(1024) NOT NULL, volume varchar(256) NULL, yearOfPublication integer NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE FILE (description varchar(1024) NULL, id integer NOT NULL, resourceID integer NULL, PRIMARY KEY (id))
;

CREATE TABLE MAIL_NODE (id integer NOT NULL, mailID integer NULL, type integer NULL, value varchar(256) NULL, PRIMARY KEY (id))
;

CREATE TABLE WEB_LINK (id integer NOT NULL, name varchar(512) NULL, url varchar(1024) NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE BOOK_CHAPTER (bookAuthors varchar(2048) NULL, bookTitle varchar(1024) NOT NULL, chapterAuthors varchar(2048) NULL, chapterTitle varchar(1024) NOT NULL, comment varchar(2048) NULL, edition varchar(512) NULL, editor varchar(512) NULL, id integer NOT NULL, isbn varchar(24) NULL, pageEnd varchar(5) NOT NULL, pageStart varchar(5) NOT NULL, placeOfPublication varchar(512) NULL, publisher varchar(512) NULL, referenceNumber varchar(128) NULL, resourceID integer NULL, signature varchar(64) NULL, yearOfPublication integer NULL, PRIMARY KEY (id))
;

CREATE TABLE DOCUMENT_LINK (comment varchar(1024) NULL, documentId integer NOT NULL, id integer NOT NULL, PRIMARY KEY (id))
;

CREATE TABLE BOOK_JOB (id integer NOT NULL, modified timestamp with time zone NULL, status integer NULL, PRIMARY KEY (id))
;

CREATE TABLE SCAN_JOB_BOOK_CHAPTER (bookChapterID integer NOT NULL, scanJobID integer NOT NULL, PRIMARY KEY (bookChapterID, scanJobID))
;

CREATE TABLE BOOK_JOB_COMMENT (bookJobId integer NOT NULL, jobCommentId integer NOT NULL, PRIMARY KEY (bookJobId, jobCommentId))
;

CREATE TABLE JOURNAL_ARTICLE (articleTitle varchar(1024) NOT NULL, authors varchar(2048) NULL, comment varchar(2048) NULL, id integer NOT NULL, issn varchar(64) NULL, issue varchar(512) NULL, journalTitle varchar(1024) NOT NULL, pageEnd varchar(5) NOT NULL, pageStart varchar(5) NOT NULL, placeOfPublication varchar(512) NULL, publisher varchar(512) NULL, referenceNumber varchar(128) NULL, resourceID integer NULL, signature varchar(64) NULL, volume varchar(256) NULL, PRIMARY KEY (id))
;

CREATE TABLE SCAN_JOB_JOURNAL_ARTICLE (journalArticleID integer NOT NULL, scanJobID integer NOT NULL, PRIMARY KEY (journalArticleID, scanJobID))
;

ALTER TABLE SCAN_JOB_COMMENT ADD FOREIGN KEY (jobCommentId) REFERENCES JOB_COMMENT (id)
;

ALTER TABLE LIBRARY_LOCATION ADD FOREIGN KEY (parentId) REFERENCES LIBRARY_LOCATION (id)
;

ALTER TABLE RESERVE_COLLECTION ADD FOREIGN KEY (libraryLocationId) REFERENCES LIBRARY_LOCATION (id)
;

ALTER TABLE RESERVE_COLLECTION ADD FOREIGN KEY (rcNumberId) REFERENCES RESERVE_COLLECTION_NUMBER (number)
;

ALTER TABLE MEMBERSHIP ADD FOREIGN KEY (roleId) REFERENCES ROLE (id)
;

ALTER TABLE PERMISSION ADD FOREIGN KEY (actionID) REFERENCES ACTION (id)
;

ALTER TABLE FILE_ACCESS ADD FOREIGN KEY (resourceID) REFERENCES RESOURCE (id)
;

ALTER TABLE PERMISSION_DEFINITION ADD FOREIGN KEY (actionID) REFERENCES ACTION (id)
;

ALTER TABLE PERMISSION_DEFINITION ADD FOREIGN KEY (roleID) REFERENCES ROLE (id)
;

ALTER TABLE ENTRY ADD FOREIGN KEY (reserveCollectionId) REFERENCES RESERVE_COLLECTION (id)
;

ALTER TABLE NUMBER_RESERVATION ADD FOREIGN KEY (locationId) REFERENCES LIBRARY_LOCATION (id)
;

ALTER TABLE NUMBER_RESERVATION ADD FOREIGN KEY (numberId) REFERENCES RESERVE_COLLECTION_NUMBER (number)
;

ALTER TABLE HEADLINE ADD FOREIGN KEY (id) REFERENCES ENTRY (id)
;

ALTER TABLE HTML ADD FOREIGN KEY (id) REFERENCES ENTRY (id)
;

ALTER TABLE PARTICIPATION ADD FOREIGN KEY (reserveCollectionId) REFERENCES RESERVE_COLLECTION (id)
;

ALTER TABLE PARTICIPATION ADD FOREIGN KEY (roleId) REFERENCES ROLE (id)
;

ALTER TABLE SCAN_JOB ADD FOREIGN KEY (locationId) REFERENCES LIBRARY_LOCATION (id)
;

ALTER TABLE BOOK ADD FOREIGN KEY (id) REFERENCES ENTRY (id)
;

ALTER TABLE REFERENCE ADD FOREIGN KEY (id) REFERENCES ENTRY (id)
;

ALTER TABLE FILE ADD FOREIGN KEY (id) REFERENCES ENTRY (id)
;

ALTER TABLE FILE ADD UNIQUE (resourceID)
;

ALTER TABLE FILE ADD FOREIGN KEY (resourceID) REFERENCES RESOURCE (id)
;

ALTER TABLE MAIL_NODE ADD FOREIGN KEY (mailID) REFERENCES MAIL (id)
;

ALTER TABLE WEB_LINK ADD FOREIGN KEY (id) REFERENCES ENTRY (id)
;

ALTER TABLE BOOK_CHAPTER ADD FOREIGN KEY (id) REFERENCES ENTRY (id)
;

ALTER TABLE BOOK_CHAPTER ADD UNIQUE (resourceID)
;

ALTER TABLE BOOK_CHAPTER ADD FOREIGN KEY (resourceID) REFERENCES RESOURCE (id)
;

ALTER TABLE DOCUMENT_LINK ADD FOREIGN KEY (id) REFERENCES ENTRY (id)
;

ALTER TABLE BOOK_JOB ADD FOREIGN KEY (id) REFERENCES BOOK (id)
;

ALTER TABLE SCAN_JOB_BOOK_CHAPTER ADD FOREIGN KEY (bookChapterID) REFERENCES BOOK_CHAPTER (id)
;

ALTER TABLE SCAN_JOB_BOOK_CHAPTER ADD FOREIGN KEY (scanJobID) REFERENCES SCAN_JOB (id)
;

ALTER TABLE BOOK_JOB_COMMENT ADD FOREIGN KEY (bookJobId) REFERENCES BOOK_JOB (id)
;

ALTER TABLE BOOK_JOB_COMMENT ADD FOREIGN KEY (jobCommentId) REFERENCES JOB_COMMENT (id)
;

ALTER TABLE JOURNAL_ARTICLE ADD FOREIGN KEY (id) REFERENCES ENTRY (id)
;

ALTER TABLE JOURNAL_ARTICLE ADD UNIQUE (resourceID)
;

ALTER TABLE JOURNAL_ARTICLE ADD FOREIGN KEY (resourceID) REFERENCES RESOURCE (id)
;

ALTER TABLE SCAN_JOB_JOURNAL_ARTICLE ADD FOREIGN KEY (journalArticleID) REFERENCES JOURNAL_ARTICLE (id)
;

ALTER TABLE SCAN_JOB_JOURNAL_ARTICLE ADD FOREIGN KEY (scanJobID) REFERENCES SCAN_JOB (id)
;

CREATE SEQUENCE pk_action INCREMENT 1 START 200
;

CREATE SEQUENCE pk_entry INCREMENT 1 START 200
;

CREATE SEQUENCE pk_fileaccess INCREMENT 1 START 200
;

CREATE SEQUENCE pk_job_comment INCREMENT 1 START 200
;

CREATE SEQUENCE pk_library_location INCREMENT 1 START 200
;

CREATE SEQUENCE pk_mail INCREMENT 1 START 200
;

CREATE SEQUENCE pk_mail_node INCREMENT 1 START 200
;

CREATE SEQUENCE pk_membership INCREMENT 20 START 200
;

CREATE SEQUENCE pk_number_reservation INCREMENT 20 START 200
;

CREATE SEQUENCE pk_participation INCREMENT 1 START 200
;

CREATE SEQUENCE pk_permission INCREMENT 1 START 200
;

CREATE SEQUENCE pk_permission_definition INCREMENT 1 START 200
;

CREATE SEQUENCE pk_reserve_collection INCREMENT 1 START 200
;

CREATE SEQUENCE pk_reserve_collection_number INCREMENT 1 START 200
;

CREATE SEQUENCE pk_resource INCREMENT 1 START 200
;

CREATE SEQUENCE pk_role INCREMENT 1 START 200
;

CREATE SEQUENCE pk_scan_job INCREMENT 1 START 200
;

CREATE SEQUENCE pk_scan_job_comment INCREMENT 20 START 200
;

CREATE SEQUENCE pk_setting INCREMENT 20 START 200
;

