# bootcampapp Project

## Overview

This repo contains a Quarkus app for use in the Red Hat UK Student BootCamp engagements.

The App is buildable using the JAVA 17 UBI S2I Builder (available by default from the Add+ in the OCP Dev Ux.

The App provides three endpoints:

1. /endpoints/health - returns the number of seconds the Pod has been active as a health response. If the 'ignore' flag is set on (see 2.) the endpoint returns nothing

2. /endpoints/setIgnoreState - takes one param (?state=(true|false) ) and sets the ignore flag used by the first endpoint

3. /endpoints/callLayers - demonstrates the use of ENV variables; returns the Hostname and IP of the Pod being called, plus if NEXTLAYER is set appends the response from that (allowing for chaining of requests)

## Deployment

Can be built locally using the standard Quarkus Maven commands, or built on an OCP cluster using the JAVA 17 UBI S2I Builder (preferred option as it adds the https route).

Adding an environment variable 'NEXTLAYER' to the deployment controls the actions of the /callLayers endpoint

## Caveat

As the system uses https the code overrides normal https security mechanisms manually. This is not good practice. 
