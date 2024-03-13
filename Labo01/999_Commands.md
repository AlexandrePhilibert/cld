# Setup infrastructure

aws ec2 create-subnet \
 --vpc-id vpc-03d46c285a2af77ba \
 --cidr-block 10.0.1.0/28 \
 --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=SUB-PRIVATE-DEVOPSTEAM01}]"

aws ec2 create-route-table --vpc-id vpc-03d46c285a2af77ba \
 --tag-specifications "ResourceType=route-table,Tags=[{Key=Name,Value=RTBLE-PRIVATE-DRUPAL-DEVOPSTEAM01}]"

aws ec2 associate-route-table --route-table-id rtb-0b24357ababc463ea --subnet-id subnet-07c0f57485cf45863

aws ec2 create-security-group \
 --group-name SG-PRIVATE-DRUPAL-DEVOPSTEAM01 \
 --description "SG-PRIVATE-DRUPAL-DEVOPSTEAM01" \
 --vpc-id vpc-03d46c285a2af77ba

aws ec2 create-route \
--route-table-id rtb-0b24357ababc463ea \
 --destination-cidr-block 0.0.0.0/0 \
 --instance-id i-085f07b949466919e

aws ec2 authorize-security-group-ingress \
 --group-id sg-0c82cccdd3700f997 \
 --ip-permissions IpProtocol=tcp,FromPort=22,ToPort=22,IpRanges='[{CidrIp=10.0.0.0/28}]'

aws ec2 authorize-security-group-ingress \
 --group-id sg-0c82cccdd3700f997 \
 --ip-permissions IpProtocol=tcp,FromPort=8080,ToPort=8080,IpRanges='[{CidrIp=10.0.0.0/28}]'

aws ec2 run-instances \
 --image-id ami-00b3a1b7cfab20134 \
 --count 1 \
 --instance-type t3.micro \
 --key-name CLD_KEY_DRUPAL_DEVOPSTEAM01 \
 --private-ip-address 10.0.1.10 \
 --security-group-ids sg-0c82cccdd3700f997 \
 --subnet-id subnet-07c0f57485cf45863 \
 --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=EC2_PRIVATE_DRUPAL_DEVOPSTEAM01}]"

# Connect to drupal instance

```bash
Host cld_dmz
    HostName 15.188.43.46
    IdentityFile ~/.ssh/CLD_KEY_DMZ_DEVOPSTEAM01.pem
    User devopsteam01

Host cld_drupal
    HostName 10.0.1.10
    IdentityFile ~/.ssh/CLD_KEY_DRUPAL_DEVOPSTEAM01.pem
    User bitnami
```

Then:

```bash
ssh -J cld_dmz cld_drupal
```

aws ec2 run-instances \
 --image-id ami-00b3a1b7cfab20134 \
 --count 1 \
 --instance-type t3.micro \
 --key-name CLD_KEY_DRUPAL_DEVOPSTEAM01 \
 --private-ip-address 10.0.1.10 \
 --security-group-ids sg-0c82cccdd3700f997 \
 --subnet-id subnet-07c0f57485cf45863 \
 --block-device-mappings '[{"DeviceName":"/dev/sda1","Ebs":{"VolumeSize":10,"DeleteOnTermination":false,"VolumeType":"gp2"}}]' \
 --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=EC2_PRIVATE_DRUPAL_DEVOPSTEAM01}]"
