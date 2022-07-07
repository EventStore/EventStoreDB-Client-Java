package com.eventstore.dbclient;

import java.util.*;

/**
 * Stream-related access control list (ACL).
 */
public class StreamAcl implements Acl {
    private ArrayList<String> readRoles;
    private ArrayList<String> writeRoles;
    private ArrayList<String> deleteRoles;
    private ArrayList<String> metaReadRoles;
    private ArrayList<String> metaWriteRoles;

    /**
     * Adds read roles.
     * @param roles
     */
    public StreamAcl addReadRoles(String... roles) {
        this.readRoles = this.readRoles == null ? new ArrayList<>() : this.readRoles;
        this.readRoles.addAll(Arrays.asList(roles));

        return this;
    }

    /**
     * Adds write roles.
     * @param roles
     */
    public StreamAcl addWriteRoles(String... roles) {
        this.writeRoles = this.writeRoles == null ? new ArrayList<>() : this.writeRoles;
        this.writeRoles.addAll(Arrays.asList(roles));

        return this;
    }

    /**
     * Adds delete roles.
     * @param roles
     */
    public StreamAcl addDeleteRoles(String... roles) {
        this.deleteRoles = this.deleteRoles == null ? new ArrayList<>() : this.deleteRoles;
        this.deleteRoles.addAll(Arrays.asList(roles));

        return this;
    }

    /**
     * Adds metadata read roles.
     * @param roles
     */
    public StreamAcl addMetaReadRoles(String... roles) {
        this.metaReadRoles = this.metaReadRoles == null ? new ArrayList<>() : this.metaReadRoles;
        this.metaReadRoles.addAll(Arrays.asList(roles));

        return this;
    }

    /**
     * Adds metadata write roles.
     * @param roles
     */
    public StreamAcl addMetaWriteRoles(String... roles) {
        this.metaWriteRoles = this.metaWriteRoles == null ? new ArrayList<>() : this.metaWriteRoles;
        this.metaWriteRoles.addAll(Arrays.asList(roles));

        return this;
    }

    private static void serializeRoles(HashMap<String, Object> output, String key, ArrayList<String> target) {
        if (target == null)
            return;

        if (target.size() == 1) {
            output.put(key, target.get(0));
        } else {
            output.put(key, target);
        }
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<String> deserializeRoles(HashMap<String, Object> source, String key) {
        ArrayList<String> list = null;
        Object value = source.get(key);

        if (value != null) {
            list = new ArrayList<>();

            if (value instanceof String) {
                list.add((String) value);
            } else if (value instanceof ArrayList) {
                list.addAll((ArrayList<String>) value);
            } else {
                throw new RuntimeException("Unsupported role type: " + value.getClass());
            }
        }

        return list;
    }

    @Override
    public Object serialize() {
        HashMap<String, Object> output = new HashMap<>();
        serializeRoles(output, "$r", this.readRoles);
        serializeRoles(output, "$w", this.writeRoles);
        serializeRoles(output, "$d", this.deleteRoles);
        serializeRoles(output, "$mr", this.metaReadRoles);
        serializeRoles(output, "$mw", this.metaWriteRoles);

        return output;
    }

     static StreamAcl deserialize(HashMap<String, Object> source) {
        StreamAcl acl = new StreamAcl();

        acl.readRoles = deserializeRoles(source, "$r");
        acl.writeRoles = deserializeRoles(source, "$w");
        acl.deleteRoles = deserializeRoles(source, "$d");
        acl.metaReadRoles = deserializeRoles(source, "$mr");
        acl.metaWriteRoles = deserializeRoles(source, "$mw");

        return acl;
    }

    /**
     * Returns read roles.
     */
    public ArrayList<String> getReadRoles() {
        return readRoles;
    }

    /**
     * Returns write roles.
     */
    public ArrayList<String> getWriteRoles() {
        return writeRoles;
    }

    /**
     * Returns delete roles.
     */
    public ArrayList<String> getDeleteRoles() {
        return deleteRoles;
    }

    /**
     * Return metadata read roles.
     */
    public ArrayList<String> getMetaReadRoles() {
        return metaReadRoles;
    }

    /**
     * Return metadata write roles.
     */
    public ArrayList<String> getMetaWriteRoles() {
        return metaWriteRoles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamAcl streamAcl = (StreamAcl) o;
        return Objects.equals(readRoles, streamAcl.readRoles) && Objects.equals(writeRoles, streamAcl.writeRoles) && Objects.equals(deleteRoles, streamAcl.deleteRoles) && Objects.equals(metaReadRoles, streamAcl.metaReadRoles) && Objects.equals(metaWriteRoles, streamAcl.metaWriteRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readRoles, writeRoles, deleteRoles, metaReadRoles, metaWriteRoles);
    }
}
