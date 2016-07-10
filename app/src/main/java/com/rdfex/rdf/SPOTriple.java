package com.rdfex.rdf;

/**
 * Created by Dell on 10-07-2016.
 */
public class SPOTriple implements Triple {
    private final String obj;
    private final String sub;
    private final String pre;

    public SPOTriple(String s, String p, String o) {
        this.sub = s;
        this.pre = p;
        this.obj = o;
    }

    @Override
    public String getSubject() {
        return sub;
    }

    @Override
    public String getPredicate() {
        return pre;
    }

    @Override
    public String getObject() {
        return obj.replaceAll("_", " ");
    }

    @Override
    public String toString() {
        return String.format("<%s,%s,%s>", sub, pre, obj);
    }
}
