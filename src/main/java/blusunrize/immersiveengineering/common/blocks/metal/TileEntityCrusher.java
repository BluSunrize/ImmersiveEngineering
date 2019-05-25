/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPoweredMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockCrusher;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityCrusher extends TileEntityPoweredMultiblock<TileEntityCrusher, CrusherRecipe> implements ISoundTile, IAdvancedSelectionBounds, IAdvancedCollisionBounds
{
	public static TileEntityType<TileEntityCrusher> TYPE;
	public List<ItemStack> inputs = new ArrayList<>();
	public int process = 0;

	public float animation_barrelRotation = 0;

	public TileEntityCrusher()
	{
		super(MultiblockCrusher.instance, 32000, true, TYPE);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			NBTTagList invList = nbt.getList("inputs", 10);
			inputs.clear();
			for(int i = 0; i < invList.size(); i++)
				inputs.add(ItemStack.read(invList.getCompound(i)));
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
		{
			NBTTagList invList = new NBTTagList();
			for(ItemStack s : inputs)
				invList.add(s.write(new NBTTagCompound()));
			nbt.setTag("inputs", invList);
		}
	}

	@Override
	public void tick()
	{
		super.tick();
		if(world.isRemote&&!isDummy())
		{
			boolean active = shouldRenderAsActive();
			ImmersiveEngineering.proxy.handleTileSound(IESounds.crusher, this, active, .5f, 1);
			if(active)
			{
				animation_barrelRotation += 18f;
				animation_barrelRotation %= 360f;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		//		if(renderAABB==null)
		//			if(pos==17)
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+3,zCoord+(facing==4||facing==5?3:2));
		//			else
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
		//		return renderAABB;
		return new AxisAlignedBB(getPos().getX()-(facing.getAxis()==Axis.Z?2: 1), getPos().getY(), getPos().getZ()-(facing.getAxis()==Axis.X?2: 1), getPos().getX()+(facing.getAxis()==Axis.Z?3: 2), getPos().getY()+3, getPos().getZ()+(facing.getAxis()==Axis.X?3: 2));
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock==1||posInMultiblock==3||posInMultiblock==4||posInMultiblock==6||posInMultiblock==8||posInMultiblock==11||posInMultiblock==12||posInMultiblock==13||posInMultiblock==14||posInMultiblock==24)
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(posInMultiblock==22)
			return new float[]{0, 0, 0, 1, .75f, 1};
		if(posInMultiblock==37)
			return new float[]{0, 0, 0, 0, 0, 0};

		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();
		if(posInMultiblock > 15&&posInMultiblock%5 > 0&&posInMultiblock%5 < 4)
		{
			float minX = 0;
			float maxX = 1;
			float minZ = 0;
			float maxZ = 1;
			if(posInMultiblock%5==1)
			{
				minX = fw==EnumFacing.EAST?.1875f: 0;
				maxX = fw==EnumFacing.WEST?.8125f: 1;
				minZ = fw==EnumFacing.SOUTH?.1875f: 0;
				maxZ = fw==EnumFacing.NORTH?.8125f: 1;
			}
			else if(posInMultiblock%5==3)
			{
				minX = fw==EnumFacing.WEST?.1875f: 0;
				maxX = fw==EnumFacing.EAST?.8125f: 1;
				minZ = fw==EnumFacing.NORTH?.1875f: 0;
				maxZ = fw==EnumFacing.SOUTH?.8125f: 1;
			}
			if((posInMultiblock%15)/5==0)
			{
				if(fl==EnumFacing.EAST)
					minX = .1875f;
				if(fl==EnumFacing.WEST)
					maxX = .8125f;
				if(fl==EnumFacing.SOUTH)
					minZ = .1875f;
				if(fl==EnumFacing.NORTH)
					maxZ = .8125f;
			}

			return new float[]{minX, 0, minZ, maxX, 1, maxZ};
		}
		if(posInMultiblock==19)
			return new float[]{facing==EnumFacing.WEST?.5f: 0, 0, facing==EnumFacing.NORTH?.5f: 0, facing==EnumFacing.EAST?.5f: 1, 1, facing==EnumFacing.SOUTH?.5f: 1};

		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		if(posInMultiblock%15==7)
			return null;
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();
		if(posInMultiblock==4)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			float minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .125f;
			float maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .25f;
			float minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .125f;
			float maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .25f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .75f;
			maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .875f;
			minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .75f;
			maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .875f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if((posInMultiblock > 20&&posInMultiblock < 24)||(posInMultiblock > 35&&posInMultiblock < 39))
		{
			List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>(3);
			float minY = .5f;
			float minX = (posInMultiblock%5==1&&fw==EnumFacing.EAST)||(posInMultiblock%5==3&&fw==EnumFacing.WEST)?.4375f: 0;
			float maxX = (posInMultiblock%5==1&&fw==EnumFacing.WEST)||(posInMultiblock%5==3&&fw==EnumFacing.EAST)?.5625f: 1;
			float minZ = (posInMultiblock%5==1&&fw==EnumFacing.SOUTH)||(posInMultiblock%5==3&&fw==EnumFacing.NORTH)?.4375f: 0;
			float maxZ = (posInMultiblock%5==1&&fw==EnumFacing.NORTH)||(posInMultiblock%5==3&&fw==EnumFacing.SOUTH)?.5625f: 1;
			if(posInMultiblock > 20&&posInMultiblock < 24)
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			else
				minY = 0;

			minX = (posInMultiblock%5==1&&fw==EnumFacing.EAST)||(posInMultiblock%5==3&&fw==EnumFacing.WEST)?.1875f: (posInMultiblock%5==1&&fw==EnumFacing.WEST)||(posInMultiblock%5==3&&fw==EnumFacing.EAST)?.5625f: 0;
			maxX = (posInMultiblock%5==1&&fw==EnumFacing.WEST)||(posInMultiblock%5==3&&fw==EnumFacing.EAST)?.8125f: (posInMultiblock%5==1&&fw==EnumFacing.EAST)||(posInMultiblock%5==3&&fw==EnumFacing.WEST)?.4375f: 1;
			minZ = (posInMultiblock%5==1&&fw==EnumFacing.SOUTH)||(posInMultiblock%5==3&&fw==EnumFacing.NORTH)?.1875f: (posInMultiblock%5==1&&fw==EnumFacing.NORTH)||(posInMultiblock%5==3&&fw==EnumFacing.SOUTH)?.5625f: 0;
			maxZ = (posInMultiblock%5==1&&fw==EnumFacing.NORTH)||(posInMultiblock%5==3&&fw==EnumFacing.SOUTH)?.8125f: (posInMultiblock%5==1&&fw==EnumFacing.SOUTH)||(posInMultiblock%5==3&&fw==EnumFacing.NORTH)?.4375f: 1;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if((posInMultiblock > 15&&posInMultiblock < 19)||(posInMultiblock > 30&&posInMultiblock < 34)||(posInMultiblock > 25&&posInMultiblock < 29)||(posInMultiblock > 40&&posInMultiblock < 44))
		{
			if(posInMultiblock%15 > 9)
				fl = fl.getOpposite();
			List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>(3);
			float minY = .5f;
			float minX = (posInMultiblock%5==1&&fw==EnumFacing.EAST)||(posInMultiblock%5==3&&fw==EnumFacing.WEST)?.4375f: fl==EnumFacing.EAST?.4375f: 0;
			float maxX = (posInMultiblock%5==1&&fw==EnumFacing.WEST)||(posInMultiblock%5==3&&fw==EnumFacing.EAST)?.5625f: fl==EnumFacing.WEST?.5625f: 1;
			float minZ = (posInMultiblock%5==1&&fw==EnumFacing.SOUTH)||(posInMultiblock%5==3&&fw==EnumFacing.NORTH)?.4375f: fl==EnumFacing.SOUTH?.4375f: 0;
			float maxZ = (posInMultiblock%5==1&&fw==EnumFacing.NORTH)||(posInMultiblock%5==3&&fw==EnumFacing.SOUTH)?.5625f: fl==EnumFacing.NORTH?.5625f: 1;
			if((posInMultiblock > 15&&posInMultiblock < 19)||(posInMultiblock > 25&&posInMultiblock < 29))
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			else
				minY = 0;

			if(posInMultiblock/15 > 9)
				fl = fl.getOpposite();

			minX = (posInMultiblock%5==1&&fw==EnumFacing.EAST)||(posInMultiblock%5==3&&fw==EnumFacing.WEST)?.1875f: fl==EnumFacing.EAST?.1875f: fl==EnumFacing.WEST?.5625f: 0;
			maxX = (posInMultiblock%5==1&&fw==EnumFacing.WEST)||(posInMultiblock%5==3&&fw==EnumFacing.EAST)?.8125f: fl==EnumFacing.WEST?.8125f: fl==EnumFacing.EAST?.4375f: 1;
			minZ = (posInMultiblock%5==1&&fw==EnumFacing.SOUTH)||(posInMultiblock%5==3&&fw==EnumFacing.NORTH)?.1875f: fl==EnumFacing.SOUTH?.1875f: fl==EnumFacing.NORTH?.5625f: 0;
			maxZ = (posInMultiblock%5==1&&fw==EnumFacing.NORTH)||(posInMultiblock%5==3&&fw==EnumFacing.SOUTH)?.8125f: fl==EnumFacing.NORTH?.8125f: fl==EnumFacing.SOUTH?.4375f: 1;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock!=17&&posInMultiblock!=32&&posInMultiblock!=27&&posInMultiblock!=42)
			{
				minX = (posInMultiblock%5==1&&fw==EnumFacing.EAST)||(posInMultiblock%5==3&&fw==EnumFacing.WEST)?.1875f: fl==EnumFacing.EAST?.4375f: fl==EnumFacing.WEST?0: .5625f;
				maxX = (posInMultiblock%5==1&&fw==EnumFacing.WEST)||(posInMultiblock%5==3&&fw==EnumFacing.EAST)?.8125f: fl==EnumFacing.WEST?.5625f: fl==EnumFacing.EAST?1: .4375f;
				minZ = (posInMultiblock%5==1&&fw==EnumFacing.SOUTH)||(posInMultiblock%5==3&&fw==EnumFacing.NORTH)?.1875f: fl==EnumFacing.SOUTH?.4375f: fl==EnumFacing.NORTH?0: .5625f;
				maxZ = (posInMultiblock%5==1&&fw==EnumFacing.NORTH)||(posInMultiblock%5==3&&fw==EnumFacing.SOUTH)?.8125f: fl==EnumFacing.NORTH?.5625f: fl==EnumFacing.SOUTH?1: .4375f;
				list.add(new AxisAlignedBB(minX, minY, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				if(posInMultiblock%15%10==1)
					fw = fw.getOpposite();
				if((posInMultiblock > 15&&posInMultiblock < 19)||(posInMultiblock > 25&&posInMultiblock < 29))
				{
					minX = fl==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.25f: fw==EnumFacing.EAST?.5f: .25f;
					maxX = fl==EnumFacing.EAST?.5f: fl==EnumFacing.WEST?.75f: fw==EnumFacing.EAST?.75f: .5f;
					minZ = fl==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.25f: fw==EnumFacing.SOUTH?.5f: .25f;
					maxZ = fl==EnumFacing.SOUTH?.5f: fl==EnumFacing.NORTH?.75f: fw==EnumFacing.SOUTH?.75f: .5f;
					list.add(new AxisAlignedBB(minX, 0, minZ, maxX, .5f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				}
			}
			return list;
		}
		if(posInMultiblock==1||posInMultiblock==3||posInMultiblock==11||posInMultiblock==13)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock%15 > 9)
				fl = fl.getOpposite();
			if(posInMultiblock%15%10==1)
				fw = fw.getOpposite();
			float minX = fl==EnumFacing.WEST?.5f: fl==EnumFacing.EAST?.25f: fw==EnumFacing.EAST?.5f: .25f;
			float maxX = fl==EnumFacing.EAST?.5f: fl==EnumFacing.WEST?.75f: fw==EnumFacing.EAST?.75f: .5f;
			float minZ = fl==EnumFacing.NORTH?.5f: fl==EnumFacing.SOUTH?.25f: fw==EnumFacing.SOUTH?.5f: .25f;
			float maxZ = fl==EnumFacing.SOUTH?.5f: fl==EnumFacing.NORTH?.75f: fw==EnumFacing.SOUTH?.75f: .5f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			return list;
		}

		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	private boolean isInInput()
	{
		return posInMultiblock > 15&&posInMultiblock < 30&&posInMultiblock%5 > 0&&posInMultiblock%5 < 4;
	}

	@Override
	public void onEntityCollision(World world, Entity entity)
	{
		boolean bpos = isInInput();
		if(bpos&&!world.isRemote&&entity.isAlive()&&!isRSDisabled())
		{
			TileEntityCrusher master = master();
			if(master==null)
				return;
			Vec3d center = new Vec3d(master.getPos()).add(.5, .75, .5);
			AxisAlignedBB crusherInternal = new AxisAlignedBB(center.x-1.0625, center.y, center.z-1.0625, center.x+1.0625, center.y+1.25, center.z+1.0625);
			if(!entity.getBoundingBox().intersects(crusherInternal))
				return;
			if(entity instanceof EntityItem&&!((EntityItem)entity).getItem().isEmpty())
			{
				ItemStack stack = ((EntityItem)entity).getItem();
				if(stack.isEmpty())
					return;
				CrusherRecipe recipe = master.findRecipeForInsertion(stack);
				if(recipe==null)
					return;
				ItemStack displayStack = recipe.getDisplayStack(stack);
				MultiblockProcess<CrusherRecipe> process = new MultiblockProcessInWorld<CrusherRecipe>(recipe, .5f, Utils.createNonNullItemStackListFromItemStack(displayStack));
				if(master.addProcessToQueue(process, true, true))
				{
					master.addProcessToQueue(process, false, true);
					stack.shrink(displayStack.getCount());
					if(stack.getCount() <= 0)
						entity.remove();
				}
			}
			else if(entity instanceof EntityLivingBase&&(!(entity instanceof EntityPlayer)||!((EntityPlayer)entity).abilities.disableDamage))
			{
				int consumed = master.energyStorage.extractEnergy(80, true);
				if(consumed > 0)
				{
					master.energyStorage.extractEnergy(consumed, false);
					EventHandler.crusherMap.put(entity.getUniqueID(), master);
					entity.attackEntityFrom(IEDamageSources.crusher, consumed/20f);
				}
			}
		}
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{20};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{19};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return true;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<CrusherRecipe> process)
	{
		return true;
	}

	private CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(getPos().add(0, -1, 0).offset(facing, -2), facing),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(this.output, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, getPos().add(0, -1, 0).offset(facing, -2), output, facing.getOpposite());
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<CrusherRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 2048;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<CrusherRecipe> process)
	{
		return 0;
	}

	@Override
	public int[] getCurrentProcessesStep()
	{
		TileEntityCrusher master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesStep();
		int[] ia = new int[processQueue.size() > 0?1: 0];
		for(int i = 0; i < ia.length; i++)
			ia[i] = processQueue.get(i).processTick;
		return ia;
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		TileEntityCrusher master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesMax();
		int[] ia = new int[processQueue.size() > 0?1: 0];
		for(int i = 0; i < ia.length; i++)
			ia[i] = processQueue.get(i).maxTicks;
		return ia;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return false;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 0;
	}

	@Override
	public int[] getOutputSlots()
	{
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return null;
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return null;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
	}

	LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new MultiblockInventoryHandler_DirectProcessing<>(this).setProcessStacking(true)
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		if(isInInput()&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityCrusher master = master();
			if(master!=null)
				return master.insertionHandler.cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public CrusherRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return CrusherRecipe.findRecipe(inserting);
	}

	@Override
	protected CrusherRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return CrusherRecipe.loadFromNBT(tag);
	}

	@Override
	public boolean shoudlPlaySound(String sound)
	{
		return shouldRenderAsActive();
	}
}