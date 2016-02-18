*******************
Reserve collections
*******************

The reserve collection software is a content management software that can be used to manage content of courses inside academic institutions. The software is developed by the library of the university in Duisburg-Essen. The goal of this software is to link services provided by the library with a course content management.

Requirements
============

* Java >= 1.8
* PostgreSQL >= 8.4 (Other databases with modification)
* Running version of `reserve collection solr schema <https://github.com/unibib-duisburg-essen/reserve-collections-solr-schema>`_

Installation
============

First of all get yourself a clone of this repository to create your own build.

.. code-block:: shell

    git clone https://github.com/unibib-duisburg-essen/reserve-collections.git

Within the web application three databases are needed. The configuration is located in

``src/main/resources/cayenne-unidue-reserve-collections.xml``

and

``src/main/webapp/WEB-INF/jetty-env.xml``

The databases are used through the object relational mapper `Apache Cayenne <http://cayenne.apache.org>`_. On start of the application the cayenne xml file is read to look for database configuration. Cause of the use of jndi, the file that keeps the real configuration details (jdbc url, username, password, etc.) is ``jetty-env.xml``. When you use tomcat or jboss make sure to provide configuration details that can be used by jndi.

By default there are three databases that are used:

+---------------------+-----------+----------+----------+------+
| database name       | host      | username | password | port |
+=====================+===========+==========+==========+======+
| reserve_collections | localhost | rc       | rc       | 5432 |
+---------------------+-----------+----------+----------+------+
| rc_access_log       | localhost | rc       | rc       | 5432 |
+---------------------+-----------+----------+----------+------+
| miless              | localhost | miless   | miless   | 5432 |
+---------------------+-----------+----------+----------+------+

