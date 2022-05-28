package com.eventstore.dbclient;

import com.google.errorprone.annotations.Immutable;

@Immutable
public final class DeleteStreamOptions extends OptionsWithExpectedRevisionBase {

	private Boolean softDelete;

	public boolean isSoftDelete() {
		return this.softDelete;
	}

	/**
	 * Builds a new (immutable) instance of the outer class.
	 */
	public static final class Builder extends OptionsWithExpectedRevisionBase.Builder<DeleteStreamOptions> {

		public Builder softDelete() {
			delegate().softDelete = true;
			return this;
		}

		public Builder hardDelete() {
			delegate().softDelete = false;
			return this;
		}

		@Override
		public DeleteStreamOptions build() {
			if (delegate().softDelete == null) {
				delegate().softDelete = true;
			}
			return super.build();
		}

		@Override
		protected DeleteStreamOptions createNewInstance() {
			return new DeleteStreamOptions();
		}

	}

}
