package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateServiceProps;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupProps;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class ServiceStack extends Stack {
    public ServiceStack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public ServiceStack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        Map<String, String> envVariables = new HashMap<>();
        String rdsAddress = Fn.importValue("rds-endpoint");
        envVariables.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://" + rdsAddress + ":5432/financeiro?createDatabaseIfNotExist=true");
        envVariables.put("SPRING_DATASOURCE_USERNAME", "heycristhian");
        envVariables.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));


        ApplicationLoadBalancedFargateService service = new ApplicationLoadBalancedFargateService(this, "ALB01",
                ApplicationLoadBalancedFargateServiceProps.builder()
                        .serviceName("service-01")
                        .cluster(cluster)
                        .cpu(512)
                        .memoryLimitMiB(1024)
                        .desiredCount(2)
                        .listenerPort(8080)
                        .taskImageOptions(getTaskImageOptions(envVariables))
                        .publicLoadBalancer(true)
                        .build());

        service.getTargetGroup().configureHealthCheck(
                HealthCheck.builder()
                        .path("/actuator/health")
                        .port("8080")
                        .healthyHttpCodes("200")
                        .build()
        );

        ScalableTaskCount scalableTaskCount = service.getService().autoScaleTaskCount(
                EnableScalingProps.builder()
                        .minCapacity(2)
                        .maxCapacity(3)
                        .build()
        );

        scalableTaskCount.scaleOnCpuUtilization("Service01AutoScaling",
                CpuUtilizationScalingProps.builder()
                        .targetUtilizationPercent(50)
                        .scaleInCooldown(Duration.seconds(60))
                        .scaleOutCooldown(Duration.seconds(60))
                        .build());

    }

    private ApplicationLoadBalancedTaskImageOptions getTaskImageOptions(Map<String, String> envVariables) {
        return ApplicationLoadBalancedTaskImageOptions.builder()
                .containerName("controle-financeiro")
                .image(ContainerImage.fromRegistry("heycristhian/controle-financeiro:latest"))
                .containerPort(8080)
                .logDriver(getLogDriver())
                .environment(envVariables)
                .build();
    }

    private LogDriver getLogDriver() {
        return LogDriver.awsLogs(getAwsLogDriverProps());
    }

    private AwsLogDriverProps getAwsLogDriverProps() {
        return AwsLogDriverProps.builder()
                .logGroup(getLogGroup())
                .streamPrefix("Service01")
                .build();
    }

    private LogGroup getLogGroup() {
        return new LogGroup(this, "Service01LogGroup", LogGroupProps.builder()
                .logGroupName("Service01")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build());
    }
}
