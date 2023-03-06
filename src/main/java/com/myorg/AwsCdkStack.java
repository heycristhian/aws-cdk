package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class AwsCdkStack extends Stack {
    public AwsCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public AwsCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

    }
}
