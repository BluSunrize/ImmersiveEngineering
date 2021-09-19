/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CrusherTileEntity extends PoweredMultiblockTileEntity<CrusherTileEntity, CrusherRecipe> implements ISoundTile, IBlockBounds
{
	public float animation_barrelRotation = 0;

	public CrusherTileEntity()
	{
		super(IEMultiblocks.CRUSHER, 32000, true, IETileTypes.CRUSHER.get());
	}

	@Override
	public void tick()
	{
		super.tick();
		if(level.isClientSide&&!isDummy())
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
	private AABB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AABB getRenderBoundingBox()
	{
		//		if(renderAABB==null)
		//			if(pos==17)
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord-(facing==2||facing==3?2:1),yCoord,zCoord-(facing==4||facing==5?2:1), xCoord+(facing==2||facing==3?3:2),yCoord+3,zCoord+(facing==4||facing==5?3:2));
		//			else
		//				renderAABB = AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
		//		return renderAABB;
		return new AABB(getBlockPos().getX()-(getFacing().getAxis()==Axis.Z?2: 1), getBlockPos().getY(), getBlockPos().getZ()-(getFacing().getAxis()==Axis.X?2: 1), getBlockPos().getX()+(getFacing().getAxis()==Axis.Z?3: 2), getBlockPos().getY()+3, getBlockPos().getZ()+(getFacing().getAxis()==Axis.X?3: 2));
	}

	public static VoxelShape getBasicShape(BlockPos posInMultiblock)
	{
		Set<BlockPos> slabs = ImmutableSet.of(
				new BlockPos(3, 0, 2),
				new BlockPos(1, 0, 2),
				new BlockPos(0, 0, 2),
				new BlockPos(3, 0, 1),
				new BlockPos(1, 0, 1),
				new BlockPos(3, 0, 0),
				new BlockPos(2, 0, 0),
				new BlockPos(1, 0, 0),
				new BlockPos(0, 0, 0),
				new BlockPos(0, 1, 1)
		);
		if(slabs.contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		if(new BlockPos(2, 1, 1).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .75f, 1);
		if(new BlockPos(2, 2, 1).equals(posInMultiblock))
			return Shapes.box(0, 0, 0, 0, 0, 0);

		if(posInMultiblock.getY() > 0&&posInMultiblock.getX() > 0&&posInMultiblock.getX() < 4)
		{
			float minX = 0;
			float maxX = 1;
			float minZ = 0;
			float maxZ = 1;
			if(posInMultiblock.getX()==3)
			{
				minX = .1875f;
				maxX = 1;
				minZ = 0;
				maxZ = 1;
			}
			else if(posInMultiblock.getX()==1)
			{
				minX = 0;
				maxX = .8125f;
				minZ = 0;
				maxZ = 1;
			}
			if(posInMultiblock.getZ()==2)
				maxZ = .8125f;

			return Shapes.box(minX, 0, minZ, maxX, 1, maxZ);
		}
		if(new BlockPos(0, 1, 2).equals(posInMultiblock))
			return Shapes.box(0, 0, .5f, 1, 1, 1);

		return Shapes.block();
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES
			= CachedShapesWithTransform.createForMultiblock(CrusherTileEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}

	private static List<AABB> getShape(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getZ()==1&&posInMultiblock.getX()==2)
			return getBasicShape(posInMultiblock).toAabbs();
		if(new BlockPos(0, 0, 2).equals(posInMultiblock))
		{
			List<AABB> list = Lists.newArrayList(new AABB(0, 0, 0, 1, .5f, 1));
			list.add(new AABB(.125, .5f, .625, .25, 1, .875));
			list.add(new AABB(.75, .5f, .625, .875, 1, .875));
			return list;
		}
		if(new BoundingBox(1, 1, 1, 3, 2, 1)
				.isInside(posInMultiblock))
		{
			List<AABB> list = new ArrayList<>(3);
			float minY = .5f;
			float minX = posInMultiblock.getX()==1?.4375f: 0;
			float maxX = posInMultiblock.getX()==3?.5625f: 1;
			if(posInMultiblock.getY()==1)
				list.add(new AABB(minX, .5f, 0, maxX, .75f, 1));
			else
				minY = 0;

			if(posInMultiblock.getX()==1)
				minX = .1875f;
			else
				minX = posInMultiblock.getX()==3?.5625f: 0;
			maxX = posInMultiblock.getX()==3?.8125f: posInMultiblock.getX()==1?.4375f: 1;
			list.add(new AABB(minX, minY, 0, maxX, 1, 1));
			return list;
		}
		if((posInMultiblock.getZ()==0||posInMultiblock.getZ()==2)&&posInMultiblock.getY() > 0&&posInMultiblock.getX() > 0&&posInMultiblock.getX() < 4)
		{
			boolean front = posInMultiblock.getZ()==0;
			boolean right = posInMultiblock.getX()==1;
			boolean left = posInMultiblock.getX()==3;
			List<AABB> list = new ArrayList<>(3);
			float minY = .5f;
			float minX = right?.4375f: 0;
			float maxX = left?.5625f: 1;
			float minZ = front?.4375f: 0;
			float maxZ = !front?.5625f: 1;
			if(posInMultiblock.getY()==1)
				list.add(new AABB(minX, .5f, minZ, maxX, .75f, maxZ));
			else
				minY = 0;

			minX = right?.1875f: (float)0;
			maxX = left?.8125f: (float)1;
			minZ = front?.1875f: .5625f;
			maxZ = !front?.8125f: .4375f;
			list.add(new AABB(minX, minY, minZ, maxX, 1, maxZ));
			if(!ImmutableSet.of(
					new BlockPos(2, 1, 2),
					new BlockPos(2, 2, 2),
					new BlockPos(2, 1, 0),
					new BlockPos(2, 2, 0)
			).contains(posInMultiblock))
			{
				minX = right?.1875f: .5625f;
				maxX = left?.8125f: .4375f;
				minZ = front?.4375f: 0;
				maxZ = !front?.5625f: 1;
				list.add(new AABB(minX, minY, minZ, maxX, 1, maxZ));

				if(ImmutableSet.of(
						new BlockPos(3, 1, 2),
						new BlockPos(2, 1, 2),
						new BlockPos(1, 1, 2),
						new BlockPos(3, 1, 0),
						new BlockPos(2, 1, 0),
						new BlockPos(1, 1, 0)
				).contains(posInMultiblock))
				{
					minZ = front?.25f: .5f;
					maxZ = front?.5f: .75f;
					list.add(new AABB(0.25, 0, minZ, 0.75, .5f, maxZ));
				}
			}
			return list;
		}
		if(ImmutableSet.of(
				new BlockPos(3, 0, 2),
				new BlockPos(1, 0, 2),
				new BlockPos(3, 0, 0),
				new BlockPos(1, 0, 0)
		).contains(posInMultiblock))
			return Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==3,
					new AABB(0.25, 0.5, 0.5, 0.5, 1, 0.75),
					new AABB(0, 0, 0, 1, .5f, 1)
			);

		return getBasicShape(posInMultiblock).toAabbs();
	}

	private boolean isInInput(boolean allowMiddleLayer)
	{
		if(posInMultiblock.getY()==2||(allowMiddleLayer&&posInMultiblock.getY()==1))
			return posInMultiblock.getX() > 0&&posInMultiblock.getX() < 4;
		return false;
	}

	@Override
	public void onEntityCollision(Level world, Entity entity)
	{
		// Actual intersection with the input box is checked later
		boolean bpos = isInInput(true);
		if(bpos&&!world.isClientSide&&entity.isAlive()&&!isRSDisabled())
		{
			CrusherTileEntity master = master();
			if(master==null)
				return;
			Vec3 center = Vec3.atCenterOf(master.getBlockPos()).add(0, 0.25, 0);
			AABB crusherInternal = new AABB(center.x-1.0625, center.y, center.z-1.0625, center.x+1.0625, center.y+1.25, center.z+1.0625);
			if(!entity.getBoundingBox().intersects(crusherInternal))
				return;
			if(entity instanceof ItemEntity&&!((ItemEntity)entity).getItem().isEmpty())
			{
				ItemStack stack = ((ItemEntity)entity).getItem();
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
			else if(entity instanceof LivingEntity&&(!(entity instanceof Player)||!((Player)entity).abilities.invulnerable))
			{
				int consumed = master.energyStorage.extractEnergy(80, true);
				if(consumed > 0)
				{
					master.energyStorage.extractEnergy(consumed, false);
					EventHandler.crusherMap.put(entity.getUUID(), master);
					entity.hurt(IEDamageSources.crusher, consumed/20f);
				}
			}
		}
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(4, 1, 1)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 2)
		);
	}

	@Override
	protected int getComparatorValueOnMaster()
	{
		float fill = processQueue.size()/(float)getProcessQueueMaxLength();
		return Mth.floor(fill*14.0F)+(fill > 0?1: 0);
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

	private CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntityAt(this,
			() -> new DirectionalBlockPos(getBlockPos().offset(0, -1, 0).relative(getFacing(), -2), getFacing()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(this.output, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(level, getBlockPos().offset(0, -1, 0).relative(getFacing(), -2), output, getFacing().getOpposite());
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
		CrusherTileEntity master = master();
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
		CrusherTileEntity master = master();
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

	LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new MultiblockInventoryHandler_DirectProcessing<>(this).setProcessStacking(true)
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY&&isInInput(false))
		{
			CrusherTileEntity master = master();
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
	protected CrusherRecipe getRecipeForId(ResourceLocation id)
	{
		return CrusherRecipe.recipeList.get(id);
	}

	@Override
	public boolean shouldPlaySound(String sound)
	{
		return shouldRenderAsActive();
	}
}