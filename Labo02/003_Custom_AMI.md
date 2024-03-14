# Custom AMI and Deploy the second Drupal instance

In this task you will update your AMI with the Drupal settings and deploy it in the second availability zone.

## Task 01 - Create AMI

### [Create AMI](https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/create-image.html)

Note : stop the instance before

|Key|Value for GUI Only|
|:--|:--|
|Name|AMI_DRUPAL_DEVOPSTEAM[XX]_LABO02_RDS|
|Description|Same as name value|

```bash
[INPUT]
aws ec2 create-image --instance-id i-0890609a4443bb12f --name AMI_DRUPAL_DEVOPSTEAM01_LABO02_RDS
[OUTPUT]
{
    "ImageId": "ami-043b89d6e9ef83673"
}
```

## Task 02 - Deploy Instances

* Restart Drupal Instance in Az1

* Deploy Drupal Instance based on AMI in Az2

|Key|Value for GUI Only|
|:--|:--|
|Name|EC2_PRIVATE_DRUPAL_DEVOPSTEAM[XX]_B|
|Description|Same as name value|

```bash
[INPUT]
aws ec2 run-instances \
 --image-id ami-043b89d6e9ef83673 \
 --count 1 \
 --instance-type t3.micro \
 --key-name CLD_KEY_DRUPAL_DEVOPSTEAM01 \
 --private-ip-address 10.0.1.140 \
 --security-group-ids sg-0c82cccdd3700f997 \
 --subnet-id subnet-05e8874c36db0c354 \
 --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=EC2_PRIVATE_DRUPAL_DEVOPSTEAM01_B}]"
[OUTPUT]
{
    "Groups": [],
    "Instances": [
        {
            "AmiLaunchIndex": 0,
            "ImageId": "ami-043b89d6e9ef83673",
            "InstanceId": "i-05bec4684a21b6d9b",
            "InstanceType": "t3.micro",
            "KeyName": "CLD_KEY_DRUPAL_DEVOPSTEAM01",
            // ...
        }
    ]
}
```

## Task 03 - Test the connectivity

### Update your ssh connection string to test

* add tunnels for ssh and http pointing on the B Instance

```bash
//updated string connection
```

## Check SQL Accesses

```sql
[INPUT]
bitnami@ip-10-0-1-10:~$ mariadb --user admin --host dbi-devopsteam01.cshki92s4w5p.eu-west-3.rds.amazonaws.com --password bitnami_drupal
[OUTPUT]

Enter password:
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 115
Server version: 10.11.7-MariaDB managed by https://aws.amazon.com/rds/

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [bitnami_drupal]>
```

```sql
[INPUT]
bitnami@ip-10-0-1-140:~$ mariadb --user admin --host dbi-devopsteam01.cshki92s4w5p.eu-west-3.rds.amazonaws.com --password bitnami_drupal

[OUTPUT]
Enter password:
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Welcome to the MariaDB monitor.  Commands end with ; or \g.
Your MariaDB connection id is 116
Server version: 10.11.7-MariaDB managed by https://aws.amazon.com/rds/

Copyright (c) 2000, 2018, Oracle, MariaDB Corporation Ab and others.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

MariaDB [bitnami_drupal]>
```

### Check HTTP Accesses

```bash
curl localhost:8080
```

### Read and write test through the web app

* Login in both webapps (same login)

* Change the users' email address on a webapp... refresh the user's profile page on the second and validated that they are communicating with the same db (rds).

* Observations ?

> It works as expected, and the email is changed in the other instance.
> 
> It is also instantanious, just after the refresh

### Change the profil picture

* Observations ?

> The image loads correctly on the first instance, but the image is not loading on the other instance. This is expected, as the storage is not shared between the instances (could be done with a multiwrite fs).