package com.teusoft.lono.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.teusoft.lono.dao.Lono;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table LONO.
*/
public class LonoDao extends AbstractDao<Lono, Long> {

    public static final String TABLENAME = "LONO";

    /**
     * Properties of entity Lono.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Temperature = new Property(1, Integer.class, "temperature", false, "TEMPERATURE");
        public final static Property Humidity = new Property(2, Integer.class, "humidity", false, "HUMIDITY");
        public final static Property TimeStamp = new Property(3, Long.class, "timeStamp", false, "TIME_STAMP");
        public final static Property Channel = new Property(4, Integer.class, "channel", false, "CHANNEL");
        public final static Property Index = new Property(5, Integer.class, "index", false, "INDEX");
    };


    public LonoDao(DaoConfig config) {
        super(config);
    }
    
    public LonoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'LONO' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'TEMPERATURE' INTEGER," + // 1: temperature
                "'HUMIDITY' INTEGER," + // 2: humidity
                "'TIME_STAMP' INTEGER," + // 3: timeStamp
                "'CHANNEL' INTEGER," + // 4: channel
                "'INDEX' INTEGER);"); // 5: index
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'LONO'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Lono entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Integer temperature = entity.getTemperature();
        if (temperature != null) {
            stmt.bindLong(2, temperature);
        }
 
        Integer humidity = entity.getHumidity();
        if (humidity != null) {
            stmt.bindLong(3, humidity);
        }
 
        Long timeStamp = entity.getTimeStamp();
        if (timeStamp != null) {
            stmt.bindLong(4, timeStamp);
        }
 
        Integer channel = entity.getChannel();
        if (channel != null) {
            stmt.bindLong(5, channel);
        }
 
        Integer index = entity.getIndex();
        if (index != null) {
            stmt.bindLong(6, index);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Lono readEntity(Cursor cursor, int offset) {
        Lono entity = new Lono( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1), // temperature
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // humidity
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // timeStamp
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // channel
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5) // index
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Lono entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTemperature(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
        entity.setHumidity(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setTimeStamp(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setChannel(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setIndex(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Lono entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Lono entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
