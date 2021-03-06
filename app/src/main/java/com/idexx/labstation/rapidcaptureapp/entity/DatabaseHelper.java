package com.idexx.labstation.rapidcaptureapp.entity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mhansen on 4/23/2017.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper
{
    private static final List<Class<? extends AbstractEntity>> TABLE_CLASSES = new ArrayList<>();

    static
    {
        TABLE_CLASSES.add(GeneralSettings.class);
        TABLE_CLASSES.add(User.class);
    }

    private static final String DB_NAME = "RapidCapture";
    private static final int DB_VERSION = 1;
    private static DatabaseHelper instance;

    private Map<Class, Dao> daoMap = new HashMap<>();

    private DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context)
    {
        if(instance == null)
        {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource)
    {
        try
        {
            Log.i(getClass().getSimpleName(), "Creating database " + DB_NAME);
            for(Class<? extends AbstractEntity> clazz : TABLE_CLASSES)
            {
                Log.i(getClass().getSimpleName(), "Creating table for class " + clazz.getSimpleName());
                TableUtils.createTable(connectionSource, clazz);
            }
            //Create settings entry
            GeneralSettings generalSettings = new GeneralSettings();
            getDao(GeneralSettings.class).createIfNotExists(generalSettings);
        }
        catch (SQLException e)
        {
            Log.e(getClass().getSimpleName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        Log.i(getClass().getSimpleName(), "Upgrading database " + DB_NAME + " from version " + oldVersion + " to " + newVersion);
        rebuild(database);
    }


    @Override
    public void onDowngrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        Log.i(getClass().getSimpleName(), "Downgrading database " + DB_NAME + " from version " + oldVersion + " to " + newVersion);
        rebuild(database);
    }

    private void rebuild(SQLiteDatabase database)
    {
        try
        {
            Log.i(getClass().getSimpleName(), "Rebuilding database " + DB_NAME);
            for(Class<? extends AbstractEntity> clazz : TABLE_CLASSES)
            {
                Log.i(getClass().getSimpleName(), "Dropping table for class " + clazz.getSimpleName());
                TableUtils.dropTable(connectionSource, clazz, true);
            }
            onCreate(database, connectionSource);
        }
        catch (SQLException e)
        {
            Log.e(getClass().getSimpleName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    public <T1, T2> Dao<T1, T2> getDaoIfExists(Class<T1> entityClass)
    {
        if(daoMap.containsKey(entityClass))
        {
            return daoMap.get(entityClass);
        }
        try
        {
            Dao<T1, T2> dao = getDao(entityClass);
            daoMap.put(entityClass, dao);
            return dao;
        }
        catch (SQLException e)
        {
            Log.e(getClass().getSimpleName(), "Error creating DAO for " + entityClass.getSimpleName(), e);
            return null;
        }
    }

    @Override
    public void close()
    {
        super.close();
        daoMap.clear();
    }
}
