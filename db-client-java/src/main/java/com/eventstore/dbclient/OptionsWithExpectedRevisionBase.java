package com.eventstore.dbclient;

import com.google.errorprone.annotations.Immutable;

@Immutable
abstract class OptionsWithExpectedRevisionBase extends OptionsBase {
    
	ExpectedRevision expectedRevision;

    public ExpectedRevision getExpectedRevision() {
        return this.expectedRevision;
    }
    
	/**
	 * Builds a new (immutable) instance of the outer class.
	 */
	public abstract static class Builder<T extends OptionsWithExpectedRevisionBase> extends OptionsBase.Builder<T> {

	    public Builder<T> expectedRevision(ExpectedRevision revision) {
	        delegate().expectedRevision = revision;
	        return this;
	    }

	    public Builder<T> expectedRevision(StreamRevision revision) {
	    	delegate().expectedRevision = ExpectedRevision.expectedRevision(revision.getValueUnsigned());
	        return this;
	    }

	    public Builder<T> expectedRevision(long revision) {
	    	delegate().expectedRevision = ExpectedRevision.expectedRevision(revision);
	        return this;
	    }
		
		@Override
		public T build() {
			if (delegate().expectedRevision == null) {
				delegate().expectedRevision = ExpectedRevision.ANY;
			}
			return super.build();
		}
		
	}   
	
}
