### Deploy the elastic load balancer

In this task you will create a load balancer in AWS that will receive
the HTTP requests from clients and forward them to the Drupal
instances.

![Schema](./img/CLD_AWS_INFA.PNG)

## Task 01 Prerequisites for the ELB

* Create a dedicated security group

|Key|Value|
|:--|:--|
|Name|SG-DEVOPSTEAM[XX]-LB|
|Inbound Rules|Application Load Balancer|
|Outbound Rules|Refer to the infra schema|

```bash
$ aws ec2 create-security-group \
 --group-name SG-DEVOPSTEAM01-LB \
 --description "SG-DEVOPSTEAM01-LB" \
 --vpc-id vpc-03d46c285a2af77ba \
 --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=SG-DEVOPSTEAM01-LB}]"
{
    "GroupId": "sg-05fbb24587e568a07"
}

$ aws ec2 authorize-security-group-ingress \
 --group-id sg-05fbb24587e568a07 \
 --ip-permissions IpProtocol=tcp,FromPort=8080,ToPort=8080,IpRanges='[{CidrIp=10.0.0.0/28},{CidrIp=10.0.1.0/28},{CidrIp=10.0.1.128/28}]'
{
  "Return": true,
  "SecurityGroupRules": [
      {
          "SecurityGroupRuleId": "sgr-0bdf31f5fb3ec18db",
          "GroupId": "sg-05fbb24587e568a07",
          "GroupOwnerId": "709024702237",
          "IsEgress": false,
          "IpProtocol": "tcp",
          "FromPort": 8080,
          "ToPort": 8080,
          "CidrIpv4": "10.0.0.0/28"
      }
  ]
}
```

* Create the Target Group

|Key|Value|
|:--|:--|
|Target type|Instances|
|Name|TG-DEVOPSTEAM[XX]|
|Protocol and port|Refer to the infra schema|
|Ip Address type|IPv4|
|VPC|Refer to the infra schema|
|Protocol version|HTTP1|
|Health check protocol|HTTP|
|Health check path|/|
|Port|Traffic port|
|Healthy threshold|2 consecutive health check successes|
|Unhealthy threshold|2 consecutive health check failures|
|Timeout|5 seconds|
|Interval|10 seconds|
|Success codes|200|

```bash
aws elbv2 create-target-group \
    --name TG-DEVOPSTEAM01 \
    --protocol HTTP \
    --ip-address-type ipv4 \
    --target-type instance \
    --vpc-id vpc-03d46c285a2af77ba \
    --protocol-version HTTP1 \
    --health-check-protocol HTTP \
    --health-check-port 8080 \
    --health-check-path "/" \
    --port 8080 \
    --healthy-threshold-count 2 \
    --unhealthy-threshold-count 2 \
    --health-check-timeout-seconds 5 \
    --health-check-interval-seconds 10 \
    --matcher HttpCode=200


[OUTPUT]

```


## Task 02 Deploy the Load Balancer

