package com.myorg;

import software.amazon.awscdk.App;

public class AwsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        // CREATING VPC
        VpcStack vpcStack = new VpcStack(app, "Vpc");

        //CREATING CLUSTER
        ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        //CREATING SERVICE (TASK/ALB/TARGET GROUP/LOGAWS)
        ServiceStack serviceStack = new ServiceStack(app, "Service", clusterStack.getCluster());
        serviceStack.addDependency(clusterStack);

        app.synth();
    }
}

