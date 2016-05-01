package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBlastFurnacePreheater;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

public class TileEntityBlastFurnaceAdvanced extends TileEntityBlastFurnace
{
	
	@Override
	public void update()
	{
		super.update();
		if(!worldObj.isRemote && worldObj.getTotalWorldTime()%8==0 && !isDummy())
		{
			TileEntity inventoryFront = this.worldObj.getTileEntity(getPos().offset(facing,2).add(0,-1,0));
			if(this.inventory[2]!=null)
			{
				ItemStack stack = this.inventory[2];
				if(inventoryFront!=null)
					stack = Utils.insertStackIntoInventory(inventoryFront, stack, facing.getOpposite());
				this.inventory[2] = stack;
			}
			TileEntity inventoryBack = this.worldObj.getTileEntity(getPos().offset(facing,-2).add(0,-1,0));
			if(this.inventory[3]!=null)
			{
				ItemStack stack = this.inventory[3];
				if(inventoryBack!=null)
					stack = Utils.insertStackIntoInventory(inventoryBack, stack, facing);
				this.inventory[3] = stack;
			}
		}
	}
	
	@Override
	public float[] getBlockBounds()
	{
		if(pos%9==4 || pos==1 || pos==10 || pos==31)
			return new float[]{0,0,0,1,1,1};

		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;

		if(pos==7)
		{
			xMin = facing.getAxis()==Axis.Z?.1875f:0;
			xMax = facing.getAxis()==Axis.Z?.8125f:1;
			zMin = facing.getAxis()==Axis.X?.1875f:0;
			zMax = facing.getAxis()==Axis.X?.8125f:1;
			yMax = .8125f;
		}
		else
		{
			float indent = 1;
			if(pos<9)
				indent = (pos>2&&pos<6)?.5f:.3125f;
			else if(pos<18)
				indent = .5f;
			else if(pos<27)
				indent = .375f;

			if((pos%9<3&&facing==EnumFacing.WEST)||(pos%9>5&&facing==EnumFacing.EAST)||(pos%3==2&&facing==EnumFacing.SOUTH)||(pos%3==0&&facing==EnumFacing.NORTH))
				xMin = (1-indent);
			if((pos%9<3&&facing==EnumFacing.EAST)||(pos%9>5&&facing==EnumFacing.WEST)||(pos%3==2&&facing==EnumFacing.NORTH)||(pos%3==0&&facing==EnumFacing.SOUTH))
				xMax = indent;
			if((pos%9<3&&facing==EnumFacing.SOUTH)||(pos%9>5&&facing==EnumFacing.NORTH)||(pos%3==2&&facing==EnumFacing.EAST)||(pos%3==0&&facing==EnumFacing.WEST))
				zMin = (1-indent);
			if((pos%9<3&&facing==EnumFacing.NORTH)||(pos%9>5&&facing==EnumFacing.SOUTH)||(pos%3==2&&facing==EnumFacing.WEST)||(pos%3==0&&facing==EnumFacing.EAST))
				zMax = indent;
		}

		return new float[]{xMin,yMin,zMin, xMax,yMax,zMax};
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(this.pos == 31)
			return new ItemStack(Blocks.hopper);
		return new ItemStack(IEContent.blockStoneDecoration,1,2);
	}

	@Override
	protected int getProcessSpeed()
	{
		int i = 1;
		for(int j=0; j<2; j++)
		{
			EnumFacing phf = j==0?facing.rotateY():facing.rotateYCCW();
			BlockPos pos = getPos().add(0,-1,0).offset(phf,2);
			if(worldObj.getTileEntity(pos) instanceof TileEntityBlastFurnacePreheater)
			{
				if( ((TileEntityBlastFurnacePreheater)worldObj.getTileEntity(pos)).facing==phf.getOpposite())
					i += ((TileEntityBlastFurnacePreheater)worldObj.getTileEntity(pos)).doSpeedup();
			}
		}
		return i;
	}

	@Override
	public void disassemble()
	{
		if(formed && !worldObj.isRemote)
		{
			BlockPos startPos = this.getPos().add(-offset[0],-offset[1],-offset[2]);
			if(!(offset[0]==0&&offset[1]==0&&offset[2]==0) && !(worldObj.getTileEntity(startPos) instanceof TileEntityBlastFurnaceAdvanced))
				return;
			
			for(int yy=-1;yy<=2;yy++)
				for(int xx=-1;xx<=1;xx++)
					for(int zz=-1;zz<=1;zz++)
						if(yy!=2 || (xx==0 && zz==0))
						{
							ItemStack s = null;
							TileEntity te = worldObj.getTileEntity(startPos.add(xx, yy, zz));
							if(te instanceof TileEntityBlastFurnaceAdvanced)
							{
								s = ((TileEntityBlastFurnaceAdvanced)te).getOriginalBlock();
								((TileEntityBlastFurnaceAdvanced)te).formed=false;
							}
							if(startPos.add(xx, yy, zz).equals(getPos()))
								s = this.getOriginalBlock();
							if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
							{
								if(startPos.add(xx, yy, zz).equals(getPos()))
									worldObj.spawnEntityInWorld(new EntityItem(worldObj, getPos().getX()+.5,getPos().getY()+.5,getPos().getZ()+.5, s));
								else
								{
									if(Block.getBlockFromItem(s.getItem())==IEContent.blockStoneDevice)
										worldObj.setBlockToAir(startPos.add(xx, yy, zz));
									worldObj.setBlockState(startPos.add(xx, yy, zz), Block.getBlockFromItem(s.getItem()).getStateFromMeta(s.getItemDamage()));
								}
							}
						}
		}
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability==net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}
	IItemHandler inputHandler = new IEInventoryHandler(2,this,0, new boolean[]{true,true},new boolean[]{false,false});
	IItemHandler outputHandler = new IEInventoryHandler(1,this,2, new boolean[]{false},new boolean[]{true});
	IItemHandler slagHandler = new IEInventoryHandler(1,this,3, new boolean[]{false},new boolean[]{true});
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityBlastFurnaceAdvanced master = (TileEntityBlastFurnaceAdvanced)master();
			if(master==null)
				return null;
			if(pos==31 && facing==EnumFacing.UP)
				return (T)master.inputHandler;
			if(pos==1 && facing==master.facing)
				return (T)master.outputHandler;
			if(pos==7 && facing==master.facing.getOpposite())
				return (T)master.slagHandler;
			return null;
		}
		return super.getCapability(capability, facing);
	}
}