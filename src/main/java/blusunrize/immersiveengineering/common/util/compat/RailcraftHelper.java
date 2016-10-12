package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.client.IEManualInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RailcraftHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		Item itemRail = Item.REGISTRY.getObject(new ResourceLocation("railcraft:rail"));
		RailgunHandler.registerProjectileProperties(new ItemStack(itemRail,1,0), 7,1.25).setColourMap(new int[][]{{0xa4a4a4,0x686868}});
		RailgunHandler.registerProjectileProperties(new ItemStack(itemRail,1,1), 6,1.375).setColourMap(new int[][]{{0xa4a4a4,0xa4a4a4,0x686868, 0xddb82c,0xc9901f}, {0xa4a4a4,0xa4a4a4,0x686868, 0xf5cc2d,0xddb82c},{0xa4a4a4,0xa4a4a4,0x686868, 0xf5cc2d,0xddb82c}, {0xa4a4a4,0xa4a4a4,0x686868, 0xddb82c,0xc9901f}});
		RailgunHandler.registerProjectileProperties(new ItemStack(itemRail,1,3), 7,1).setColourMap(new int[][]{{0x999999,0xa4a4a4,0xa4a4a4, 0xc9901f,0xc9901f,0xba851d}});
		RailgunHandler.registerProjectileProperties(new ItemStack(itemRail,1,4), 8,1.375).setColourMap(new int[][]{{0x686868,0x808080,0x808080, 0x3e2e60,0x3e2e60,0x31254d}});
		RailgunHandler.registerProjectileProperties(new ItemStack(itemRail,1,5), 7,1).setColourMap(new int[][]{{0x999999,0xa4a4a4,0xa4a4a4, 0x9a6033,0x9a6033,0xa86938}});

		Item itemRebar = Item.REGISTRY.getObject(new ResourceLocation("railcraft:rebar"));
		int[][] rebarColourMap = new int[8*3+1][];
		rebarColourMap[0] = new int[]{0x4a2700, 0x592f00,0x592f00,0x592f00, 0x4a2700};
		rebarColourMap[1] = new int[]{0x572e00, 0x673700,0x673700,0x673700, 0x572e00};
		for(int i=0; i<8; i++)
		{
			rebarColourMap[1+ i*3] = rebarColourMap[1+ i*3+1] = rebarColourMap[1];
			rebarColourMap[1+ i*3+2] = rebarColourMap[0];
		}
		RailgunHandler.registerProjectileProperties(new ItemStack(itemRebar), 7,1.25).setColourMap(rebarColourMap);
		IEManualInstance.config_bool.put("literalRailGun", true);
	}

	@Override
	public void postInit()
	{
	}
}