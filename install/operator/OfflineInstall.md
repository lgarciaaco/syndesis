# Installing Fuse Online 1.8.18 in a closed environment

This is a guidance on how to install fuse when there is no access from Openshift to the Red Hat Container Catalog because the Openshift cluster sits behind a firewall.


## Downloading fuse images
##### Assumptions
There is an internal (internal as only accessible from the private network) docker registry that is accessible from the Openshift cluster.


##### Requirements
* A workstation preferably with linux or macos
* bash, docker
* The administrator (user performing this actions) needs to have direct access to the internal network as well to the internet.
* The administrator has an account for Red Hat Container Catalog (registry.redhat.io) and it is already configured for the docker daemon to download images.
* The administrator can push docker images in the internal registry and the docker daemon is correctly configured to do so.

##### Procedure
The product images are:

|Component|Image|
| ------- | --- |
|fuse-ignite-meta|registry.redhat.io/fuse7/fuse-ignite-meta:1.5|
|fuse-ignite-s2i|registry.redhat.io/fuse7/fuse-ignite-s2i:1.5|
|fuse-ignite-server|registry.redhat.io/fuse7/fuse-ignite-server:1.5|
|fuse-ignite-ui|registry.redhat.io/fuse7/fuse-ignite-ui:1.5|
|syndesis-operator|registry.redhat.io/fuse7/fuse-online-operator:1.5|
|fuse-postgres-exporter|registry.redhat.io/fuse7-tech-preview/fuse-postgres-exporter:1.5|
|jboss-amq-63|registry.access.redhat.com/jboss-amq-6/amq63-openshift:1.3|
|oauth-proxy|registry.redhat.io/openshift4/ose-oauth-proxy:4.2|
|prometheus|registry.redhat.io/openshift3/prometheus:v3.9|
|syndesis-postgresql|registry.redhat.io/rhscl/postgresql-95-rhel7:latest|
|fuse-dv|registry.redhat.io/fuse7-tech-preview/fuse-dv-rhel7:1.5|

All product images need to be downloaded from the Red Hat registry and uploaded to the local registry. For this example, I am going to use my private docker.io account (docker.io/lgarciaac), this should be replaced  by the internal docker registry that is accessible from the Openshift cluster and where the docker images are going to be pushed to.
```
REGISTRY=docker.io    # replace with internal registry
PROJECT=lgarciaac     # replace with internal project

for i in fuse-ignite-meta fuse-ignite-s2i fuse-ignite-server fuse-ignite-ui fuse-online-operator; do
  docker pull registry.redhat.io/fuse7/$i:1.5
  docker tag registry.redhat.io/fuse7/$i:1.5 $REGISTRY/$PROJECT/$i:1.5
  docker push $REGISTRY/$PROJECT/$i:1.5
done

docker pull registry.redhat.io/fuse7-tech-preview/fuse-postgres-exporter:1.5
docker tag registry.redhat.io/fuse7-tech-preview/fuse-postgres-exporter:1.5 $REGISTRY/$PROJECT/fuse-postgres-exporter:1.5
docker push $REGISTRY/$PROJECT/fuse-postgres-exporter:1.5

docker pull registry.access.redhat.com/jboss-amq-6/amq63-openshift:1.3
docker tag registry.access.redhat.com/jboss-amq-6/amq63-openshift:1.3 $REGISTRY/$PROJECT/amq63-openshift:1.3
docker push $REGISTRY/$PROJECT/amq63-openshift:1.3

docker pull registry.redhat.io/openshift4/ose-oauth-proxy:4.2
docker tag registry.redhat.io/openshift4/ose-oauth-proxy:4.2 $REGISTRY/$PROJECT/ose-oauth-proxy:4.2
docker push $REGISTRY/$PROJECT/ose-oauth-proxy:4.2

docker pull registry.redhat.io/openshift3/prometheus:v3.9
docker tag registry.redhat.io/openshift3/prometheus:v3.9 $REGISTRY/$PROJECT/prometheus:v3.9
docker push $REGISTRY/$PROJECT/prometheus:v3.9

docker pull registry.redhat.io/rhscl/postgresql-95-rhel7:latest
docker tag registry.redhat.io/rhscl/postgresql-95-rhel7:latest $REGISTRY/$PROJECT/postgresql-95-rhel7:latest
docker push $REGISTRY/$PROJECT/postgresql-95-rhel7:latest

docker pull registry.redhat.io/fuse7-tech-preview/fuse-dv-rhel7:1.5
docker tag registry.redhat.io/fuse7-tech-preview/fuse-dv-rhel7:1.5 $REGISTRY/$PROJECT/fuse-dv-rhel7:1.5
docker push $REGISTRY/$PROJECT/fuse-dv-rhel7:1.5
```

