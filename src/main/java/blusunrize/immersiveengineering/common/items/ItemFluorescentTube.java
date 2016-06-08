package blusunrize.immersiveengineering.common.items;

import java.util.List;

import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigBoolean;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigFloat;
import blusunrize.immersiveengineering.common.entities.EntityFluorescentTube;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemFluorescentTube extends ItemIEBase implements IConfigurableTool
{

	public ItemFluorescentTube()
	{
		super("fluorescentTube", 1);
	}
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ)
	{
		if (side==EnumFacing.UP)
		{
			if (!world.isRemote)
			{
				Vec3 look = player.getLookVec();
				float angle = (float) Math.toDegrees(Math.atan2(look.xCoord, look.zCoord));
				EntityFluorescentTube tube = new EntityFluorescentTube(world, stack.copy(), angle);
				tube.setPosition(pos.getX()+hitX, pos.getY()+1.5, pos.getZ()+hitZ);
				world.spawnEntityInWorld(tube);
				stack.splitStack(1);
				if (stack.stackSize>0)
					player.setCurrentItemOrArmor(0, stack);
				else
					player.setCurrentItemOrArmor(0, null);
			}
			return true;
		}
		return super.onItemUse(stack, player, world, pos, side, hitX, hitY, hitZ);
	}
	public static float[] getRGB(ItemStack s)
	{
		if (ItemNBTHelper.hasKey(s, "rgb"))
		{
			NBTTagCompound nbt = ItemNBTHelper.getTagCompound(s, "rgb");
			return new float[]{nbt.getFloat("r"), nbt.getFloat("g"), nbt.getFloat("b")};
		}
		return new float[]{1, 1, 1};
	}
	public static void setRGB(ItemStack s, float[] rgb)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat("r", rgb[0]);
		nbt.setFloat("g", rgb[1]);
		nbt.setFloat("b", rgb[2]);
		ItemNBTHelper.setTagCompound(s, "rgb", nbt);
	}
	@Override
	public boolean canConfigure(ItemStack stack)
	{
		return true;
	}
	@Override
	public ToolConfigBoolean[] getBooleanOptions(ItemStack stack)
	{
		return new ToolConfigBoolean[0];
	}
	@Override
	public ToolConfigFloat[] getFloatOptions(ItemStack stack)
	{
		ToolConfigFloat[] ret = new ToolConfigFloat[3];
		float[] rgb = getRGB(stack);
		ret[0] = new ToolConfigFloat("red", 60, 20, rgb[0]);
		ret[1] = new ToolConfigFloat("green", 60, 40, rgb[1]);
		ret[2] = new ToolConfigFloat("blue", 60, 60, rgb[2]);
		return ret;
	}
	@Override
	public void applyConfigOption(ItemStack stack, String key, Object value)
	{
		int id = key.equals("red")?0:(key.equals("green")?1:2);
		float[] rgb = getRGB(stack);
		rgb[id] = (float) value;
		setRGB(stack, rgb);
	}
	@Override
	public String fomatConfigName(ItemStack stack, ToolConfig config)
	{
		return config.name;
	}
	@Override
	public String fomatConfigDescription(ItemStack stack, ToolConfig config)
	{
		return config.name;
	}
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
	{
		super.addInformation(stack, playerIn, tooltip, advanced);
		float[] rgb = getRGB(stack);
		tooltip.add(StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.colour.red", rgb[0]));
		tooltip.add(StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.colour.green", rgb[1]));
		tooltip.add(StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.colour.blue", rgb[2]));
	}
}
