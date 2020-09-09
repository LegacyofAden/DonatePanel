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
package com.l2scripts.sguard.network.packets;

import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

public class OpenURLPacket extends L2GameServerPacket implements IGuardPacket
{
	private final String url;
	public OpenURLPacket(String url)
	{
		this.url = url;
	}
	protected void writeImpl()
	{
		writeC(0xFF);
		writeC(0x03);
		writeS(this.url);
	}
}