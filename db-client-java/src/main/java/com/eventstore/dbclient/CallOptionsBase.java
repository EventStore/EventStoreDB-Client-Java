package com.eventstore.dbclient;

class CallOptionsBase<T> extends AuthOptionsBase {
    private Long deadline;
    private final OperationKind kind;
    private boolean requiresLeader;

    protected CallOptionsBase() {
        this(OperationKind.Regular);
    }

    protected CallOptionsBase(OperationKind kind) {
        this.kind = kind;
    }

    Long getDeadline() {
        return deadline;
    }

    OperationKind getKind() {
        return kind;
    }

    boolean isLeaderRequired() {
        return this.requiresLeader;
    }

    /**
     * Requires the request to be performed by the leader of the cluster.
     *
     * @return updated options
     */
    public T requiresLeader() {
        return requiresLeader(true);
    }

    /**
     * Do not require the request to be performed by the leader of the cluster.
     *
     * @return updated options
     */
    public T notRequireLeader() {
        return requiresLeader(false);
    }

    /**
     * If true, requires the request to be performed by the leader of the cluster.
     *
     * @param value
     * @return updated options
     */
    @SuppressWarnings("unchecked")
    public T requiresLeader(boolean value) {
        this.requiresLeader = value;
        return (T) this;
    }

    /**
     * A length of time (in milliseconds) to use for gRPC deadlines.
     *
     * @param durationInMs
     * @return
     */
    @SuppressWarnings("unchecked")
    public T deadline(long durationInMs) {
        deadline = durationInMs;

        return (T) this;
    }

    /**
     * Sets user credentials for the request.
     * User credentials take precedence over any configured {@link UserCertificate}.
     *
     * @param userCredentials
     * @return updated options
     * @see UserCredentials
     */
    @SuppressWarnings("unchecked")
    public T authenticated(UserCredentials userCredentials) {
        setUserCredentials(userCredentials);
        return (T) this;
    }

    /**
     * Sets user credentials for the request.
     * User credentials take precedence over any configured {@link UserCertificate}.
     *
     * @param login
     * @param password
     * @return updated options
     */
    public T authenticated(String login, String password) {
        return authenticated(new UserCredentials(login, password));
    }

    /**
     * Sets user certificate for the request.
     * If any {@link UserCredentials} are configured, the server will ignore the user certificate.
     *
     * @param userCertificate
     * @return updated options
     * @see UserCertificate
     */
    @SuppressWarnings("unchecked")
    public T authenticated(UserCertificate userCertificate) {
        setUserCertificate(userCertificate);
        return (T) this;
    }
}