[Source](https://aws.amazon.com/elasticloadbalancing/)

* Create the Load Balancer

|Key|Value|
|:--|:--|
|Type|Application Load Balancer|
|Name|ELB-DEVOPSTEAM99|
|Scheme|Internal|
|Ip Address type|IPv4|
|VPC|Refer to the infra schema|
|Security group|Refer to the infra schema|
|Listeners Protocol and port|Refer to the infra schema|
|Target group|Your own target group created in task 01|

Provide the following answers (leave any
field not mentioned at its default value):

```bash
$ aws elbv2 create-load-balancer \
  --name ELB-DEVOPSTEAM01 \
  --scheme internal \
  --ip-address-type ipv4 \
  --subnets subnet-05e8874c36db0c354 subnet-067cd8c9786078887 \
  --security-group sg-05fbb24587e568a07

{
  "LoadBalancers": [
    {
      "LoadBalancerArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM01/0fcddb113def09e2",
      "DNSName": "internal-ELB-DEVOPSTEAM01-1563399142.eu-west-3.elb.amazonaws.com",
      "CanonicalHostedZoneId": "Z3Q77PNBQS71R4",
      "CreatedTime": "2024-03-21T14:42:53.820000+00:00",
      "LoadBalancerName": "ELB-DEVOPSTEAM01",
      "Scheme": "internal",
      "VpcId": "vpc-03d46c285a2af77ba",
      "State": {
        "Code": "provisioning"
      },
      "Type": "application"
      // ...
    }
  ]
}

$ aws elbv2 register-targets \
--target-group-arn arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM01/ef9ade524eda7d8f \
--targets Id=i-0890609a4443bb12f Id=i-05bec4684a21b6d9b

OK

$ aws elbv2 create-listener --load-balancer-arn arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM01/0fcddb113def09e2 \
--protocol HTTP --port 8080 \
--default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM01/ef9ade524eda7d8f

{
    "Listeners": [
        {
            "ListenerArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:listener/app/ELB-DEVOPSTEAM01/0fcddb113def09e2/5474211b8fc8c71d",
            "LoadBalancerArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:loadbalancer/app/ELB-DEVOPSTEAM01/0fcddb113def09e2",
            "Port": 8080,
            "Protocol": "HTTP",
            "DefaultActions": [
                {
                    "Type": "forward",
                    "TargetGroupArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM01/ef9ade524eda7d8f",
                    "ForwardConfig": {
                        "TargetGroups": [
                            {
                                "TargetGroupArn": "arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM01/ef9ade524eda7d8f",
                                "Weight": 1
                            }
                        ],
                        "TargetGroupStickinessConfig": {
                            "Enabled": false
                        }
                    }
                }
            ]
        }
    ]
}

```

* Get the ELB FQDN (DNS NAME - A Record)

```bash
[INPUT]
aws elbv2 describe-load-balancers --names "ELB-DEVOPSTEAM01" | jq '.LoadBalancers[0].DNSName'
"internal-ELB-DEVOPSTEAM01-1563399142.eu-west-3.elb.amazonaws.com"

[OUTPUT]

```

* Get the ELB deployment status

Note : In the EC2 console select the Target Group. In the
       lower half of the panel, click on the **Targets** tab. Watch the
       status of the instance go from **unused** to **initial**.

* Ask the DMZ administrator to register your ELB with the reverse proxy via the private teams channel

* Update your string connection to test your ELB and test it

```bash
aws elbv2 describe-target-health --target-group-arn arn:aws:elasticloadbalancing:eu-west-3:709024702237:targetgroup/TG-DEVOPSTEAM01/ef9ade524eda7d8f
```

* Test your application through your ssh tunneling

```bash
[INPUT]
ssh devopsteam01@15.188.43.46 -i ~/.ssh/CLD_KEY_DMZ_DEVOPSTEAM01.pem -L 8080:internal-ELB-DEVOPSTEAM01-1563399142.eu-west-3.elb.amazonaws.com:8080
curl localhost:8080

[OUTPUT]
# It works!!!
```

#### Questions - Analysis

* On your local machine resolve the DNS name of the load balancer into
  an IP address using the `nslookup` command (works on Linux, macOS and Windows). Write
  the DNS name and the resolved IP Address(es) into the report.

```
❯ nslookup internal-ELB-DEVOPSTEAM01-1563399142.eu-west-3.elb.amazonaws.com
Server:		10.193.64.16
Address:	10.193.64.16#53

Non-authoritative answer:
Name:	internal-ELB-DEVOPSTEAM01-1563399142.eu-west-3.elb.amazonaws.com
Address: 10.0.1.12

❯ dig internal-ELB-DEVOPSTEAM01-1563399142.eu-west-3.elb.amazonaws.com
<TRUNCATED>
;; ANSWER SECTION:
internal-ELB-DEVOPSTEAM01-1563399142.eu-west-3.elb.amazonaws.com. 56 IN	A 10.0.1.9
internal-ELB-DEVOPSTEAM01-1563399142.eu-west-3.elb.amazonaws.com. 56 IN	A 10.0.1.132

```

We see that the DNS is a way to round-robin between the AZs (if we run the command multiple times, the order of the records change)

* From your Drupal instance, identify the ip from which requests are sent by the Load Balancer.

Help : execute `tcpdump port 8080`

```sh
$ tcpdump port 8080

listening on ens5, link-type EN10MB (Ethernet), snapshot length 262144 bytes
15:12:56.514166 IP 10.0.1.132.44988 > provisioner-local.http-alt: Flags [S], seq 3522211386, win 26883, options [mss 8961,sackOK,TS val 1014782316 ecr 0,nop,wscale 8], length 0
15:12:56.514196 IP provisioner-local.http-alt > 10.0.1.132.44988: Flags [S.], seq 1147542880, ack 3522211387, win 62643, options [mss 8961,sackOK,TS val 2928239211 ecr 1014782316,nop,wscale 7], length 0
15:12:56.515118 IP 10.0.1.132.44988 > provisioner-local.http-alt: Flags [.], ack 1, win 106, options [nop,nop,TS val 1014782317 ecr 2928239211], length 0
15:12:56.515118 IP 10.0.1.132.44988 > provisioner-local.http-alt: Flags [P.], seq 1:130, ack 1, win 106, options [nop,nop,TS val 1014782317 ecr 2928239211], length 129: HTTP: GET / HTTP/1.1
15:12:56.515145 IP provisioner-local.http-alt > 10.0.1.132.44988: Flags [.], ack 130, win 489, options [nop,nop,TS val 2928239212 ecr 1014782317], length 0
```
We see that the requests come from the 10.0.1.132 IP

* In the Apache access log identify the health check accesses from the
  load balancer and copy some samples into the report.

```sh
$ tail -n 10 /opt/bitnami/apache2/logs/access_log
10.0.1.9 - - [21/Mar/2024:15:13:58 +0000] "GET / HTTP/1.1" 200 5144
10.0.1.132 - - [21/Mar/2024:15:14:06 +0000] "GET / HTTP/1.1" 200 5144
10.0.1.9 - - [21/Mar/2024:15:14:08 +0000] "GET / HTTP/1.1" 200 5144
10.0.1.132 - - [21/Mar/2024:15:14:16 +0000] "GET / HTTP/1.1" 200 5144
10.0.1.9 - - [21/Mar/2024:15:14:18 +0000] "GET / HTTP/1.1" 200 5144
10.0.1.132 - - [21/Mar/2024:15:14:26 +0000] "GET / HTTP/1.1" 200 5144
10.0.1.9 - - [21/Mar/2024:15:14:28 +0000] "GET / HTTP/1.1" 200 5144
10.0.1.132 - - [21/Mar/2024:15:14:36 +0000] "GET / HTTP/1.1" 200 5144
10.0.1.9 - - [21/Mar/2024:15:14:38 +0000] "GET / HTTP/1.1" 200 5144
10.0.1.132 - - [21/Mar/2024:15:14:46 +0000] "GET / HTTP/1.1" 200 5144
```
