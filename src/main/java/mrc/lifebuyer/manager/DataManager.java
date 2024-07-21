package mrc.lifebuyer.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DataManager {
    private Connection connection;

    private final String databaseName;

    public DataManager(String databaseName) {
        this.databaseName = databaseName;
    }

    public void connect() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.databaseName + ".db");
        createTable("player_data_level", "player_id TEXT, shop_name TEXT, level INTEGER DEFAULT 1");
        createTable("player_data_count", "player_id TEXT, shop_name TEXT, count INTEGER DEFAULT 0");
        createTable("player_data_material", "player_id TEXT, shop_name TEXT, material TEXT, count INTEGER DEFAULT 0");
    }

    public void disconnect() throws SQLException {
        if (this.connection != null)
            this.connection.close();
    }

    public void createTable(String tableName, String columns) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columns + ")";
        PreparedStatement statement = this.connection.prepareStatement(query);
        try {
            statement.execute();
            if (statement != null)
                statement.close();
        } catch (Throwable throwable) {
            if (statement != null)
                try {
                    statement.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    private String getHash(String input) {
        return Integer.toString(input.hashCode());
    }

    public int getPlayerLevel(UUID playerId, String shopName) throws SQLException {
        String query = "SELECT level FROM player_data_level WHERE player_id = ? AND shop_name = ?";
        PreparedStatement statement = this.connection.prepareStatement(query);
        try {
            statement.setString(1, getHash(playerId.toString()));
            statement.setString(2, getHash(shopName));
            ResultSet resultSet = statement.executeQuery();
            try {
                if (resultSet.next()) {
                    int i = resultSet.getInt("level");
                    if (resultSet != null)
                        resultSet.close();
                    if (statement != null)
                        statement.close();
                    return i;
                }
                if (resultSet != null)
                    resultSet.close();
            } catch (Throwable throwable) {
                if (resultSet != null)
                    try {
                        resultSet.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
            if (statement != null)
                statement.close();
        } catch (Throwable throwable) {
            if (statement != null)
                try {
                    statement.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
        return 1;
    }

    public void setPlayerLevel(UUID playerId, String shopName, int level) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM player_data_level WHERE player_id = ? AND shop_name = ?";
        String insertQuery = "INSERT INTO player_data_level (player_id, shop_name, level) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE player_data_level SET level = ? WHERE player_id = ? AND shop_name = ?";
        PreparedStatement selectStatement = this.connection.prepareStatement(selectQuery);
        try {
            PreparedStatement insertStatement = this.connection.prepareStatement(insertQuery);
            try {
                PreparedStatement updateStatement = this.connection.prepareStatement(updateQuery);
                try {
                    selectStatement.setString(1, getHash(playerId.toString()));
                    selectStatement.setString(2, getHash(shopName));
                    ResultSet resultSet = selectStatement.executeQuery();
                    if (resultSet.next()) {
                        int rowCount = resultSet.getInt(1);
                        if (rowCount > 0) {
                            updateStatement.setInt(1, level);
                            updateStatement.setString(2, getHash(playerId.toString()));
                            updateStatement.setString(3, getHash(shopName));
                            updateStatement.executeUpdate();
                        } else {
                            insertStatement.setString(1, getHash(playerId.toString()));
                            insertStatement.setString(2, getHash(shopName));
                            insertStatement.setInt(3, level);
                            insertStatement.executeUpdate();
                        }
                    }
                    if (updateStatement != null)
                        updateStatement.close();
                } catch (Throwable throwable) {
                    if (updateStatement != null)
                        try {
                            updateStatement.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (insertStatement != null)
                    insertStatement.close();
            } catch (Throwable throwable) {
                if (insertStatement != null)
                    try {
                        insertStatement.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
            if (selectStatement != null)
                selectStatement.close();
        } catch (Throwable throwable) {
            if (selectStatement != null)
                try {
                    selectStatement.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public int getPlayerCount(UUID playerId, String shopName) throws SQLException {
        String query = "SELECT count FROM player_data_count WHERE player_id = ? AND shop_name = ?";
        PreparedStatement statement = this.connection.prepareStatement(query);
        try {
            statement.setString(1, getHash(playerId.toString()));
            statement.setString(2, getHash(shopName));
            ResultSet resultSet = statement.executeQuery();
            try {
                if (resultSet.next()) {
                    int i = resultSet.getInt("count");
                    if (resultSet != null)
                        resultSet.close();
                    if (statement != null)
                        statement.close();
                    return i;
                }
                if (resultSet != null)
                    resultSet.close();
            } catch (Throwable throwable) {
                if (resultSet != null)
                    try {
                        resultSet.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
            if (statement != null)
                statement.close();
        } catch (Throwable throwable) {
            if (statement != null)
                try {
                    statement.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
        return 0;
    }

    public void setPlayerCount(UUID playerId, String shopName, int count) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM player_data_count WHERE player_id = ? AND shop_name = ?";
        String insertQuery = "INSERT INTO player_data_count (player_id, shop_name, count) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE player_data_count SET count = ? WHERE player_id = ? AND shop_name = ?";
        PreparedStatement selectStatement = this.connection.prepareStatement(selectQuery);
        try {
            PreparedStatement insertStatement = this.connection.prepareStatement(insertQuery);
            try {
                PreparedStatement updateStatement = this.connection.prepareStatement(updateQuery);
                try {
                    selectStatement.setString(1, getHash(playerId.toString()));
                    selectStatement.setString(2, getHash(shopName));
                    ResultSet resultSet = selectStatement.executeQuery();
                    if (resultSet.next()) {
                        int rowCount = resultSet.getInt(1);
                        if (rowCount > 0) {
                            updateStatement.setInt(1, count);
                            updateStatement.setString(2, getHash(playerId.toString()));
                            updateStatement.setString(3, getHash(shopName));
                            updateStatement.executeUpdate();
                        } else {
                            insertStatement.setString(1, getHash(playerId.toString()));
                            insertStatement.setString(2, getHash(shopName));
                            insertStatement.setInt(3, count);
                            insertStatement.executeUpdate();
                        }
                    }
                    if (updateStatement != null)
                        updateStatement.close();
                } catch (Throwable throwable) {
                    if (updateStatement != null)
                        try {
                            updateStatement.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (insertStatement != null)
                    insertStatement.close();
            } catch (Throwable throwable) {
                if (insertStatement != null)
                    try {
                        insertStatement.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
            if (selectStatement != null)
                selectStatement.close();
        } catch (Throwable throwable) {
            if (selectStatement != null)
                try {
                    selectStatement.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public int getPlayerMaterial(UUID playerId, String shopName, String material) throws SQLException {
        String query = "SELECT count FROM player_data_material WHERE player_id = ? AND shop_name = ? AND material = ?";
        PreparedStatement statement = this.connection.prepareStatement(query);
        try {
            statement.setString(1, getHash(playerId.toString()));
            statement.setString(2, getHash(shopName));
            statement.setString(3, getHash(material));
            ResultSet resultSet = statement.executeQuery();
            try {
                if (resultSet.next()) {
                    int i = resultSet.getInt("count");
                    if (resultSet != null)
                        resultSet.close();
                    if (statement != null)
                        statement.close();
                    return i;
                }
                if (resultSet != null)
                    resultSet.close();
            } catch (Throwable throwable) {
                if (resultSet != null)
                    try {
                        resultSet.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
            if (statement != null)
                statement.close();
        } catch (Throwable throwable) {
            if (statement != null)
                try {
                    statement.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
        return 0;
    }

    public void setPlayerMaterial(UUID playerId, String shopName, String material, int count) throws SQLException {
        String selectQuery = "SELECT COUNT(*) AS count FROM player_data_material WHERE player_id = ? AND shop_name = ? AND material = ?";
        PreparedStatement selectStatement = this.connection.prepareStatement(selectQuery);
        try {
            selectStatement.setString(1, getHash(playerId.toString()));
            selectStatement.setString(2, getHash(shopName));
            selectStatement.setString(3, getHash(material));
            ResultSet resultSet = selectStatement.executeQuery();
            try {
                if (resultSet.next()) {
                    int existingCount = resultSet.getInt("count");
                    if (existingCount > 0) {
                        String updateQuery = "UPDATE player_data_material SET count = ? WHERE player_id = ? AND shop_name = ? AND material = ?";
                        PreparedStatement updateStatement = this.connection.prepareStatement(updateQuery);
                        try {
                            updateStatement.setInt(1, count);
                            updateStatement.setString(2, getHash(playerId.toString()));
                            updateStatement.setString(3, getHash(shopName));
                            updateStatement.setString(4, getHash(material));
                            updateStatement.executeUpdate();
                            if (updateStatement != null)
                                updateStatement.close();
                        } catch (Throwable throwable) {
                            if (updateStatement != null)
                                try {
                                    updateStatement.close();
                                } catch (Throwable throwable1) {
                                    throwable.addSuppressed(throwable1);
                                }
                            throw throwable;
                        }
                        if (resultSet != null)
                            resultSet.close();
                        if (selectStatement != null)
                            selectStatement.close();
                        return;
                    }
                }
                if (resultSet != null)
                    resultSet.close();
            } catch (Throwable throwable) {
                if (resultSet != null)
                    try {
                        resultSet.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
            if (selectStatement != null)
                selectStatement.close();
        } catch (Throwable throwable) {
            if (selectStatement != null)
                try {
                    selectStatement.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
        String insertQuery = "INSERT INTO player_data_material (player_id, shop_name, material, count) VALUES (?, ?, ?, ?)";
        PreparedStatement insertStatement = this.connection.prepareStatement(insertQuery);
        try {
            insertStatement.setString(1, getHash(playerId.toString()));
            insertStatement.setString(2, getHash(shopName));
            insertStatement.setString(3, getHash(material));
            insertStatement.setInt(4, count);
            insertStatement.executeUpdate();
            if (insertStatement != null)
                insertStatement.close();
        } catch (Throwable throwable) {
            if (insertStatement != null)
                try {
                    insertStatement.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
            throw throwable;
        }
    }

    public void clearPlayerData(UUID playerId, String shopName) throws SQLException {
        String deleteQuery = "DELETE FROM player_data_material WHERE player_id = ? AND shop_name = ?";
        try {
            PreparedStatement deleteStatement = this.connection.prepareStatement(deleteQuery);
            try {
                deleteStatement.setString(1, getHash(playerId.toString()));
                deleteStatement.setString(2, getHash(shopName));
                deleteStatement.executeUpdate();
                if (deleteStatement != null)
                    deleteStatement.close();
            } catch (Throwable throwable) {
                if (deleteStatement != null)
                    try {
                        deleteStatement.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
