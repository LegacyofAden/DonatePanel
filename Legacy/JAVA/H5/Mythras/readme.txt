Some mythras packs have other versions if one not work try this code for DonateGiverTaskManager.java

gameserver:
ThreadPoolManager.getInstance().scheduleAtFixedRate(new DonateGiverTaskManager(), 5000L, 5000L);

and DonateGiverTaskManager:

package l2f.gameserver.taskmanager;

import l2f.commons.threading.RunnableImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import l2f.gameserver.database.DatabaseFactory;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.network.serverpackets.SystemMessage;
import l2f.gameserver.network.serverpackets.SystemMessage2;
import l2f.gameserver.network.serverpackets.components.ChatType;
import l2f.gameserver.utils.ItemFunctions;
import org.slf4j.LoggerFactory;

public class DonateGiverTaskManager extends RunnableImpl
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DonateGiverTaskManager.class);

    @Override
    public void runImpl() throws Exception
    {
        int obj_id = 0;
        int count = 0;
        String charName = null;
        int id = 0;
        String playerName = null;

        try (Connection con = DatabaseFactory.getInstance().getConnection())
        {
            try (PreparedStatement statement = con.prepareStatement("SELECT id, count, playername FROM donate_holder WHERE order_status=1;");
                 ResultSet rset = statement.executeQuery())
            {
                while (rset.next())
                {
                    id = rset.getInt("id");
                    count = rset.getInt("count");
                    playerName = rset.getString("playername");
                    Player activeChar;
                    if (id > 0 && count > 0 && playerName != null)
                    {
                        try
                        {
                            obj_id = selectPlayer(playerName);
                            if (obj_id == 0)
                                return;

                            activeChar = World.getPlayer(obj_id);//Player.restore(obj_id);
                            if (activeChar == null)
                                return;
                            if (activeChar.isOnline())
                            {
                                if (activeChar.getName().toLowerCase().equals(playerName.toLowerCase()))
                                {
                                    charName = activeChar.getName();
                                    ItemFunctions.addItem(activeChar, id, count, false, "Donate");
                                    activeChar.getInventory().store();
                                    activeChar.sendItemList(false);
									// if item function set to false you can notify the user with one of these
                                    // activeChar.sendPacket(SystemMessage2.obtainItems(id, count, 0));
                                    // activeChar.sendChatMessage(activeChar.getObjectId(), 2, activeChar.getName(), "Received " + count + " donate coins.");

                                    //activeChar.sendMessage("Received " + count + " Donator Coin(s).");
                                    //activeChar.sendMessage("Thank you for supporting our server!");

                                    //activeChar.sendPacket(SystemMessage2.obtainItems(id, count, 0));
                                    activeChar.sendChatMessage(activeChar.getObjectId(), 2, activeChar.getName(), "Received " + count + " Donator Coin(s).");
                                    activeChar.sendChatMessage(activeChar.getObjectId(), 2, activeChar.getName(), "Thank you for supporting our server!");
                                    RemoveDonation(charName);
                                    activeChar.sendActionFailed();
                                }
                            }
                        }
                        catch (Exception E)
                        {
                            E.printStackTrace();
                        }
                    }
                }
            }
            catch (Exception e)
            {
                LOG.warn("Check donate items failed. " + e.getMessage());
            }
        }
        catch (SQLException e)
        {
            LOG.error("Error while connecting to database!", e);
        }
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
            LOG.warn("Failed to remove donation from database char: " + playername);
            LOG.warn(e.getMessage());
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
            LOG.warn("Failed to remove donation from database char: " + playername);
            LOG.warn(e.getMessage());
        }
        return charId;
    }
}