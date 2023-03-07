package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ClusterProps;
import software.constructs.Construct;

public class ClusterStack extends Stack {

    private final Cluster cluster;

    public ClusterStack(final Construct scope, final String id, Vpc vpc) {
        this(scope, id, null, vpc);
    }

    public ClusterStack(final Construct scope, final String id, final StackProps props, Vpc vpc) {
        super(scope, id, props);

        this.cluster = new Cluster(this, "cluster01",
                ClusterProps.builder()
                        .clusterName("cluster-01")
                        .vpc(vpc)
                        .build()
        );
    }

    public Cluster getCluster() {
        return cluster;
    }
}
