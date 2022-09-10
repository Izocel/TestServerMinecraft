package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;


import org.json.JSONArray;


public class UsersDao extends BaseDao {

    private Logger logger;

	public UsersDao () {
        super();
        this.tablename = "users";
        this.logger = Logger.getLogger("WJE:" + this.getClass().getName());
		this.creds = this.getCredentials();
	}

    public JSONArray findAllowed() {

        JSONArray results = new JSONArray();

        try {
            String sql = "SELECT * FROM " + this.tablename + " WHERE checked = 1";
            this.open();
            final PreparedStatement pstmt = this.connection.prepareStatement(sql);
            pstmt.executeQuery();

            final ResultSet resultSet = pstmt.getResultSet();
            results = this.toJsonArray(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            this.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

}