By the end of this operation, all product images should now be hosted in the internal private registry. In my case, this looks like:

|Component|Image|
| ------- | --- |
|fuse-ignite-meta|docker.io/lgarciaac/fuse-ignite-meta:1.5|
|fuse-ignite-s2i|docker.io/lgarciaac/fuse-ignite-s2i:1.5|
|fuse-ignite-server|docker.io/lgarciaac/fuse-ignite-server:1.5|
|fuse-ignite-ui|docker.io/lgarciaac/fuse-ignite-ui:1.5|
|syndesis-operator|docker.io/lgarciaac/fuse-online-operator:1.5|
|fuse-postgres-exporter|docker.io/lgarciaac/fuse-postgres-exporter:1.5|
|jboss-amq-63|docker.io/lgarciaac/amq63-openshift:1.3|
|oauth-proxy|docker.io/lgarciaac/ose-oauth-proxy:4.2|
|prometheus|docker.io/lgarciaac/prometheus:v3.9|
|syndesis-postgresql|docker.io/lgarciaac/postgresql-95-rhel7:latest|
|fuse-dv|docker.io/lgarciaac/fuse-dv-rhel7:1.5|

These are the image we will use in further steps.

## Getting fuse online
##### Requirements
* bash, git

Download the content from https://github.com/syndesisio/fuse-online-install/tree/1.8.18/templates. You can download each file or follow the following instructions:

```bash
git clone git@github.com:syndesisio/fuse-online-install.git
cd fuse-online-install
git fetch --all --tags
git checkout tags/1.8.18 -b 1.8.18
cd templates
```

Check that you are in the right directory
```bash
$ ls
fuse-online-template.yml  README.md  serviceaccount-as-oauthclient-restricted.yml
```
## Replacing fuse online images in the templates
Now we need to point to the images we uploaded previously. The images are specified in `fuse-online-template.yml`. In my case the following commands replace the images. Replace docker.io/lgarciaac accordly using your own images.

