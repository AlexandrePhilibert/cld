# Database migration

In this task you will migrate the Drupal database to the new RDS database instance.

![Schema](./img/CLD_AWS_INFA.PNG)

## Task 01 - Securing current Drupal data

### [Get Bitnami MariaDb user's password](https://docs.bitnami.com/aws/faq/get-started/find-credentials/)

```bash
[INPUT]
cat /home/bitnami/bitnami_credentials | grep "default username and password is"

[OUTPUT]
The default username and password is 'user' and '<redacted>'.
```

### Get Database Name of Drupal

```bash
[INPUT]
mariadb -u root -p -e 'SHOW DATABASES'

[OUTPUT]
+--------------------+
| Database           |
+--------------------+
| bitnami_drupal     |
| information_schema |
| mysql              |
| performance_schema |
| sys                |
| test               |
+--------------------+
```

### [Dump Drupal DataBases](https://mariadb.com/kb/en/mariadb-dump/)

```bash
[INPUT]
mariadb-dump -u root -p -r dump.sql bitnami_drupal

[OUTPUT]
None.

You can check that the dump was performed correctly by running:

[INPUT]
tail dump.sql

[OUTPUT]
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-03-14 15:55:31
```

### Create the new Data base on RDS

```sql
[INPUT]
CREATE DATABASE bitnami_drupal;
```

### [Import dump in RDS db-instance](https://mariadb.com/kb/en/restoring-data-from-dump-files/)

Note : you can do this from the Drupal Instance. Do not forget to set the "-h" parameter.

```bash
[INPUT]
mariadb --user admin --host dbi-devopsteam01.cshki92s4w5p.eu-west-3.rds.amazonaws.com --password bitnami_drupal < dump.sql

[OUTPUT]
None

You can check that the database was correctly imported by runnig:

[INPUT]
mariadb --user admin --host dbi-devopsteam01.cshki92s4w5p.eu-west-3.rds.amazonaws.com --password bitnami_drupal -e 'SHOW TABLES'

[OUTPUT]
+----------------------------------+
| Tables_in_bitnami_drupal         |
+----------------------------------+
| block_content                    |
| block_content__body              |
| block_content_field_data         |
| block_content_field_revision     |
| block_content_revision           |
| block_content_revision__body     |
| cache_bootstrap                  |
| cache_config                     |
| cache_container                  |
...
```

### [Get the current Drupal connection string parameters](https://www.drupal.org/docs/8/api/database-api/database-configuration)

```bash
[INPUT]
tail -n 15 /opt/bitnami/drupal/sites/default/settings.php

[OUTPUT]
#   include $app_root . '/' . $site_path . '/settings.local.php';
# }
$databases['default']['default'] = array (
  'database' => 'bitnami_drupal',
  'username' => 'bn_drupal',
  'password' => '<redacted>',
  'prefix' => '',
  'host' => '127.0.0.1',
  'port' => '3306',
  'isolation_level' => 'READ COMMITTED',
  'driver' => 'mysql',
  'namespace' => 'Drupal\\mysql\\Driver\\Database\\mysql',
  'autoload' => 'core/modules/mysql/src/Driver/Database/mysql/',
);
$settings['config_sync_directory'] = 'sites/default/files/config_9Ixp6MXaD825etoZ93DHou9SzqdalUcXiZoAjf_zRmlYPttj68Hw4q7loOaMOOV6kfGpBVYcpw/sync';

```

### Replace the current host with the RDS FQDN

```
//settings.php

$databases['default']['default'] = array (
   [...]
  'host' => 'dbi-devopsteam01.cshki92s4w5p.eu-west-3.rds.amazonaws.com',
   [...]
);
```

### [Create the Drupal Users on RDS Data base](https://mariadb.com/kb/en/create-user/)

Note : only calls from both private subnets must be approved.

- [By Password](https://mariadb.com/kb/en/create-user/#identified-by-password)
- [Account Name](https://mariadb.com/kb/en/create-user/#account-names)
- [Network Mask](https://cric.grenoble.cnrs.fr/Administrateurs/Outils/CalculMasque/)

```sql
[INPUT]
CREATE USER bn_drupal@'10.0.1.%' IDENTIFIED BY '<redacted>';

GRANT ALL PRIVILEGES ON bitnami_drupal.* TO bn_drupal@'10.0.1.%';

FLUSH PRIVILEGES;
```

```sql
//validation
[INPUT]
SHOW GRANTS for bn_drupal@'10.0.1.%';

[OUTPUT]
+-----------------------------------------------------------------------------------------------------------------+
| Grants for bn_drupal@10.0.1.%                                                                                   |
+-----------------------------------------------------------------------------------------------------------------+
| GRANT USAGE ON *.* TO `bn_drupal`@`10.0.1.%` IDENTIFIED BY PASSWORD '<redacted>' |
| GRANT ALL PRIVILEGES ON `bitnami_drupal`.* TO `bn_drupal`@`10.0.1.%`                                            |
+-----------------------------------------------------------------------------------------------------------------+
```

### Validate access (on the drupal instance)

```sql
[INPUT]
mariadb -h dbi-devopsteam01.cshki92s4w5p.eu-west-3.rds.amazonaws.com -u bn_drupal -p

[INPUT]
SHOW DATABASES;

[OUTPUT]
+--------------------+
| Database           |
+--------------------+
| bitnami_drupal     |
| information_schema |
+--------------------+
2 rows in set (0.001 sec)
```
