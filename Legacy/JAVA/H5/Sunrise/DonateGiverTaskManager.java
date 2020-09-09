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
package l2r.gameserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import l2r.L2DatabaseFactory;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.InventoryUpdate;

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
		
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			Logger __log = Logger.getLogger(DonateGiverTaskManager.class.getName());
			String charName = null;
			int id = 0;
			int count = 0;
			String playerName = null;
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT id, count, playername FROM donate_holder WHERE order_status=1;"))
			{
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						id = rset.getInt("id");
						count = rset.getInt("count");
						playerName = rset.getString("playername");
						if ((id > 0) && (count > 0) && (playerName != null))
						{
							int obj_id = selectPlayer(playerName);
							for (L2PcInstance activeChar : L2World.getInstance().getPlayers())
							{
								if ((activeChar == null) || (activeChar.isOnline() == false))
								{
									continue;
								}
								if (activeChar.getObjectId() == obj_id)
								{
									if (activeChar.getName().toLowerCase().equals(playerName.toLowerCase()))
									{
										charName = activeChar.getName();
										activeChar.getInventory().addItem("Donate", id, count, activeChar, null);
										activeChar.sendItemList(false);
										activeChar.sendMessage("Received " + count + " donate coins.");
										RemoveDonation(charName);
										
										InventoryUpdate iu = new InventoryUpdate();
										activeChar.sendInventoryUpdate(iu);
										
										activeChar.sendPacket(ActionFailed.STATIC_PACKET);
									}
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
		}, 5000L);
	}
	
	/**
	 * @param playername
	 */
	private static void RemoveDonation(String playername)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
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
		int charId = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `characters` WHERE `char_name`=? LIMIT 1;"))
		{
			statement.setString(1, playername);
			try (ResultSet rset3 = statement.executeQuery())
			{
				while (rset3.next())
				{
					charId = rset3.getInt("charId");
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