```bash
sed -i 's|name: ${SYNDESIS_REGISTRY}/fuse7/fuse-ignite-meta:1.5|name: docker.io/lgarciaac/fuse-ignite-meta:1.5|' fuse-online-template.yml
sed -i 's|name: ${SYNDESIS_REGISTRY}/fuse7/fuse-ignite-s2i:1.5|name: docker.io/lgarciaac/fuse-ignite-s2i:1.5|' fuse-online-template.yml
sed -i 's|name: ${SYNDESIS_REGISTRY}/fuse7/fuse-ignite-server:1.5|name: docker.io/lgarciaac/fuse-ignite-server:1.5|' fuse-online-template.yml
sed -i 's|name: ${SYNDESIS_REGISTRY}/fuse7/fuse-ignite-ui:1.5|name: docker.io/lgarciaac/fuse-ignite-ui:1.5|' fuse-online-template.yml
sed -i 's|name: registry.redhat.io/openshift4/ose-oauth-proxy:4.2|name: docker.io/lgarciaac/ose-oauth-proxy:4.2|' fuse-online-template.yml
sed -i 's|name: ${SYNDESIS_REGISTRY}/fuse7-tech-preview/fuse-postgres-exporter:1.5|name: docker.io/lgarciaac/fuse-postgres-exporter:1.5|' fuse-online-template.yml
sed -i 's|name: registry.redhat.io/openshift3/prometheus:v3.9|name: docker.io/lgarciaac/prometheus:v3.9|' fuse-online-template.yml
sed -i 's|name: ${SYNDESIS_REGISTRY}/rhscl/postgresql-95-rhel7:latest|name: docker.io/lgarciaac/postgresql-95-rhel7:latest|' fuse-online-template.yml
sed -i 's|name: registry.access.redhat.com/jboss-amq-6/amq63-openshift:1.3|name: docker.io/lgarciaac/amq63-openshift:1.3|' fuse-online-template.yml
sed -i 's|name: ${SYNDESIS_REGISTRY}/fuse7-tech-preview/fuse-dv-rhel7:1.5|name: docker.io/lgarciaac/fuse-dv-rhel7:1.5|' fuse-online-template.yml
```
Check that images were replaced:
```bash
$ grep $REGISTRY/$PROJECT fuse-online-template.yml
        name: docker.io/lgarciaac/fuse-ignite-server:1.5
        name: docker.io/lgarciaac/fuse-ignite-ui:1.5
        name: docker.io/lgarciaac/fuse-ignite-meta:1.5
        name: docker.io/lgarciaac/ose-oauth-proxy:4.2
        name: docker.io/lgarciaac/prometheus:v3.9
        name: docker.io/lgarciaac/fuse-postgres-exporter:1.5
        name: docker.io/lgarciaac/fuse-ignite-s2i:1.5
        name: docker.io/lgarciaac/amq63-openshift:1.3
        name: docker.io/lgarciaac/postgresql-95-rhel7:latest
        name: docker.io/lgarciaac/fuse-dv-rhel7:1.5

```
## Installing fuse online
 * A working openshift cluster v3.9 or higher
 * The openshift cluster is settup to pull images from the internal registry
 * bash, [oc](https://docs.openshift.com/container-platform/4.2/cli_reference/openshift_cli/getting-started-cli.html#cli-installing-cli_cli-developer-commands)
 * Cluster administrator access to the openshift cluster
##### Setup preparations

The following steps mimic the fuse online provisioning app that's used with the eval cluster.

##### Namespace Setup

```
oc new-project syndesis
```

##### Install the template into

```
oc project syndesis
oc create -f fuse-online-template.yml
```

##### Setup OAuth Service Account

```
oc create -f serviceaccount-as-oauthclient-restricted.yml
```

##### Figure out your route
If you type `oc status`, the first line of the output should show you the value we need to build the route. Now depending on your setup, this output  shows an IP or a dns record. We will treat those two cases differently.

* DNS Record:
    ```bash
    $ oc status | head -n1
    In project syndesis on server https://api.lgarcia.rhmw-integrations.net:6443
    ```
    Take the url and replace `syndesis.apps` for api. For this example, it is:
    ```
    export ROUTE_HOSTNAME=syndesis.apps.lgarcia.rhmw-integrations.net
    ```

* IP
    ```bash
    $ oc status | head -n1
    In project syndesis on server https://192.168.64.5:8443
    ```
    Take the IP part of the uri. For this example, it is:
    ```
    export ROUTE_HOSTNAME=192.168.64.5
    ```

##### Install Fuse Online

```
oc new-app --template=syndesis/fuse-ignite-1.8 -p OPENSHIFT_OAUTH_CLIENT_SECRET=$(oc sa get-token syndesis-oauth-client -n syndesis) -p IMAGE_STREAM_NAMESPACE=syndesis -p OPENSHIFT_PROJECT=syndesis -p SAR_PROJECT=syndesis -p ROUTE_HOSTNAME=$ROUTE_HOSTNAME -n syndesis
```
