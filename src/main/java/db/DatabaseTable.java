package db;

public abstract class DatabaseTable {

    protected final Database db;

    public DatabaseTable(Database db) {
        this.db = db;
    }

    public abstract void create();
}
