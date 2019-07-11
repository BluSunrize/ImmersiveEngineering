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
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPoweredMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockArcFurnace;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TileEntityArcFurnace extends TileEntityPoweredMultiblock<TileEntityArcFurnace, ArcFurnaceRecipe>
		implements ISoundTile, IInteractionObjectIE, IAdvancedSelectionBounds, IAdvancedCollisionBounds
{
	public static TileEntityType<TileEntityArcFurnace> TYPE;
	private static final int SLAG_SLOT = 22;
	private static final int FIRST_OUT_SLOT = 16;
	private static final int OUT_SLOT_COUNT = 6;
	private static final int SLAG_OUT_POS = 22;
	private static final int MAIN_OUT_POS = 2;
	private static final int[] OUTPUT_SLOTS;

	static
	{
		OUTPUT_SLOTS = new int[OUT_SLOT_COUNT];
		for(int i = 0; i < OUT_SLOT_COUNT; ++i)
		{
			OUTPUT_SLOTS[i] = FIRST_OUT_SLOT+i;
		}
	}

	public NonNullList<ItemStack> inventory = NonNullList.withSize(26, ItemStack.EMPTY);
	public int pouringMetal = 0;
	private CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(this.getBlockPosForPos(MAIN_OUT_POS).offset(facing, -1), facing.getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	private CapabilityReference<IItemHandler> slagOut = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(this.getBlockPosForPos(SLAG_OUT_POS).offset(facing), facing.getOpposite()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
	
	public TileEntityArcFurnace()
	{
		super(MultiblockArcFurnace.instance, 64000, true, TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 26);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		if(!descPacket)
			nbt.put("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void tick()
	{
		super.tick();
		if(isDummy())
			return;
		if(world.isRemote)
		{
			if(pouringMetal > 0)
				pouringMetal--;
			if(shouldRenderAsActive())
				for(int i = 0; i < 4; i++)
				{
					if(Utils.RAND.nextInt(6)==0)
						ImmersiveEngineering.proxy.spawnSparkFX(world, getPos().getX()+.5-.25*facing.getXOffset(),
								getPos().getY()+2.9, getPos().getZ()+.5-.25*facing.getZOffset(),
								Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
					if(Utils.RAND.nextInt(6)==0)
						ImmersiveEngineering.proxy.spawnSparkFX(world, getPos().getX()+.5+(facing==Direction.EAST?-.25: .25),
								getPos().getY()+2.9, getPos().getZ()+.5+(facing==Direction.SOUTH?.25: -.25),
								Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
					if(Utils.RAND.nextInt(6)==0)
						ImmersiveEngineering.proxy.spawnSparkFX(world, getPos().getX()+.5+(facing==Direction.WEST?.25: -.25),
								getPos().getY()+2.9, getPos().getZ()+.5+(facing==Direction.NORTH?-.25: .25),
								Utils.RAND.nextDouble()*.05-.025, .025, Utils.RAND.nextDouble()*.05-.025);
				}
		}
		else if(!isRSDisabled()&&energyStorage.getEnergyStored() > 0)
		{
			if(this.tickedProcesses > 0)
				for(int i = 23; i < 26; i++)
					if(this.inventory.get(i).attemptDamageItem(1, Utils.RAND, null))
					{
						this.inventory.set(i, ItemStack.EMPTY);
						//						updateClient = true;
						//						update = true;
					}

			if(this.processQueue.size() < this.getProcessQueueMaxLength())
			{
				Map<Integer, Integer> usedInvSlots = new HashMap<Integer, Integer>();
				for(MultiblockProcess<ArcFurnaceRecipe> process : processQueue)
					if(process instanceof MultiblockProcessInMachine)
					{
						int[] inputSlots = ((MultiblockProcessInMachine<ArcFurnaceRecipe>)process).getInputSlots();
						int[] inputAmounts = ((MultiblockProcessInMachine<ArcFurnaceRecipe>)process).getInputAmounts();
						if(inputAmounts!=null)
							for(int i = 0; i < inputSlots.length; i++)
								if(usedInvSlots.containsKey(inputSlots[i]))
									usedInvSlots.put(inputSlots[i], usedInvSlots.get(inputSlots[i])+inputAmounts[i]);
								else
									usedInvSlots.put(inputSlots[i], inputAmounts[i]);
					}

				NonNullList<ItemStack> additives = NonNullList.withSize(4, ItemStack.EMPTY);
				for(int i = 0; i < 4; i++)
					if(!inventory.get(12+i).isEmpty())
					{
						additives.set(i, inventory.get(12+i).copy());
						if(usedInvSlots.containsKey(12+i))
							additives.get(i).shrink(usedInvSlots.get(12+i));
					}

				for(int slot = 0; slot < 12; slot++)
					if(!usedInvSlots.containsKey(slot))
					{
						ItemStack stack = this.getInventory().get(slot);
						if(!stack.isEmpty()&&stack.getCount() > 0)
						{
							ArcFurnaceRecipe recipe = ArcFurnaceRecipe.findRecipe(stack, additives);
							if(recipe!=null)
							{
								MultiblockProcessArcFurnace process = new MultiblockProcessArcFurnace(recipe, slot, 12, 13, 14, 15);
								if(this.addProcessToQueue(process, true))
								{
									this.addProcessToQueue(process, false);
									int[] consumedAdditives = recipe.getConsumedAdditives(additives, true);
									if(consumedAdditives!=null)
										process.setInputAmounts(1, consumedAdditives[0], consumedAdditives[1], consumedAdditives[2], consumedAdditives[3]);
									//							update = true;
								}
							}
						}
					}
			}

			if(world.getGameTime()%8==0)
			{
				if(output.isPresent())
					for(int j : OUTPUT_SLOTS)
						if(!inventory.get(j).isEmpty())
						{
							ItemStack stack = Utils.copyStackWithAmount(inventory.get(j), 1);
							stack = Utils.insertStackIntoInventory(output, stack, false);
							if(stack.isEmpty())
							{
								this.inventory.get(j).shrink(1);
								if(this.inventory.get(j).getCount() <= 0)
									this.inventory.set(j, ItemStack.EMPTY);
							}
						}
				if(!inventory.get(SLAG_SLOT).isEmpty()&&slagOut.isPresent())
				{
					int out = Math.min(inventory.get(SLAG_SLOT).getCount(), 16);
					ItemStack stack = Utils.copyStackWithAmount(inventory.get(SLAG_SLOT), out);
					stack = Utils.insertStackIntoInventory(slagOut, stack, false);
					if(!stack.isEmpty())
						out -= stack.getCount();
					this.inventory.get(SLAG_SLOT).shrink(out);
					if(this.inventory.get(SLAG_SLOT).getCount() <= 0)
						this.inventory.set(SLAG_SLOT, ItemStack.EMPTY);
				}
			}
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int type)
	{
		if(id==0)
			pouringMetal = type;
		return super.receiveClientEvent(id, type);
	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		//		if(renderAABB==null)
		//			if(posInMultiblock==17)
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+3,zCoord+(facing==4||facing==5?3:2));
		//			else
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
		//		return renderAABB;
		return new AxisAlignedBB(getPos().getX()-(facing.getAxis()==Axis.Z?2: 1), getPos().getY(), getPos().getZ()-(facing.getAxis()==Axis.X?2: 1), getPos().getX()+(facing.getAxis()==Axis.Z?3: 2), getPos().getY()+3, getPos().getZ()+(facing.getAxis()==Axis.X?3: 2));
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock==1||posInMultiblock==3)
			return new float[]{facing==Direction.EAST?.4375f: 0, 0, facing==Direction.SOUTH?.4375f: 0, facing==Direction.WEST?.5625f: 1, .5f, facing==Direction.NORTH?.5625f: 1};
		else if(posInMultiblock < 20&&posInMultiblock!=2)
			return new float[]{0, 0, 0, 1, .5f, 1};
		else if(posInMultiblock==25)
			return new float[]{facing==Direction.WEST?.5f: 0, 0, facing==Direction.NORTH?.5f: 0, facing==Direction.EAST?.5f: 1, 1, facing==Direction.SOUTH?.5f: 1};
		else if((posInMultiblock >= 36&&posInMultiblock <= 38)||(posInMultiblock >= 41&&posInMultiblock <= 43))
		{
			Direction fw = facing.rotateY();
			if(mirrored|posInMultiblock%5==3)
				fw = fw.getOpposite();
			if(posInMultiblock%5==2)
				fw = null;
			float minX = fw==Direction.EAST?.125f: 0;
			float maxX = fw==Direction.WEST?.875f: 1;
			float minZ = fw==Direction.SOUTH?.125f: 0;
			float maxZ = fw==Direction.NORTH?.875f: 1;
			if(posInMultiblock <= 38)
			{
				minX -= facing==Direction.EAST?.875f: 0;
				maxX += facing==Direction.WEST?.875f: 0;
				minZ -= facing==Direction.SOUTH?.875f: 0;
				maxZ += facing==Direction.NORTH?.875f: 0;
			}
			return new float[]{minX, .5f, minZ, maxX, 1, maxZ};
		}
		else if(posInMultiblock==40||posInMultiblock==44)
		{
			Direction fl = posInMultiblock==44?facing.getOpposite(): facing;
			return new float[]{fl==Direction.NORTH?.125f: fl==Direction.SOUTH?.625f: 0, .125f, fl==Direction.EAST?.125f: fl==Direction.WEST?.625f: 0, fl==Direction.SOUTH?.875f: fl==Direction.NORTH?.375f: 1, .375f, fl==Direction.WEST?.875f: fl==Direction.EAST?.375f: 1};
		}
		else if(posInMultiblock >= 46&&posInMultiblock <= 48)
			return new float[]{facing==Direction.WEST?.25f: 0, 0, facing==Direction.NORTH?.25f: 0, facing==Direction.EAST?.75f: 1, 1, facing==Direction.SOUTH?.75f: 1};
		else if(posInMultiblock==97)
			return new float[]{facing.getAxis()==Axis.X?.375f: 0, 0, facing.getAxis()==Axis.Z?.375f: 0, facing.getAxis()==Axis.X?.625f: 1, 1, facing.getAxis()==Axis.Z?.625f: 1};
		else if(posInMultiblock==122)
			return new float[]{facing==Direction.WEST?.3125f: 0, 0, facing==Direction.NORTH?.3125f: 0, facing==Direction.EAST?.6875f: 1, .9375f, facing==Direction.SOUTH?.6875f: 1};
		else if(posInMultiblock==117)
			return new float[]{0, .625f, 0, 1, .9375f, 1};
		else if(posInMultiblock==112)
			return new float[]{facing==Direction.EAST?.125f: 0, 0, facing==Direction.SOUTH?.125f: 0, facing==Direction.WEST?.875f: 1, .9375f, facing==Direction.NORTH?.875f: 1};
		else if(posInMultiblock==51||posInMultiblock==53||posInMultiblock==96||posInMultiblock==98||posInMultiblock==121||posInMultiblock==123)
		{
			Direction fw = facing.rotateY();
			if(mirrored^posInMultiblock%5==3)
				fw = fw.getOpposite();
			return new float[]{fw==Direction.EAST?.5f: 0, 0, fw==Direction.SOUTH?.5f: 0, fw==Direction.WEST?.5f: 1, 1, fw==Direction.NORTH?.5f: 1};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		if(posInMultiblock%15==7)
			return null;
		Direction fl = facing;
		Direction fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();
		if(posInMultiblock==0)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			float minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .125f;
			float maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .25f;
			float minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .125f;
			float maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .25f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .75f;
			maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .875f;
			minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .75f;
			maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .875f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(posInMultiblock >= 46&&posInMultiblock <= 48)
		{
			float minX = fl==Direction.WEST?.25f: 0;
			float maxX = fl==Direction.EAST?.75f: 1;
			float minZ = fl==Direction.NORTH?.25f: 0;
			float maxZ = fl==Direction.SOUTH?.75f: 1;
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==Direction.WEST?0: fl==Direction.EAST?.75f: .25f;
			maxX = fl==Direction.EAST?1: fl==Direction.WEST?.25f: .75f;
			minZ = fl==Direction.NORTH?0: fl==Direction.SOUTH?.75f: .25f;
			maxZ = fl==Direction.SOUTH?1: fl==Direction.NORTH?.25f: .75f;
			list.add(new AxisAlignedBB(minX, .25f, minZ, maxX, .75, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		else if(posInMultiblock%25 >= 10&&(posInMultiblock%5==0||posInMultiblock%5==4))
		{
			List<AxisAlignedBB> list = posInMultiblock < 25?Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ())): new ArrayList(2);
			if(posInMultiblock%5==4)
				fw = fw.getOpposite();
			float minX = fw==Direction.EAST?.5f: 0;
			float maxX = fw==Direction.WEST?.5f: 1;
			float minZ = fw==Direction.SOUTH?.5f: 0;
			float maxZ = fw==Direction.NORTH?.5f: 1;
			if(posInMultiblock%25/5!=3)
				list.add(new AxisAlignedBB(minX, posInMultiblock < 25?.5: 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock < 25)
			{
				minX = fw==Direction.EAST?.125f: fw==Direction.WEST?.625f: fl==Direction.EAST?.375f: -1.625f;
				maxX = fw==Direction.EAST?.375f: fw==Direction.WEST?.875f: fl==Direction.WEST?.625f: 2.625f;
				minZ = fw==Direction.SOUTH?.125f: fw==Direction.NORTH?.625f: fl==Direction.SOUTH?.375f: -1.625f;
				maxZ = fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.875f: fl==Direction.NORTH?.625f: 2.625f;
				AxisAlignedBB aabb = new AxisAlignedBB(minX, .6875, minZ, maxX, .9375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(posInMultiblock%25-10)/5, 0, -fl.getZOffset()*(posInMultiblock%25-10)/5);
				list.add(aabb);

				minX = fw==Direction.EAST?.375f: fw==Direction.WEST?.5f: fl==Direction.EAST?.375f: .375f;
				maxX = fw==Direction.EAST?.5f: fw==Direction.WEST?.625f: fl==Direction.WEST?.625f: .625f;
				minZ = fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.5f: fl==Direction.SOUTH?.375f: .375f;
				maxZ = fw==Direction.SOUTH?.5f: fw==Direction.NORTH?.625f: fl==Direction.NORTH?.625f: .625f;
				aabb = new AxisAlignedBB(minX, .6875, minZ, maxX, .9375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(posInMultiblock%25-10)/5, 0, -fl.getZOffset()*(posInMultiblock%25-10)/5);
				list.add(aabb);

				minX = fw==Direction.EAST?.375f: fw==Direction.WEST?.5f: fl==Direction.EAST?2.375f: -1.625f;
				maxX = fw==Direction.EAST?.5f: fw==Direction.WEST?.625f: fl==Direction.WEST?-1.375f: 2.625f;
				minZ = fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.5f: fl==Direction.SOUTH?2.375f: -1.625f;
				maxZ = fw==Direction.SOUTH?.5f: fw==Direction.NORTH?.625f: fl==Direction.NORTH?-1.375f: 2.625f;
				aabb = new AxisAlignedBB(minX, .6875, minZ, maxX, .9375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(posInMultiblock%25-10)/5, 0, -fl.getZOffset()*(posInMultiblock%25-10)/5);
				list.add(aabb);
			}
			else if(posInMultiblock < 50)
			{
				minX = fw==Direction.EAST?.125f: fw==Direction.WEST?.625f: fl==Direction.EAST?.375f: -1.625f;
				maxX = fw==Direction.EAST?.375f: fw==Direction.WEST?.875f: fl==Direction.WEST?.625f: 2.625f;
				minZ = fw==Direction.SOUTH?.125f: fw==Direction.NORTH?.625f: fl==Direction.SOUTH?.375f: -1.625f;
				maxZ = fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.875f: fl==Direction.NORTH?.625f: 2.625f;
				AxisAlignedBB aabb = new AxisAlignedBB(minX, .125, minZ, maxX, .375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(posInMultiblock%25-10)/5, 0, -fl.getZOffset()*(posInMultiblock%25-10)/5);
				list.add(aabb);

				minX = fw==Direction.EAST?.375f: fw==Direction.WEST?.5f: fl==Direction.EAST?.375f: .375f;
				maxX = fw==Direction.EAST?.5f: fw==Direction.WEST?.625f: fl==Direction.WEST?.625f: .625f;
				minZ = fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.5f: fl==Direction.SOUTH?.375f: .375f;
				maxZ = fw==Direction.SOUTH?.5f: fw==Direction.NORTH?.625f: fl==Direction.NORTH?.625f: .625f;
				aabb = new AxisAlignedBB(minX, .125, minZ, maxX, .375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(posInMultiblock%25-10)/5, 0, -fl.getZOffset()*(posInMultiblock%25-10)/5);
				if(posInMultiblock%5==0)
					aabb = aabb.offset(0, .6875, 0);
				list.add(aabb);
				if(posInMultiblock%5==0)
				{
					minX = fw==Direction.EAST?.125f: fw==Direction.WEST?.625f: fl==Direction.EAST?.375f: .375f;
					maxX = fw==Direction.EAST?.375f: fw==Direction.WEST?.875f: fl==Direction.WEST?.625f: .625f;
					minZ = fw==Direction.SOUTH?.125f: fw==Direction.NORTH?.625f: fl==Direction.SOUTH?.375f: .375f;
					maxZ = fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.875f: fl==Direction.NORTH?.625f: .625f;
					aabb = new AxisAlignedBB(minX, .375, minZ, maxX, 1.0625, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
					aabb = aabb.offset(-fl.getXOffset()*(posInMultiblock%25-10)/5, 0, -fl.getZOffset()*(posInMultiblock%25-10)/5);
					list.add(aabb);
				}
				minX = fw==Direction.EAST?.375f: fw==Direction.WEST?.5f: fl==Direction.EAST?2.375f: -1.625f;
				maxX = fw==Direction.EAST?.5f: fw==Direction.WEST?.625f: fl==Direction.WEST?-1.375f: 2.625f;
				minZ = fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.5f: fl==Direction.SOUTH?2.375f: -1.625f;
				maxZ = fw==Direction.SOUTH?.5f: fw==Direction.NORTH?.625f: fl==Direction.NORTH?-1.375f: 2.625f;
				aabb = new AxisAlignedBB(minX, .125, minZ, maxX, .375, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ());
				aabb = aabb.offset(-fl.getXOffset()*(posInMultiblock%25-10)/5, 0, -fl.getZOffset()*(posInMultiblock%25-10)/5);
				list.add(aabb);
			}
			else if(posInMultiblock==60||posInMultiblock==64)
			{
				minX = fw==Direction.EAST?.375f: fw==Direction.WEST?.5f: .25f;
				maxX = fw==Direction.EAST?.5f: fw==Direction.WEST?.625f: .75f;
				minZ = fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.5f: .25f;
				maxZ = fw==Direction.SOUTH?.5f: fw==Direction.NORTH?.625f: .75f;
				list.add(new AxisAlignedBB(minX, .25, minZ, maxX, .75, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, PlayerEntity player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{46, 47, 48};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{25};
	}

	@Override
	public int getComparatorInputOverride()
	{
		if(posInMultiblock==112)
		{
			TileEntityArcFurnace master = master();
			if(master!=null)
			{
				float f = 0;
				for(int i = 23; i < 26; i++)
					if(!master.inventory.get(i).isEmpty())
						f += 1-(master.inventory.get(i).getDamage()/(float)master.inventory.get(i).getMaxDamage());
				return MathHelper.floor(Math.max(f/3f, 0)*15);
			}
		}
		return super.getComparatorInputOverride();
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean shouldRenderAsActive()
	{
		return hasElectrodes()&&super.shouldRenderAsActive();
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		if(!hasElectrodes())
			return false;
		if(process.recipe!=null&&!process.recipe.slag.isEmpty())
		{
			if(this.inventory.get(SLAG_SLOT).isEmpty())
				return true;
			return ItemHandlerHelper.canItemStacksStack(this.inventory.get(SLAG_SLOT), process.recipe.slag)&&inventory.get(SLAG_SLOT).getCount()+process.recipe.slag.getCount() <= getSlotLimit(SLAG_SLOT);
		}
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(this.output, output, false);
		if(!output.isEmpty())
		{
			BlockPos pos = getPos().add(0, -1, 0).offset(facing, -2);
			Utils.dropStackAtPos(world, pos, output, facing);
		}
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		if(!process.recipe.slag.isEmpty())
		{
			if(this.inventory.get(SLAG_SLOT).isEmpty())
				this.inventory.set(SLAG_SLOT, process.recipe.slag.copy());
			else if(ItemHandlerHelper.canItemStacksStack(this.inventory.get(SLAG_SLOT), process.recipe.slag)||inventory.get(SLAG_SLOT).getCount()+process.recipe.slag.getCount() > getSlotLimit(SLAG_SLOT))
				this.inventory.get(SLAG_SLOT).grow(process.recipe.slag.getCount());
		}
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 12;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 12;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<ArcFurnaceRecipe> process)
	{
		return 0;
	}


	@Override
	public int getComparatedSize()
	{
		return 12;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return slot > SLAG_SLOT?1: 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return OUTPUT_SLOTS;
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
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
	}


	private LazyOptional<IItemHandler> inputHandler = registerConstantCap(
			new IEInventoryHandler(12, this, 0, true, false)
	{
		//ignore the given slot and spread it out
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if(stack.isEmpty())
				return stack;
			stack = stack.copy();
			List<Integer> possibleSlots = new ArrayList<>(12);
			for(int i = 0; i < 12; i++)
			{
				ItemStack here = inventory.get(i);
				if(here.isEmpty())
				{
					if(!simulate)
						inventory.set(i, stack);
					return ItemStack.EMPTY;
				}
				else if(ItemHandlerHelper.canItemStacksStack(stack, here)&&here.getCount() < here.getMaxStackSize())
				{
					possibleSlots.add(i);
				}
			}
			possibleSlots.sort(Comparator.comparingInt(a -> inventory.get(a).getCount()));
			for(int i : possibleSlots)
			{
				ItemStack here = inventory.get(i);
				int fillCount = Math.min(here.getMaxStackSize()-here.getCount(), stack.getCount());
				if(!simulate)
					here.grow(fillCount);
				stack.shrink(fillCount);
				if(stack.isEmpty())
					return ItemStack.EMPTY;
			}
			return stack;
		}
	});
	private LazyOptional<IItemHandler> additiveHandler = registerConstantCap(
			new IEInventoryHandler(4, this, 12, true, false));
	private LazyOptional<IItemHandler> outputHandler = registerConstantCap(
			new IEInventoryHandler(OUT_SLOT_COUNT, this, FIRST_OUT_SLOT, false, true));
	private LazyOptional<IItemHandler> slagHandler = registerConstantCap(
			new IEInventoryHandler(1, this, SLAG_SLOT, false, true));
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityArcFurnace master = master();
			if(master==null)
				return LazyOptional.empty();
			if(posInMultiblock==MAIN_OUT_POS)
				return master.outputHandler.cast();
			else if(posInMultiblock==SLAG_OUT_POS)
				return master.slagHandler.cast();
			else if(posInMultiblock==(mirrored?88: 86))
				return master.inputHandler.cast();
			else if(posInMultiblock==(mirrored?86: 88))
				return master.additiveHandler.cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public ArcFurnaceRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected ArcFurnaceRecipe readRecipeFromNBT(CompoundNBT tag)
	{
		return ArcFurnaceRecipe.loadFromNBT(tag);
	}

	@Override
	@Nullable
	protected MultiblockProcess<ArcFurnaceRecipe> loadProcessFromNBT(CompoundNBT tag)
	{
		ArcFurnaceRecipe recipe = readRecipeFromNBT(tag);
		if(recipe!=null)
		{
			MultiblockProcessArcFurnace process = new MultiblockProcessArcFurnace(recipe, tag.getIntArray("process_inputSlots"));
			if(tag.hasKey("process_inputAmounts"))
				process.setInputAmounts(tag.getIntArray("process_inputAmounts"));
			return process;
		}
		return null;
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return formed&&(posInMultiblock==2||posInMultiblock==25||(posInMultiblock > 25&&posInMultiblock%5 > 0&&posInMultiblock%5 < 4&&posInMultiblock%25/5 < 4));
	}

	@Nonnull
	@Override
	public ResourceLocation getGuiName()
	{
		return Lib.GUIID_ArcFurnace;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return master();
	}

	@Override
	public boolean shoudlPlaySound(String sound)
	{
		return false;
	}

	public static class MultiblockProcessArcFurnace extends MultiblockProcessInMachine<ArcFurnaceRecipe>
	{
		public MultiblockProcessArcFurnace(ArcFurnaceRecipe recipe, int... inputSlots)
		{
			super(recipe, inputSlots);
		}

		@Override
		protected NonNullList<ItemStack> getRecipeItemOutputs(TileEntityPoweredMultiblock<?, ArcFurnaceRecipe> multiblock)
		{
			ItemStack input = multiblock.getInventory().get(this.inputSlots[0]);
			NonNullList<ItemStack> additives = NonNullList.withSize(4, ItemStack.EMPTY);
			for(int i = 0; i < 4; i++)
				additives.set(i, !multiblock.getInventory().get(12+i).isEmpty()?multiblock.getInventory().get(12+i).copy(): ItemStack.EMPTY);
			return recipe.getOutputs(input, additives);
		}

		@Override
		protected void processFinish(TileEntityPoweredMultiblock<?, ArcFurnaceRecipe> te)
		{
			super.processFinish(te);
			te.getWorld().addBlockEvent(te.getPos(), te.getBlockState().getBlock(), 0, 40);
		}
	}

	public boolean hasElectrodes()
	{
		for(int i = 23; i < 26; i++)
			if(inventory.get(i).isEmpty())
				return false;
		return true;
	}
}