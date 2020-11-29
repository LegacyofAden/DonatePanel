package custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.database.DatabaseFactory;
import l2f.gameserver.model.AbsServerMail;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;

public class DonateManager
{
	private static Logger _log = Logger.getLogger(DonateManager.class.getName());

	public static DonateManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final DonateManager _instance = new DonateManager();
	}

	protected DonateManager()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(()-> start(), 5000, 5000);
		_log.info("DonateGiver: started.");
	}

	private static void start()
	{
		String charName = null;
		int no = 0;
		int id = 0;
		int count = 0;
		String playerName = "";
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT no, id, count, playername FROM donate_holder;"))
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
						Player Player = World.getPlayer(playerName);
						if (Player != null && Player.isOnline() == true)
						{
							for (Player activeChar : GameObjectsStorage.getAllPlayersForIterate())
							{
								if (activeChar == null || activeChar.isInStoreMode() || activeChar.isOnline() == false || activeChar.getNetConnection() == null)
								{
									continue;
								}
								if (activeChar.getName().toLowerCase().equals(playerName.toLowerCase()))
								{
									new DonationSuccessMail(activeChar, id, count);
									RemoveDonation(no);
									activeChar.sendActionFailed();
								}
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
		try (Connection con = DatabaseFactory.getInstance().getConnection();
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

	private static final class DonationSuccessMail extends AbsServerMail
	{
		public DonationSuccessMail(Player player,int id,int count)
		{
			super(player, count, count);
		}

		@Override
		protected void prepare()
		{
			_mail.setTopic("Automatic Donation Success!");
			_mail.setBody(" Thank you for your Donation! \\n Here are your Donator Coins, in case you need any admin support you can contact us on discord \\n Thank you!");
		}
	}
}