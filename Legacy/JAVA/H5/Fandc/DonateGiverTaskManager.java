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
import java.util.logging.Logger;

import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.utils.ItemFunctions;
import l2f.loginserver.database.L2DatabaseFactory;

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
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> start(), 5000, 5000);
		_log.info("DonateGiver: started.");
	}
	
	private static void start()
	{
		String charName = null;
		int no = 0;
		int id = 0;
		int count = 0;
		String playerName = "";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
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
					if (id > 0 && count > 0 && playerName != "")
					{
						for (Player activeChar : World.getPlayer(playerName))
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
								activeChar.sendMessage("Received donation coins.");
								RemoveDonation(no);
								activeChar.sendActionFailed();
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				_log.warning("Donate rewarder fail: for character: " + charName + " " + count + " Donate Coins! " + e.getMessage());
			}
		}
		catch (Exception e)
		{
			_log.warning("Check donate items failed. " + e.getMessage());
		}
		return;
	}
	
	/**
	 * @param no
	 */
	private static void RemoveDonation(int no)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM donate_holder WHERE no=? LIMIT 1;"))
		{
			statement.setInt(1, no);
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.warning("Failed to remove donation from database char: " + no);
			_log.warning(e.getMessage());
		}
	}
}