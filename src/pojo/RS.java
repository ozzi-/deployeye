package pojo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import helpers.Log;
import service.Scheduler;

public class RS {
	
	private static List<RS> resultSetList = Collections.synchronizedList(new ArrayList<RS>());
	private ResultSet rs;
	private Connection con;
	private boolean open;
	private long created;
	private String query;

	public RS(ResultSet rs, Connection con, String query) {
		this.query = query;
		this.setRs(rs);
		this.setCon(con);
		this.open = true;
		this.created = System.currentTimeMillis();
		resultSetList.add(this);
	}

	public int getLengthOfRS() {
		int rowCount = 0;
		if (!open) {
			return -1;
		}
		try {
			while (rs.next()) {
				rowCount++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rowCount;
	}

	public ResultSet getRs() {
		return rs;
	}

	public void setRs(ResultSet rs) {
		this.rs = rs;
	}

	public Connection getCon() {
		return con;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public void close() throws SQLException {
		if (open) {
			rs.close();
			con.close();
			open = false;
			removeFromOpenList(this);
		}
	}

	private static void removeFromOpenList(RS rsToRemove) {
		resultSetList.remove(rsToRemove);
	}

	public long getCreated() {
		return created;
	}

	public static void cleanUpRS() {
		long rsMaxLifeTime = 5000;

		ArrayList<RS> toCloseList = getResultSetsThatNeedClosing(rsMaxLifeTime);
		int closedRSCount = closeResultSets(toCloseList);
		
		if (closedRSCount > 0) {
			Log.logWarning("Scheduler - Cleaned up " + closedRSCount + " unclosed DB result sets - this would otherwise lead to a memory leak and should not happen", Scheduler.class);
		}
	}

	private static int closeResultSets(ArrayList<RS> toCloseList) {
		int closedRSCount=0;
		for (RS rs : toCloseList) {
			try {
				Log.logWarning("Scheduler - Unclosed DB result set being forcibly closed - query used: "+rs.query, Scheduler.class);
				rs.close();
				closedRSCount++;
			} catch (Exception e) {
				Log.logException(e, Scheduler.class);
				e.printStackTrace();
			}
		}
		return closedRSCount;
	}

	private static ArrayList<RS> getResultSetsThatNeedClosing(long rsMaxLifeTime) {
		ArrayList<RS> toCloseList = new ArrayList<RS>();
		ListIterator<RS> iter = resultSetList.listIterator();
		while (iter.hasNext()) {
			RS rsIter = iter.next();
			long cur = System.currentTimeMillis();
			if (rsIter.getCreated() + rsMaxLifeTime < cur) {
				toCloseList.add(rsIter);
			}
		}
		return toCloseList;
	}
}