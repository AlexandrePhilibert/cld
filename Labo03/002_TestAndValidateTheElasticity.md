# Task 003 - Test and validate the elasticity

![Schema](./img/CLD_AWS_INFA.PNG)

## Simulate heavy load to trigger a scaling action

- [Install the package "stress" on your Drupal instance](https://www.geeksforgeeks.org/linux-stress-command-with-examples/)

- [Install the package htop on your Drupal instance](https://www.geeksforgeeks.org/htop-command-in-linux-with-examples/)

- Check how many vCPU are available (with htop command)

```
[INPUT]
htop

[OUTPUT]
2 cores
```

![](./img/CLD_HTOP.PNG)

### Stress your instance

```
[INPUT]
//stress command
stress --cpu 2
[OUTPUT]
```

![](./img/CLD_HTOP_STRESSED.PNG)

- (Scale-IN) Observe the autoscaling effect on your infa

![](./img/CLD_AWS_CLOUDWATCH_CPU_METRICS.PNG)

![](./img/CLD_AWS_EC2_LIST.PNG.PNG)

![](./img/CLD_AWS_ASG_ACTIVITY_HISTORY.PNG)

![](./img/CLD_AWS_CLOUDWATCH_ALARMHIGH_STATS.PNG)

- (Scale-OUT) As soon as all 4 instances have started, end stress on the main machine.

[Change the default cooldown period](https://docs.aws.amazon.com/autoscaling/ec2/userguide/ec2-auto-scaling-scaling-cooldowns.html)

![](./img/CLD_AWS_CLOUDWATCH_ALARMLOW_STATS.PNG)

![](./img/CLD_AWS_ASG_ACTIVITY_HISTORY_SCALEOUT.PNG)

## Release Cloud resources

Once you have completed this lab release the cloud resources to avoid
unnecessary charges:

- Terminate the EC2 instances.
  - Make sure the attached EBS volumes are deleted as well.
- Delete the Auto Scaling group.
- Delete the Elastic Load Balancer.
- Delete the RDS instance.

(this last part does not need to be documented in your report.)

## Delivery

Inform your teacher of the deliverable on the repository (link to the commit to retrieve)
