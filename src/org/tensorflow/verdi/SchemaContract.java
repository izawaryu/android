package org.tensorflow.verdi;

import android.provider.BaseColumns;

/**
 * Copyright (c) 2017 Ryu Izawa. All Rights Reserved.
 */

public final class SchemaContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SchemaContract() {}

    // Inner class that defines the table contents
    public static class SchemaCollection implements BaseColumns {
        public static final String TABLE_NAME = "tbl_Collection";
        public static final String COLUMN_NAME_0 = "OnDate";
        public static final String COLUMN_NAME_1 = "ByUser";
        public static final String COLUMN_NAME_2 = "Latitude";
        public static final String COLUMN_NAME_3 = "Longitude";
        public static final String COLUMN_NAME_4 = "Species";
        public static final String COLUMN_NAME_5 = "Genus";

        public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY, " +
            COLUMN_NAME_0 + " TEXT, " +
            COLUMN_NAME_1 + " TEXT, " +
            COLUMN_NAME_2 + " TEXT, " +
            COLUMN_NAME_3 + " TEXT, " +
            COLUMN_NAME_4 + " TEXT, " +
            COLUMN_NAME_5 + " TEXT );";

        public static final String DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }
}