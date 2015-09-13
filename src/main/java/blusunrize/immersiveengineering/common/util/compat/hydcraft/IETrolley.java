package blusunrize.immersiveengineering.common.util.compat.hydcraft;

import java.util.ArrayList;

import k4unl.minecraft.Hydraulicraft.api.IHarvesterTrolley;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "k4unl.minecraft.Hydraulicraft.api.IHarvesterTrolley", modid = "HydCraft")
public class IETrolley implements IHarvesterTrolley
{
	@Override
	@Optional.Method(modid = "HydCraft")
	public boolean canHarvest(World world, int x, int y, int z)
	{
		return world.getBlock(x, y, z).equals(IEContent.blockCrop) && world.getBlockMetadata(x, y, z)==((BlockIECrop)IEContent.blockCrop).getMaxMeta(world.getBlockMetadata(x, y, z));
	}

	@Override
	@Optional.Method(modid = "HydCraft")
	public boolean canPlant(World world, int x, int y, int z, ItemStack stack)
	{
		Block soil = world.getBlock(x, y-1, z);
		return (soil.canSustainPlant(world, x, y-1, z, ForgeDirection.UP, (IPlantable)IEContent.itemSeeds) && world.isAirBlock(x, y, z) && (soil.isFertile(world, x, y-1, z)));
	}

	@Override
	@Optional.Method(modid = "HydCraft")
	public Block getBlockForSeed(ItemStack stack)
	{
		return IEContent.blockCrop;
	}

	@Override
	@Optional.Method(modid = "HydCraft")
	public ArrayList<ItemStack> getHandlingSeeds()
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(new ItemStack(IEContent.itemSeeds));
		return ret;
	}

	@Override
	@Optional.Method(modid = "HydCraft")
	public String getName()
	{
		return "ieCrop";
	}

	@Override
	@Optional.Method(modid = "HydCraft")
	public int getPlantHeight(World world, int x, int y, int z)
	{
		return 2;
	}

	static ResourceLocation texture = new ResourceLocation("immersiveengineering:textures/models/hcTrolley.png");
	@Override
	@Optional.Method(modid = "HydCraft")
	public ResourceLocation getTexture()
	{
		return texture;
	}
}