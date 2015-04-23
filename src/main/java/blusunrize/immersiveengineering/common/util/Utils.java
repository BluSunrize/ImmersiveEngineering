package blusunrize.immersiveengineering.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.IImmersiveConnectable;
import cpw.mods.fml.common.registry.GameData;

public class Utils
{
	public static boolean compareToOreName(ItemStack stack, String oreName)
	{
		for(int oid : OreDictionary.getOreIDs(stack))
			if(OreDictionary.getOreName(oid).equals(oreName))
				return true;
		return false;
	}
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		if(o instanceof String)
			return compareToOreName(stack, (String)o);
		if(o instanceof ItemStack)
			return OreDictionary.itemMatches((ItemStack)o, stack, false);
		return false;
	}
	public static ItemStack copyStackWithAmount(ItemStack stack, int amount)
	{
		ItemStack s2 = stack.copy();
		s2.stackSize=amount;
		return s2;
	}
	static String[] dyeNames = {"Black","Red","Green","Brown","Blue","Purple","Cyan","LightGray","Gray","Pink","Lime","Yellow","LightBlue","Magenta","Orange","White"};
	public static int getDye(ItemStack stack)
	{
		if(stack==null)
			return -1;
		if(stack.getItem().equals(Items.dye))
			return stack.getItemDamage();
		for(int dye=0;dye<dyeNames.length;dye++)
			if(compareToOreName(stack,"dye"+dyeNames[dye]))
				return dye;
		return -1;
	}

	public static ChunkCoordinates toCC(Object object)
	{
		if(object instanceof ChunkCoordinates)
			return (ChunkCoordinates)object;
		if(object instanceof TileEntity)
			return new ChunkCoordinates(((TileEntity)object).xCoord,((TileEntity)object).yCoord,((TileEntity)object).zCoord);
		return null;
	}
	public static IImmersiveConnectable toIIC(Object object, World world)
	{
		if(object instanceof IImmersiveConnectable)
			return (IImmersiveConnectable)object;
		else if(object instanceof ChunkCoordinates && world!=null && world.getTileEntity( ((ChunkCoordinates)object).posX, ((ChunkCoordinates)object).posY, ((ChunkCoordinates)object).posZ) instanceof IImmersiveConnectable)
		{
			return (IImmersiveConnectable)world.getTileEntity( ((ChunkCoordinates)object).posX, ((ChunkCoordinates)object).posY, ((ChunkCoordinates)object).posZ);
		}
		return null;
	}

	public static boolean tilePositionMatch(TileEntity tile0, TileEntity tile1)
	{
		return tile0.xCoord==tile1.xCoord && tile0.yCoord==tile1.yCoord && tile0.zCoord==tile1.zCoord;
	}

	public static MovingObjectPosition getMovingObjectPositionFromPlayer(World world, EntityLivingBase living, boolean bool)
	{
		float f = 1.0F;
		float f1 = living.prevRotationPitch + (living.rotationPitch - living.prevRotationPitch) * f;
		float f2 = living.prevRotationYaw + (living.rotationYaw - living.prevRotationYaw) * f;
		double d0 = living.prevPosX + (living.posX - living.prevPosX) * (double)f;
		double d1 = living.prevPosY + (living.posY - living.prevPosY) * (double)f + (double)(world.isRemote ? living.getEyeHeight() - (living instanceof EntityPlayer?((EntityPlayer)living).getDefaultEyeHeight():0) : living.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
		double d2 = living.prevPosZ + (living.posZ - living.prevPosZ) * (double)f;
		Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 5.0D;
		if (living instanceof EntityPlayerMP)
			d3 = ((EntityPlayerMP)living).theItemInWorldManager.getBlockReachDistance();

		Vec3 vec31 = vec3.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
		return world.func_147447_a(vec3, vec31, bool, !bool, false);
	}
	public static boolean canBlocksSeeOther(World world, ChunkCoordinates cc0, ChunkCoordinates cc1, Vec3 pos0, Vec3 pos1)
	{
		MovingObjectPosition mop =  world.rayTraceBlocks(pos0, pos1);
		return mop==null || (mop.blockX==cc0.posX&&mop.blockY==cc0.posY&&mop.blockZ==cc0.posZ) || (mop.blockX==cc1.posX&&mop.blockY==cc1.posY&&mop.blockZ==cc1.posZ);
	}

	public static boolean isHammer(ItemStack stack)
	{
		if(stack==null)
			return false;
		return stack.getItem().getToolClasses(stack).contains(Lib.TOOL_HAMMER);
	}

	public static Vec3 getFlowVector(World world, int x, int y, int z)
	{
		if(world.getBlock(x, y, z) instanceof BlockFluidBase)
			return ((BlockFluidBase)world.getBlock(x, y, z)).getFlowVector(world, x, y, z);
		else if( !(world.getBlock(x, y, z) instanceof BlockLiquid))
			return Vec3.createVectorHelper(0, 0, 0);

		BlockLiquid block = (BlockLiquid)world.getBlock(x, y, z);
		Vec3 vec3 = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
		Material mat = block.getMaterial();
		int l = getEffectiveFlowDecay(world, x, y, z, mat);

		for (int i1 = 0; i1 < 4; ++i1)
		{
			int j1 = x;
			int k1 = z;

			if (i1 == 0)
				j1 = x - 1;
			if (i1 == 1)
				k1 = z - 1;
			if (i1 == 2)
				++j1;
			if (i1 == 3)
				++k1;
			int l1 = getEffectiveFlowDecay(world, j1, y, k1, mat);
			int i2;

			if (l1 < 0)
			{
				if (!world.getBlock(j1, y, k1).getMaterial().blocksMovement())
				{
					l1 = getEffectiveFlowDecay(world, j1, y - 1, k1, mat);

					if (l1 >= 0)
					{
						i2 = l1 - (l - 8);
						vec3 = vec3.addVector((double)((j1 - x) * i2), (double)((y - y) * i2), (double)((k1 - z) * i2));
					}
				}
			}
			else if (l1 >= 0)
			{
				i2 = l1 - l;
				vec3 = vec3.addVector((double)((j1 - x) * i2), (double)((y - y) * i2), (double)((k1 - z) * i2));
			}
		}

		if (world.getBlockMetadata(x, y, z) >= 8)
		{
			boolean flag = false;

			if (flag || block.isBlockSolid(world, x, y, z - 1, 2))
				flag = true;
			if (flag || block.isBlockSolid(world, x, y, z + 1, 3))
				flag = true;
			if (flag || block.isBlockSolid(world, x - 1, y, z, 4))
				flag = true;
			if (flag || block.isBlockSolid(world, x + 1, y, z, 5))
				flag = true;
			if (flag || block.isBlockSolid(world, x, y + 1, z - 1, 2))
				flag = true;
			if (flag || block.isBlockSolid(world, x, y + 1, z + 1, 3))
				flag = true;
			if (flag || block.isBlockSolid(world, x - 1, y + 1, z, 4))
				flag = true;
			if (flag || block.isBlockSolid(world, x + 1, y + 1, z, 5))
				flag = true;
			if (flag)
				vec3 = vec3.normalize().addVector(0.0D, -6.0D, 0.0D);
		}
		vec3 = vec3.normalize();
		return vec3;
	}
	static int getEffectiveFlowDecay(IBlockAccess world, int x, int y, int z, Material mat)
	{
		if (world.getBlock(x, y, z).getMaterial() != mat)
			return -1;
		int l = world.getBlockMetadata(x, y, z);
		if (l >= 8)
			l = 0;
		return l;
	}
	public static Vec3 addVectors(Vec3 vec0, Vec3 vec1)
	{
		return vec0.addVector(vec1.xCoord,vec1.yCoord,vec1.zCoord);
	}

	public static Collection<ItemStack> getContainersFilledWith(FluidStack fluidStack)
	{
		List<ItemStack> containers = new ArrayList();
		for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData())
			if(data.fluid.containsFluid(fluidStack))
				containers.add(data.filledContainer);
		return containers;
	}

	public static String nameFromStack(ItemStack stack)
	{
		if(stack==null)
			return "";
		try
		{
			return GameData.getItemRegistry().getNameForObject(stack.getItem());
		}
		catch (NullPointerException e) {}
		return "";
	}
}