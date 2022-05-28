package com.eventstore.dbclient;

import com.google.errorprone.annotations.Immutable;

import io.grpc.Metadata;

@Immutable
abstract class OptionsBase {
	
	ConnectionMetadata metadata;
	
	Long deadline;
	
	OperationKind kind;
	
	UserCredentials credentials;
	
	boolean requiresLeader;

	protected OptionsBase() {
		this.metadata = new ConnectionMetadata();
		this.kind = OperationKind.Regular;
	}

	public Metadata getMetadata() {
		return this.metadata.build();
	}

	public boolean hasUserCredentials() {
		return this.metadata.hasUserCredentials();
	}

	public String getUserCredentials() {
		return this.metadata.getUserCredentials();
	}

	public Long getDeadline() {
		return deadline;
	}

	public OperationKind getKind() {
		return kind;
	}

	public boolean isLeaderRequired() {
		return this.requiresLeader;
	}

	public UserCredentials getCredentials() {
		return this.credentials;
	}

	/**
	 * Builds a new (immutable) instance of the outer class.
	 */
	public abstract static class Builder<T extends OptionsBase> {

		private T delegate;

		protected Builder() {
			this.delegate = createNewInstance();
		}

		public Builder<T> deadline(long durationInMs) {
			delegate.deadline = durationInMs;
			return this;
		}

		public Builder<T> authenticated(UserCredentials credentials) {
			delegate.credentials = credentials;
			return this;
		}

		public Builder<T> requiresLeader() {
			return requiresLeader(true);
		}

		public Builder<T> notRequireLeader() {
			return requiresLeader(false);
		}

		public Builder<T> requiresLeader(boolean value) {
			delegate.requiresLeader = value;
			return this;
		}
		
		protected abstract T createNewInstance();
		
		protected T delegate() {
			return delegate;
		}
		
		public T build() {
			final T result = delegate;
			delegate = createNewInstance();
			return result;
		}

	}

}
