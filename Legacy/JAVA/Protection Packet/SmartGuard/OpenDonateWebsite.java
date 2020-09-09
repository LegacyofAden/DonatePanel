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

import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

import ru.akumu.smartguard.core.network.packets.ISmartPacket;

public class OpenDonateWebsite extends L2GameServerPacket implements ISmartPacket
{
	private final String _url;

	public OpenDonateWebsite(String url)
	{
		_url = url;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFF);
		writeC(0x03);
		writeS(_url);
	}
}