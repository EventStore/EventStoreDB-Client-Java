package com.eventstore.dbclient;

import java.util.*;

public class StreamAcl implements Acl {
    private ArrayList<String> readRoles;
    private ArrayList<String> writeRoles;
    private ArrayList<String> deleteRoles;
    private ArrayList<String> metaReadRoles;
    private ArrayList<String> metaWriteRoles;

    public StreamAcl addReadRoles(String... roles) {
        this.readRoles = this.readRoles == null ? new ArrayList<>() : this.readRoles;
        this.readRoles.addAll(Arrays.asList(roles));

        return this;
    }

    public StreamAcl addWriteRoles(String... roles) {
        this.writeRoles = this.writeRoles == null ? new ArrayList<>() : this.writeRoles;
        this.writeRoles.addAll(Arrays.asList(roles));

        return this;
    }

    public StreamAcl addDeleteRoles(String... roles) {
        this.deleteRoles = this.deleteRoles == null ? new ArrayList<>() : this.deleteRoles;
        this.deleteRoles.addAll(Arrays.asList(roles));

        return this;
    }

    public StreamAcl addMetaReadRoles(String... roles) {
        this.metaReadRoles = this.metaReadRoles == null ? new ArrayList<>() : this.metaReadRoles;
        this.metaReadRoles.addAll(Arrays.asList(roles));

        return this;
    }

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

    public static StreamAcl deserialize(HashMap<String, Object> source) {
        StreamAcl acl = new StreamAcl();

        acl.readRoles = deserializeRoles(source, "$r");
        acl.writeRoles = deserializeRoles(source, "$w");
        acl.deleteRoles = deserializeRoles(source, "$d");
        acl.metaReadRoles = deserializeRoles(source, "$mr");
        acl.metaWriteRoles = deserializeRoles(source, "$mw");

        return acl;
    }
    public ArrayList<String> getReadRoles() {
        return readRoles;
    }

    public ArrayList<String> getWriteRoles() {
        return writeRoles;
    }

    public ArrayList<String> getDeleteRoles() {
        return deleteRoles;
    }

    public ArrayList<String> getMetaReadRoles() {
        return metaReadRoles;
    }

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
