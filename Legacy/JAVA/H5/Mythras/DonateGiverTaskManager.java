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
package l2f.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import l2f.commons.threading.RunnableImpl;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.database.DatabaseFactory;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.utils.ItemFunctions;

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
	
	protected DonateGiverTaskManager()
	{
		_log.info("DonateGiver: started.");
		
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl()
			{
				Logger __log = Logger.getLogger(DonateGiverTaskManager.class.getName());
				String charName = null;
				int id = 0;
				int count = 0;
				String playerName = null;
				try (Connection con = DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement = con.prepareStatement("SELECT id, count, playername FROM donate_holder WHERE order_status=1;"))
				{
					try (ResultSet rset = statement.executeQuery())
					{
						while (rset.next())
						{
							id = rset.getInt("id");
							count = rset.getInt("count");
							playerName = rset.getString("playername");
							if (id > 0 && count > 0 && playerName != null)
							{
								int obj_id = selectPlayer(playerName);
								for (Player activeChar : World.getPlayer(obj_id))
								{
									if (activeChar == null || activeChar.isOnline() == false)
									{
										continue;
									}
									if (activeChar.getName().toLowerCase().equals(playerName.toLowerCase()))
									{
										charName = activeChar.getName();
										ItemFunctions.addItem(activeChar, id, count, true, "Donate");
										activeChar.getInventory().store();
										activeChar.sendItemList(false);
										activeChar.sendMessage("Received "+ count +" donate coins.");
										RemoveDonation(charName);
										activeChar.sendActionFailed();
									}
								}
							}
						}
					}
					catch (Exception e)
					{
						__log.warning("Donate rewarder fail: for character: " + charName + " " + count + " Donate Coins! " + e.getMessage());
					}
				}
				catch (Exception e)
				{
					__log.warning("Check donate items failed. " + e.getMessage());
				}
			}
		}, 5000L, 5000L);
	}
	
	/**
	 * @param playername
	 */
	private static void RemoveDonation(String playername)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM donate_holder WHERE playername=?;"))
		{
			statement.setString(1, playername);
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.warning("Failed to remove donation from database char: " + playername);
			_log.warning(e.getMessage());
		}
	}
	
	private static int selectPlayer(String playername)
	{
		int charId=0;
		try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM `characters` WHERE `char_name`=? LIMIT 1;"))
		{
			statement.setString(1, playername);
			try (ResultSet rset3 = statement.executeQuery())
			{
				while (rset3.next())
				{
					charId = rset3.getInt("obj_Id");
				}
			}
		}
		catch (SQLException e)
		{
			_log.warning("Failed to remove donation from database char: " + playername);
			_log.warning(e.getMessage());
		}
		return charId;
	}
}