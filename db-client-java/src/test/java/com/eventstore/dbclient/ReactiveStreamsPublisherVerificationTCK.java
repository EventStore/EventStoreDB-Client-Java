package com.eventstore.dbclient;

import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.SkipException;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class ReactiveStreamsPublisherVerificationTCK<T> extends PublisherVerification<T> {

    public ReactiveStreamsPublisherVerificationTCK(TestEnvironment env) {
        super(env);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        mapTestNgAssumptionViolations(super::setUp);
    }

    @Test
    public void testAllTestNGTestsAreOverridden() {
        for (Method method : ReactiveStreamsPublisherVerificationTCK.class.getMethods())
            if (method.getAnnotation(org.testng.annotations.Test.class) != null)
                assertNotNull("TestNG test must be annotated with junit @Test: " + method, method.getAnnotation(Test.class));
    }

    @Override
    @Test
    public void required_createPublisher1MustProduceAStreamOfExactly1Element() throws Throwable {
        mapTestNgAssumptionViolations(super::required_createPublisher1MustProduceAStreamOfExactly1Element);
    }

    @Override
    @Test
    public void required_createPublisher3MustProduceAStreamOfExactly3Elements() throws Throwable {
        mapTestNgAssumptionViolations(super::required_createPublisher3MustProduceAStreamOfExactly3Elements);
    }

    @Override
    @Test
    public void required_validate_maxElementsFromPublisher() throws Exception {
        mapTestNgAssumptionViolations(super::required_validate_maxElementsFromPublisher);
    }

    @Override
    @Test
    public void required_validate_boundedDepthOfOnNextAndRequestRecursion() throws Exception {
        mapTestNgAssumptionViolations(super::required_validate_boundedDepthOfOnNextAndRequestRecursion);
    }

    @Override
    @Test
    public void required_spec101_subscriptionRequestMustResultInTheCorrectNumberOfProducedElements() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec101_subscriptionRequestMustResultInTheCorrectNumberOfProducedElements);
    }

    @Override
    @Test
    public void required_spec102_maySignalLessThanRequestedAndTerminateSubscription() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec102_maySignalLessThanRequestedAndTerminateSubscription);
    }

    @Override
    @Test
    public void stochastic_spec103_mustSignalOnMethodsSequentially() throws Throwable {
        mapTestNgAssumptionViolations(super::stochastic_spec103_mustSignalOnMethodsSequentially);
    }

    @Override
    @Test
    public void optional_spec104_mustSignalOnErrorWhenFails() throws Throwable {
        mapTestNgAssumptionViolations(super::optional_spec104_mustSignalOnErrorWhenFails);
    }

    @Override
    @Test
    public void required_spec105_mustSignalOnCompleteWhenFiniteStreamTerminates() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec105_mustSignalOnCompleteWhenFiniteStreamTerminates);
    }

    @Override
    @Test
    public void optional_spec105_emptyStreamMustTerminateBySignallingOnComplete() throws Throwable {
        mapTestNgAssumptionViolations(super::optional_spec105_emptyStreamMustTerminateBySignallingOnComplete);
    }

    @Override
    @Test
    public void untested_spec106_mustConsiderSubscriptionCancelledAfterOnErrorOrOnCompleteHasBeenCalled() throws Throwable {
        mapTestNgAssumptionViolations(super::untested_spec106_mustConsiderSubscriptionCancelledAfterOnErrorOrOnCompleteHasBeenCalled);
    }

    @Override
    @Test
    public void required_spec107_mustNotEmitFurtherSignalsOnceOnCompleteHasBeenSignalled() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec107_mustNotEmitFurtherSignalsOnceOnCompleteHasBeenSignalled);
    }

    @Override
    @Test
    public void untested_spec107_mustNotEmitFurtherSignalsOnceOnErrorHasBeenSignalled() throws Throwable {
        mapTestNgAssumptionViolations(super::untested_spec107_mustNotEmitFurtherSignalsOnceOnErrorHasBeenSignalled);
    }

    @Override
    @Test
    public void untested_spec108_possiblyCanceledSubscriptionShouldNotReceiveOnErrorOrOnCompleteSignals() throws Throwable {
        mapTestNgAssumptionViolations(super::untested_spec108_possiblyCanceledSubscriptionShouldNotReceiveOnErrorOrOnCompleteSignals);
    }

    @Override
    @Test
    public void untested_spec109_subscribeShouldNotThrowNonFatalThrowable() throws Throwable {
        mapTestNgAssumptionViolations(super::untested_spec109_subscribeShouldNotThrowNonFatalThrowable);
    }

    @Override
    @Test
    public void required_spec109_subscribeThrowNPEOnNullSubscriber() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec109_subscribeThrowNPEOnNullSubscriber);
    }

    @Override
    @Test
    public void required_spec109_mustIssueOnSubscribeForNonNullSubscriber() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec109_mustIssueOnSubscribeForNonNullSubscriber);
    }

    @Override
    @Test
    public void required_spec109_mayRejectCallsToSubscribeIfPublisherIsUnableOrUnwillingToServeThemRejectionMustTriggerOnErrorAfterOnSubscribe() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec109_mayRejectCallsToSubscribeIfPublisherIsUnableOrUnwillingToServeThemRejectionMustTriggerOnErrorAfterOnSubscribe);
    }

    @Override
    @Test
    public void untested_spec110_rejectASubscriptionRequestIfTheSameSubscriberSubscribesTwice() throws Throwable {
        mapTestNgAssumptionViolations(super::untested_spec110_rejectASubscriptionRequestIfTheSameSubscriberSubscribesTwice);
    }

    @Override
    @Test
    public void optional_spec111_maySupportMultiSubscribe() throws Throwable {
        mapTestNgAssumptionViolations(super::optional_spec111_maySupportMultiSubscribe);
    }

    @Override
    @Test
    public void optional_spec111_registeredSubscribersMustReceiveOnNextOrOnCompleteSignals() throws Throwable {
        mapTestNgAssumptionViolations(super::optional_spec111_registeredSubscribersMustReceiveOnNextOrOnCompleteSignals);
    }

    @Override
    @Test
    public void optional_spec111_multicast_mustProduceTheSameElementsInTheSameSequenceToAllOfItsSubscribersWhenRequestingOneByOne() throws Throwable {
        mapTestNgAssumptionViolations(super::optional_spec111_multicast_mustProduceTheSameElementsInTheSameSequenceToAllOfItsSubscribersWhenRequestingOneByOne);
    }

    @Override
    @Test
    public void optional_spec111_multicast_mustProduceTheSameElementsInTheSameSequenceToAllOfItsSubscribersWhenRequestingManyUpfront() throws Throwable {
        mapTestNgAssumptionViolations(super::optional_spec111_multicast_mustProduceTheSameElementsInTheSameSequenceToAllOfItsSubscribersWhenRequestingManyUpfront);
    }

    @Override
    @Test
    public void optional_spec111_multicast_mustProduceTheSameElementsInTheSameSequenceToAllOfItsSubscribersWhenRequestingManyUpfrontAndCompleteAsExpected() throws Throwable {
        mapTestNgAssumptionViolations(super::optional_spec111_multicast_mustProduceTheSameElementsInTheSameSequenceToAllOfItsSubscribersWhenRequestingManyUpfrontAndCompleteAsExpected);
    }

    @Override
    @Test
    public void required_spec302_mustAllowSynchronousRequestCallsFromOnNextAndOnSubscribe() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec302_mustAllowSynchronousRequestCallsFromOnNextAndOnSubscribe);
    }

    @Override
    @Test
    public void required_spec303_mustNotAllowUnboundedRecursion() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec303_mustNotAllowUnboundedRecursion);
    }

    @Override
    @Test
    public void untested_spec304_requestShouldNotPerformHeavyComputations() throws Exception {
        mapTestNgAssumptionViolations(super::untested_spec304_requestShouldNotPerformHeavyComputations);
    }

    @Override
    @Test
    public void untested_spec305_cancelMustNotSynchronouslyPerformHeavyComputation() throws Exception {
        mapTestNgAssumptionViolations(super::untested_spec305_cancelMustNotSynchronouslyPerformHeavyComputation);
    }

    @Override
    @Test
    public void required_spec306_afterSubscriptionIsCancelledRequestMustBeNops() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec306_afterSubscriptionIsCancelledRequestMustBeNops);
    }

    @Override
    @Test
    public void required_spec307_afterSubscriptionIsCancelledAdditionalCancelationsMustBeNops() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec307_afterSubscriptionIsCancelledAdditionalCancelationsMustBeNops);
    }

    @Override
    @Test
    public void required_spec309_requestZeroMustSignalIllegalArgumentException() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec309_requestZeroMustSignalIllegalArgumentException);
    }

    @Override
    @Test
    public void required_spec309_requestNegativeNumberMustSignalIllegalArgumentException() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec309_requestNegativeNumberMustSignalIllegalArgumentException);
    }

    @Override
    @Test
    public void optional_spec309_requestNegativeNumberMaySignalIllegalArgumentExceptionWithSpecificMessage() throws Throwable {
        mapTestNgAssumptionViolations(super::optional_spec309_requestNegativeNumberMaySignalIllegalArgumentExceptionWithSpecificMessage);
    }

    @Override
    @Test
    public void required_spec312_cancelMustMakeThePublisherToEventuallyStopSignaling() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec312_cancelMustMakeThePublisherToEventuallyStopSignaling);
    }

    @Override
    @Test
    public void required_spec313_cancelMustMakeThePublisherEventuallyDropAllReferencesToTheSubscriber() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec313_cancelMustMakeThePublisherEventuallyDropAllReferencesToTheSubscriber);
    }

    @Override
    @Test
    public void required_spec317_mustSupportAPendingElementCountUpToLongMaxValue() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec317_mustSupportAPendingElementCountUpToLongMaxValue);
    }

    @Override
    @Test
    public void required_spec317_mustSupportACumulativePendingElementCountUpToLongMaxValue() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec317_mustSupportACumulativePendingElementCountUpToLongMaxValue);
    }

    @Override
    @Test
    public void required_spec317_mustNotSignalOnErrorWhenPendingAboveLongMaxValue() throws Throwable {
        mapTestNgAssumptionViolations(super::required_spec317_mustNotSignalOnErrorWhenPendingAboveLongMaxValue);
    }

    private static <T extends Throwable> void mapTestNgAssumptionViolations(ThrowingRunnable<T> runnable) throws T {
        try {
            runnable.run();
        } catch (SkipException e) {
            throw new AssumptionViolatedException(e.getMessage());
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable<T extends Throwable> {
        void run() throws T;
    }
}