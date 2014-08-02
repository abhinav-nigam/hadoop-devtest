____________________________________________________________
About Project:
____________________________________________________________
Project Type: Crawler/Robot
Technology: Java
Target Marketplace: eBay.com
Strategy Used:  eBay Sitemap Crawl
____________________________________________________________
Setup Instructions:
____________________________________________________________
Basic Requirements-
Java- 1.6
Postgres Sql - 9.2
Other Dependency - Libraries mentioned in pom.xml

____________________________________________________________
Setup steps:
____________________________________________________________
1). Install above components
  - Postgres installation:
    Create new file /etc/apt/sources.list.d/pgdg.list and insert the following line
        deb http://apt.postgresql.org/pub/repos/apt/ <your-ubuntu-release>-pgdg main
    You can know your ubuntu release using lsb_release -c
    Run the following commands:
        wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
        sudo apt-get update
        sudo apt-get install postgresql-9.2 pgadmin3
2). Run the scripts from db folder
    psql -f ebay_sitemap_crawler.sql

How To RUN:
1). Open the program only input- common-configuration.yml
Path : <Local Directory>\DataCrawler\common-configuration.yml
2). Give the parh to your local directory that you want to use as processing directory

3). Open File - <Local Directory>\DataCrawler\src\main\java\com\talentica\platform\eBay\robotstxt\config\robotstxt-configuration.yml
4). Modify database url-
    dbUrl: jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>?user=<USERNAME>&password=<PASSWORD>
    EG.: dbUrl: jdbc:postgresql://localhost:5432/crawler_db?user=postgres&password=postgres

5). Run the program Main.Java file
    Path : <Local Directory>\DataCrawler\src\main\java\com\talentica\platform\Main.java
    Give input mentioned in step (1)
_____________________________________________________________





