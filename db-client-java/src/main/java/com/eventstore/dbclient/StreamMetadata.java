package com.eventstore.dbclient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents stream metadata with strongly typed properties for system values and a dictionary-like interface for
 * custom values.
 */
public class StreamMetadata {
    private Integer maxAge;
    private Integer truncateBefore;
    private Integer cacheControl;
    private Acl acl;
    private Integer maxCount;
    private HashMap<String, Object> customProperties;

    /**
     * The maximum age of events allowed in the stream.
     */
    public Integer getMaxAge() {
        return maxAge;
    }

    /**
     * The maximum age of events allowed in the stream.
     */
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * The event number from which previous events can be scavenged. This is used to implement deletion of
     * streams.
     */
    public Integer getTruncateBefore() {
        return truncateBefore;
    }

    /**
     * The event number from which previous events can be scavenged. This is used to implement deletion of
     * streams.
     */
    public void setTruncateBefore(Integer truncateBefore) {
        this.truncateBefore = truncateBefore;
    }

    /**
     * The amount of time for which the stream head is cacheable (in seconds).
     */
    public Integer getCacheControl() {
        return cacheControl;
    }

    /**
     * The amount of time for which the stream head is cacheable (in seconds).
     */
    public void setCacheControl(Integer cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * The Access Control List of the stream (ACL).
     */
    public Acl getAcl() {
        return acl;
    }

    /**
     * The Access Control List of the stream (ACL).
     */
    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    /**
     * The maximum number of events allowed in the stream.
     */
    public Integer getMaxCount() {
        return maxCount;
    }

    /**
     * The maximum number of events allowed in the stream.
     */
    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    /**
     * An enumerable of key-value pairs of keys to JSON text for user-provider metadata.
     */
    public HashMap<String, Object> getCustomProperties() {
        return customProperties;
    }

    /**
     * An enumerable of key-value pairs of keys to JSON text for user-provider metadata.
     */
    public void setCustomProperties(HashMap<String, Object> customProperties) {
        this.customProperties = customProperties;
    }

    static private void insertValue(HashMap<String, Object> output, String key, Object value) {
        if (value != null) {
            output.put(key, value);
        }
    }

    Object serialize() {
        HashMap<String, Object> output = new HashMap<>();

        insertValue(output, "$maxAge", this.maxAge);
        insertValue(output, "$maxCount", this.maxCount);
        insertValue(output, "$tb", this.truncateBefore);
        insertValue(output, "$cacheControl", this.cacheControl);

        if (this.acl != null) {
            insertValue(output, "$acl", this.acl.serialize());
        }

        if (this.customProperties != null) {
            this.customProperties.forEach((key, value) -> {
                if (key.startsWith("$"))
                    return;

                insertValue(output, key, value);
            });
        }

        return output;
    }

     static StreamMetadata deserialize(HashMap<String, Object> source) {
        StreamMetadata metadata = new StreamMetadata();
        HashMap<String, Object> customProperties = null;

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            switch (entry.getKey()) {
                case "$maxAge":
                    metadata.setMaxAge((Integer) entry.getValue());
                    break;
                case "$maxCount":
                    metadata.setMaxCount((Integer) entry.getValue());
                    break;
                case "$tb":
                    metadata.setTruncateBefore((Integer) entry.getValue());
                    break;
                case "$cacheControl":
                    metadata.setCacheControl((Integer) entry.getValue());
                    break;
                case "$acl":
                    metadata.setAcl(Acls.deserialize(entry.getValue()));
                    break;
                default:
                    customProperties = customProperties == null ? new HashMap<>() : customProperties;
                    customProperties.put(entry.getKey(), entry.getValue());
                    break;
            }
        }

        metadata.setCustomProperties(customProperties);

        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamMetadata that = (StreamMetadata) o;
        return Objects.equals(maxAge, that.maxAge) && Objects.equals(truncateBefore, that.truncateBefore) && Objects.equals(cacheControl, that.cacheControl) && Objects.equals(acl, that.acl) && Objects.equals(maxCount, that.maxCount) && Objects.equals(customProperties, that.customProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxAge, truncateBefore, cacheControl, acl, maxCount, customProperties);
    }
}
