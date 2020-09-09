/**
 * Author Nightwolf
 * bugs contact:
 * Email Nightw0lv@hotmail.com
 * Skype nightwolf.black
 * Created for Denart Designs that holds the ownership of this files
 * You are allowed to edit this code but you are not allowed to sell this code or parts of this code under any sircuimstances.
 * buy this from https://shop.denart-designs.com/ get updates latest news and support.
 * Do not remove this, or any credits in order to ask for support.
 * Damn we created that think and changing a line or remove the authors credits does not make you author
 * plus its not helping us to improve it and give you updates..
 */
package extensions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;
import net.l2jpx.gameserver.model.L2World;
import net.l2jpx.gameserver.model.actor.instance.L2PcInstance;
import net.l2jpx.gameserver.network.serverpackets.ActionFailed;
import net.l2jpx.gameserver.network.serverpackets.ItemList;
import net.l2jpx.gameserver.thread.ThreadPoolManager;
import net.l2jpx.util.CloseUtil;
import net.l2jpx.util.database.DatabaseUtils;
import net.l2jpx.util.database.L2DatabaseFactory;

public class DonateGiverTaskManager
{
	static final Logger LOGGER = Logger.getLogger(DonateGiverTaskManager.class);
	@SuppressWarnings("unused")
	private ScheduledFuture<?> _autoCheck;
	
	public static DonateGiverTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DonateGiverTaskManager _instance = new DonateGiverTaskManager();
	}
	
	protected DonateGiverTaskManager()
	{
		_autoCheck = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckTask(), 5000, 5000);
		LOGGER.info("DonateGiver: started.");
	}
	
	protected class CheckTask implements Runnable
	{
		@Override
		public void run()
		{
			int no = 0;
			int id = 0;
			int count = 0;
			String playerName = "";
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection(false);
				
				final PreparedStatement statement = con.prepareStatement("SELECT no, id, count, playername FROM donate_holder WHERE order_status=1;");
				final ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					no = rset.getInt("no");
					id = rset.getInt("id");
					count = rset.getInt("count");
					playerName = rset.getString("playername");
					if (id > 0 && count > 0 && playerName != "")
					{
						for (L2PcInstance activeChar : L2World.getInstance().getAllPlayers())
						{
							if (activeChar == null || activeChar.isOnline() == false)
							{
								continue;
							}
							if (activeChar.getName().toLowerCase().equals(playerName.toLowerCase()))
							{
								activeChar.getInventory().addItem("Donate", id, count, activeChar, null);
								activeChar.getInventory().updateDatabase();
								activeChar.sendPacket(new ItemList(activeChar, true));
								activeChar.sendMessage("Received donation coins.");
								RemoveDonation(no);
								activeChar.sendPacket(ActionFailed.STATIC_PACKET);
							}
						}
					}
				}
				DatabaseUtils.close(rset);
				DatabaseUtils.close(statement);
			}
			catch (final SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				CloseUtil.close(con);
				con = null;
			}
			
			return;
		}
	}
	
	/**
	 * @param no
	 */
	static void RemoveDonation(int no)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(false);
			statement = con.prepareStatement("DELETE FROM donate_holder WHERE no=? LIMIT 1;");
			statement.setInt(1, no);
			statement.execute();
			DatabaseUtils.close(statement);
		}
		catch (final Exception e)
		{
			LOGGER.error("Failed to remove donation from database no: " + no);
		}
		finally
		{
			CloseUtil.close(con);
		}
	}
}