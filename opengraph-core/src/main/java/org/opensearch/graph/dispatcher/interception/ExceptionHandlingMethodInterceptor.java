package org.opensearch.graph.dispatcher.interception;





import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class ExceptionHandlingMethodInterceptor implements MethodInterceptor {
    //region MethodInterceptor Implementation
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        try {
            return methodInvocation.proceed();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
    //endregion
}
