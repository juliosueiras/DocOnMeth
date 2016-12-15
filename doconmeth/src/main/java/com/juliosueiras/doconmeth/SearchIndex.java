package com.juliosueiras.doconmeth;


import com.orm.SugarRecord;

/**
 * Search index model (using Sugar ORM for management)
 */
public class SearchIndex extends SugarRecord {
    String name;
    String type;
    String path;
    String docType;

    public SearchIndex() {
    }

    public SearchIndex(String name, String type, String path, String docType) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.docType = docType;
    }
}
