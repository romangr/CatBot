/*
 * This file is generated by jOOQ.
 */
package ru.romangr.catbot.tables;


import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import ru.romangr.catbot.DefaultSchema;
import ru.romangr.catbot.Keys;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Migrations extends TableImpl<Record> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>MIGRATIONS</code>
     */
    public static final Migrations MIGRATIONS = new Migrations();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>MIGRATIONS.name</code>.
     */
    public final TableField<Record, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(50).nullable(false), this, "");

    private Migrations(Name alias, Table<Record> aliased) {
        this(alias, aliased, null);
    }

    private Migrations(Name alias, Table<Record> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>MIGRATIONS</code> table reference
     */
    public Migrations(String alias) {
        this(DSL.name(alias), MIGRATIONS);
    }

    /**
     * Create an aliased <code>MIGRATIONS</code> table reference
     */
    public Migrations(Name alias) {
        this(alias, MIGRATIONS);
    }

    /**
     * Create a <code>MIGRATIONS</code> table reference
     */
    public Migrations() {
        this(DSL.name("MIGRATIONS"), null);
    }

    public <O extends Record> Migrations(Table<O> child, ForeignKey<O, Record> key) {
        super(child, key, MIGRATIONS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    public UniqueKey<Record> getPrimaryKey() {
        return Keys.MIGRATIONS__;
    }

    @Override
    public Migrations as(String alias) {
        return new Migrations(DSL.name(alias), this);
    }

    @Override
    public Migrations as(Name alias) {
        return new Migrations(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Migrations rename(String name) {
        return new Migrations(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Migrations rename(Name name) {
        return new Migrations(name, null);
    }
}