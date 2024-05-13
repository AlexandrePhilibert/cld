# Task 3 - Add and exercise resilience

By now you should have understood the general principle of configuring, running and accessing applications in Kubernetes. However, the above application has no support for resilience. If a container (resp. Pod) dies, it stops working. Next, we add some resilience to the application.

## Subtask 3.1 - Add Deployments

In this task you will create Deployments that will spawn Replica Sets as health-management components.

Converting a Pod to be managed by a Deployment is quite simple.

  * Have a look at an example of a Deployment described here: <https://kubernetes.io/docs/concepts/workloads/controllers/deployment/>

  * Create Deployment versions of your application configurations (e.g. `redis-deploy.yaml` instead of `redis-pod.yaml`) and modify/extend them to contain the required Deployment parameters.

  * Again, be careful with the YAML indentation!

  * Make sure to have always 2 instances of the API and Frontend running. 

  * Use only 1 instance for the Redis-Server. Why?

    > Otherwise we would get in the situation where some data is persisted to one instance, and some to the other. So it would be possible to get in a situation where the TODO you just added does not appear.
  * Delete all application Pods (using `kubectl delete pod ...`) and replace them with deployment versions.

  * Verify that the application is still working and the Replica Sets are in place. (`kubectl get all`, `kubectl get pods`, `kubectl describe ...`)

## Subtask 3.2 - Verify the functionality of the Replica Sets

In this subtask you will intentionally kill (delete) Pods and verify that the application keeps working and the Replica Set is doing its task.

Hint: You can monitor the status of a resource by adding the `--watch` option to the `get` command. To watch a single resource:

```sh
$ kubectl get <resource-name> --watch
```

To watch all resources of a certain type, for example all Pods:

```sh
$ kubectl get pods --watch
```

You may also use `kubectl get all` repeatedly to see a list of all resources.  You should also verify if the application stays available by continuously reloading your browser window.

  * What happens if you delete a Frontend or API Pod? How long does it take for the system to react?
    > Another one is created nearly immediately. It should happen in less than 5 seconds (the reconciliation rate of the reconcilier)
    
  * What happens when you delete the Redis Pod?

    > the application is down for a few seconds, and all  the todos are lost.
    
  * How can you change the number of instances temporarily to 3? Hint: look for scaling in the deployment documentation

    > You change the replicas property in the deployment YAML file. You can also use the `kubectl scale` command:
    ```sh
    kubectl scale deployment frontend --replicas 2
    ```
    
  * What autoscaling features are available? Which metrics are used?

    > Depends on the metrics enabled for for the cluster.
    > The most common features are CPU and Memory Usage.
    
  * How can you update a component? (see "Updating a Deployment" in the deployment documentation)

    > Update the deployment YAML file, and use `kubectl apply` to deploy the changes to the cluster.

## Subtask 3.3 - Put autoscaling in place and load-test it

On the GKE cluster deploy autoscaling on the Frontend with a target CPU utilization of 30% and number of replicas between 1 and 4. 

> The HPA (Horizontal Pod Autoscaler) definition file is available at [part3/frontend-hpa.yaml](./part3/frontend-hpa.yaml)

Load-test using Vegeta (500 requests should be enough).

> [!NOTE]
>
> - The autoscale may take a while to trigger.
>
> - If your autoscaling fails to get the cpu utilization metrics, run the following command
>
>   - ```sh
>     $ kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
>     ```
>
>   - Then add the *resources* part in the *container part* in your `frontend-deploy` :
>
>   - ```yaml
>     spec:
>       containers:
>         - ...:
>           env:
>             - ...:
>           resources:
>             requests:
>               cpu: 10m
>     ```
>

## Deliverables

Document your observations in the lab report. Document any difficulties you faced and how you overcame them. Copy the object descriptions into the lab report.

> I had to deploy the metrics service.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  labels:
    component: redis
    app: todo
spec:
  replicas: 1
  selector:
    matchLabels:
      component: redis
      app: todo
  template:
    metadata:
      labels:
        component: redis
        app: todo
    spec:
      containers:
        - name: redis
          image: redis
          ports:
            - containerPort: 6379
          args:
            - redis-server
            - --requirepass ccp2
            - --appendonly yes
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api
  labels:
    component: api
    app: todo
spec:
  replicas: 2
  selector:
    matchLabels:
      component: api
      app: todo
  template:
    metadata:
      labels:
        component: api
        app: todo
    spec:
      containers:
        - name: api
          image: icclabcna/ccp2-k8s-todo-api
          ports:
            - containerPort: 8081
          env:
            - name: REDIS_ENDPOINT
              value: redis-svc
            - name: REDIS_PWD
              value: ccp2
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  labels:
    component: frontend
    app: todo
spec:
  replicas: 2
  selector:
    matchLabels:
      component: frontend
      app: todo
  template:
    metadata:
      labels:
        component: frontend
        app: todo
    spec:
      containers:
        - name: frontend
          image: icclabcna/ccp2-k8s-todo-frontend
          ports:
            - containerPort: 8080
          env:
            - name: API_ENDPOINT_URL
              value: http://api-svc:8081
          resources:
            requests:
              cpu: 10m
```