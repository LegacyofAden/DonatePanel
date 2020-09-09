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
package l2f.gameserver.handler.voicecommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import l2f.commons.dbutils.DbUtils;
import l2f.gameserver.database.DatabaseFactory;
import l2f.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.scripts.Functions;
import l2f.gameserver.utils.ItemFunctions;

public class Check extends Functions implements IVoicedCommandHandler
{
	// for manual players use .check in order to get their items
	// dont forget to register the handler to IVoicedCommandHandler
	private static Logger _log = Logger.getLogger(Check.class.getName());
	private static final String[] COMMANDS =
	{
		"check"
	};

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(activeChar == null)
			return false;
		if (command.startsWith("check"))
		{
			int no = 0;
			int id = 0;
			int count = 0;
			String playerName = "";
			Connection con = null;
			PreparedStatement statement = null;
			try 
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT no, id, count, playername FROM donate_holder WHERE playername=? AND order_status=1 LIMIT 1;");
				statement.setString(1, activeChar.getName());
				ResultSet rset = statement.executeQuery();
				if (rset.next())
				{
					no = rset.getInt("no");
					id = rset.getInt("id");
					count = rset.getInt("count");
					playerName = rset.getString("playername");
					for (Player player : World.getPlayer(activeChar.getName()))
					{
						if (player == null || player.isOnline() == false)
						{
							continue;
						}
						if (player.getName().toLowerCase().equals(playerName.toLowerCase()))
						{
							ItemFunctions.addItem(player, id, count, true, "Donate");
							player.getInventory().store();
							player.sendItemList(false);
							player.sendMessage("Received donation coins.");
							RemoveDonation(no);
							player.sendActionFailed();
						}
					}
					return true;
				}
				else activeChar.sendMessage("We could not find your donation.");
			}
			catch (Exception e)
			{
				_log.warning("Check donate items failed. " + e.getMessage());
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		return true;
	}
	
	private static void RemoveDonation(int no)
	{
		Connection con = null;
		PreparedStatement statement = null;
		
		try 
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM donate_holder WHERE no=? LIMIT 1;");
			statement.setInt(1, no);
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			_log.warning("Failed to remove donation from database no: " + no);
			_log.warning(e.getMessage());
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}