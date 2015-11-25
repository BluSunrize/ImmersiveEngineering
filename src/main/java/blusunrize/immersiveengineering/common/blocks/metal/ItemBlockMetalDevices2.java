package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenBarrel;
import blusunrize.immersiveengineering.common.util.Lib;

public class ItemBlockMetalDevices2 extends ItemBlockIEBase
{
	public ItemBlockMetalDevices2(Block b)
	{
		super(b);
	}


	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advInfo)
	{
		if(stack.getItemDamage()==BlockMetalDevices2.META_barrel)
		{
			if(stack.hasTagCompound())
			{
				NBTTagCompound tag = stack.getTagCompound().getCompoundTag("tank");
				if(!tag.hasKey("Empty"))
				{
					FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag);
					list.add(fluid.getLocalizedName()+": "+fluid.amount+"mB");
				}				
				else
					list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"barrel"));
			}
			else
				list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"barrel"));
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;

		if(meta==BlockMetalDevices2.META_fluidPump)
			if(!world.isAirBlock(x, y+1, z))
				return false;

		boolean ret = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, meta);
		if(!ret) return false;
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if(tileEntity instanceof TileEntityBreakerSwitch)
		{
			if(side<2)
			{
				((TileEntityBreakerSwitch)tileEntity).sideAttached = ForgeDirection.OPPOSITES[side]+1;
				((TileEntityBreakerSwitch)tileEntity).facing = f;
			}
			else
				((TileEntityBreakerSwitch)tileEntity).facing = ForgeDirection.OPPOSITES[side];
		}
		else if(tileEntity instanceof TileEntityEnergyMeter)
		{
			((TileEntityEnergyMeter)tileEntity).facing = f;
		}
		else if(tileEntity instanceof TileEntityFloodlight)
		{
			((TileEntityFloodlight)tileEntity).side = side;
			if(f==side && player.rotationPitch>0)
				f = ForgeDirection.OPPOSITES[f];
			((TileEntityFloodlight)tileEntity).facing = f;
		}
		else if(tileEntity instanceof TileEntityFluidPump)
		{
			((TileEntityFluidPump)tileEntity).dummy = false;
			world.setBlock(x, y+1, z, IEContent.blockMetalDevice2, BlockMetalDevices2.META_fluidPump, 3);
		}
		else if(tileEntity instanceof TileEntityWoodenBarrel)
		{
			if(stack.hasTagCompound())
				((TileEntityWoodenBarrel)tileEntity).readTank(stack.getTagCompound());
		}
		else if(tileEntity instanceof TileEntityChargingStation)
		{
			((TileEntityChargingStation)tileEntity).facing = f;
		}
		return ret;
	}
}