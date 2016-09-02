//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package vazkii.botania.api.item;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.eventhandler.Event;

public class TinyPotatoRenderEvent extends Event
{
	public final TileEntity tile;
	public final String name;
	public final double x;
	public final double y;
	public final double z;
	public final float partTicks;
	public final int destroyStage;

	public TinyPotatoRenderEvent(TileEntity tile, String name, double x, double y, double z, float partTicks, int destroyStage)
	{
		this.tile = tile;
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.partTicks = partTicks;
		this.destroyStage = destroyStage;
	}
}
