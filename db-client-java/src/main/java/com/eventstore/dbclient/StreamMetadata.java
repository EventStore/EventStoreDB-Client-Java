package com.eventstore.dbclient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StreamMetadata {
    private Integer maxAge;
    private Integer truncateBefore;
    private Integer cacheControl;
    private Acl acl;
    private Integer maxCount;
    private HashMap<String, Object> customProperties;

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public Integer getTruncateBefore() {
        return truncateBefore;
    }

    public void setTruncateBefore(Integer truncateBefore) {
        this.truncateBefore = truncateBefore;
    }

    public Integer getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(Integer cacheControl) {
        this.cacheControl = cacheControl;
    }

    public Acl getAcl() {
        return acl;
    }

    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public HashMap<String, Object> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(HashMap<String, Object> customProperties) {
        this.customProperties = customProperties;
    }

    static private void insertValue(HashMap<String, Object> output, String key, Object value) {
        if (value != null) {
            output.put(key, value);
        }
    }

    public Object serialize() {
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

    public static StreamMetadata deserialize(HashMap<String, Object> source) {
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
