package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

/**
 * @Author Nightwolf
 * Adapted by LordWinter for l2jeternity
 *
 * https://l2jeternity.com/
 */
public class DonateGiverTaskManager
{
	private static Logger _log = Logger.getLogger(DonateGiverTaskManager.class.getName());
	
	public static DonateGiverTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DonateGiverTaskManager _instance = new DonateGiverTaskManager();
	}
	
	public DonateGiverTaskManager()
	{
		_log.info("DonateGiver: started.");
		
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
		{
			final Logger __log = Logger.getLogger(DonateGiverTaskManager.class.getName());
			final String charName = null;
			int no = 0;
			int id = 0;
			int count = 0;
			String playerName = null;
			try (
		        Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT no, id, count, playername FROM donate_holder WHERE order_status=1;"))
			{
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						no = rset.getInt("no");
						id = rset.getInt("id");
						count = rset.getInt("count");
						playerName = rset.getString("playername");
						if ((id > 0) && (count > 0) && (playerName != null))
						{
							final Player player = World.getInstance().getPlayer(playerName);
							if (player != null)
							{
								for (final Player activeChar : World.getInstance().getAllPlayers())
								{
									if ((activeChar == null) || (activeChar.isOnline() == false))
									{
										continue;
									}
									if (activeChar.getObjectId() == player.getObjectId())
									{
										if (activeChar.getName().toLowerCase().equals(playerName.toLowerCase()))
										{
											activeChar.addItem("Donate", id, count, activeChar, true);
											activeChar.sendMessage("Received " + count + " donate coins.");
											RemoveDonation(no);
										}
									}
								}
							}
						}
					}
				}
				catch (final Exception e)
				{
					__log.warning("Donate rewarder fail: for character: " + charName + " " + count + " Donate Coins! " + e.getMessage());
				}
			}
			catch (final Exception e)
			{
				__log.warning("Check donate items failed. " + e.getMessage());
			}
		}, 5000L, 5000L);
	}
	
	private static void RemoveDonation(int no)
	{
		try (
		    Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM donate_holder WHERE no=?;"))
		{
			statement.setInt(1, no);
			statement.execute();
		}
		catch (final SQLException e)
		{
			_log.warning("Failed to remove donation from database with id:" + no);
			_log.warning(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		new DonateGiverTaskManager();
	}
}