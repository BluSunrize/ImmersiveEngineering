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
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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
import java.util.Set;

public class CrusherBlockEntity extends PoweredMultiblockBlockEntity<CrusherBlockEntity, CrusherRecipe>
		implements ISoundBE, IBlockBounds, IEClientTickableBE
{
	public float animation_barrelRotation = 0;

	public CrusherBlockEntity(BlockEntityType<CrusherBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.CRUSHER, 32000, true, type, pos, state);
	}

	@Override
	public void tickClient()
	{
		boolean active = shouldRenderAsActive();
		ImmersiveEngineering.proxy.handleTileSound(IESounds.crusher, this, active, .5f, 1);
		if(active)
		{
			animation_barrelRotation += 18f;
			animation_barrelRotation %= 360f;
		}
	}

	public void spawnParticles(ItemStack stack)
	{
		Level level = getLevelNonnull();
		if(level instanceof ServerLevel)
		{
			((ServerLevel)level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), getBlockPos().getX(), getBlockPos().getY() + 2.125, getBlockPos().getZ() + 0.5 , 8, 0, 0, 0, 0.0625);
			((ServerLevel)level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), getBlockPos().getX() + 0.5, getBlockPos().getY() + 2.125, getBlockPos().getZ() + 0.5, 8, 0, 0, 0, 0.0625);
			((ServerLevel)level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack), getBlockPos().getX() + 1, getBlockPos().getY() + 2.125, getBlockPos().getZ() + 0.5, 8, 0, 0, 0, 0.0625);
		}
	}

	private Pair<BlockState, AABB> renderAABB;

	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderAABB==null||renderAABB.getFirst()!=getBlockState())
			renderAABB = Pair.of(getBlockState(), new AABB(
					getBlockPos().getX()-(getFacing().getAxis()==Axis.Z?2: 1),
					getBlockPos().getY(),
					getBlockPos().getZ()-(getFacing().getAxis()==Axis.X?2: 1),
					getBlockPos().getX()+(getFacing().getAxis()==Axis.Z?3: 2),
					getBlockPos().getY()+3,
					getBlockPos().getZ()+(getFacing().getAxis()==Axis.X?3: 2)
			));
		return renderAABB.getSecond();
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
			= CachedShapesWithTransform.createForMultiblock(CrusherBlockEntity::getShape);

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
			CrusherBlockEntity master = master();
			if(master==null)
				return;
			Vec3 center = Vec3.atCenterOf(master.getBlockPos()).add(0, 0.25, 0);
			AABB crusherInternal = new AABB(center.x-1.0625, center.y, center.z-1.0625, center.x+1.0625, center.y+1.25, center.z+1.0625);
			if(!entity.getBoundingBox().intersects(crusherInternal))
				return;
			if(entity instanceof ItemEntity itemEntity)
			{
				ItemStack stack = itemEntity.getItem();
				if(stack.isEmpty())
					return;
				CrusherRecipe recipe = master.findRecipeForInsertion(stack);
				if(recipe==null)
					return;
				ItemStack displayStack = recipe.getDisplayStack(stack);
				MultiblockProcess<CrusherRecipe> process = new MultiblockProcessInWorld<>(recipe, this::getRecipeForId, .5f, Utils.createNonNullItemStackListFromItemStack(displayStack));
				if(master.addProcessToQueue(process, true, true))
				{
					master.addProcessToQueue(process, false, true);
					stack = stack.copy();
					stack.shrink(displayStack.getCount());
					if(stack.isEmpty())
						entity.discard();
					else
						itemEntity.setItem(stack);
				}
			}
			else if(entity instanceof LivingEntity&&(!(entity instanceof Player player)||!player.getAbilities().invulnerable))
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
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(new MultiblockFace(4, 1, 1, RelativeBlockFace.UP));
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
		return Mth.ceil(fill*14.0F)+(fill > 0?1: 0);
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return true;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<CrusherRecipe> process)
	{
		if (processQueue.size() > 0 && processQueue.get(0).getRecipe(getLevelNonnull()) != null)
		{
			spawnParticles(processQueue.get(0).getRecipe(getLevelNonnull()).input.getItems()[0]);
		}
		return true;
	}

	@Override
	protected boolean shouldSyncProcessQueue()
	{
		return false;
	}

	private final CapabilityReference<IItemHandler> output = CapabilityReference.forBlockEntityAt(this,
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
		CrusherBlockEntity master = master();
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
		CrusherBlockEntity master = master();
		if(master!=this&&master!=null)
			return master.getCurrentProcessesMax();
		int[] ia = new int[processQueue.size() > 0?1: 0];
		for(int i = 0; i < ia.length; i++)
			ia[i] = processQueue.get(i).getMaxTicks(level);
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
	public void doGraphicalUpdates()
	{
	}

	private final MultiblockCapability<IItemHandler> insertionHandler = MultiblockCapability.make(
			this, be -> be.insertionHandler, CrusherBlockEntity::master,
			registerCapability(new MultiblockInventoryHandler_DirectProcessing<>(this).setProcessStacking(true))
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY&&isInInput(false))
			return insertionHandler.getAndCast();
		return super.getCapability(capability, facing);
	}

	@Override
	public CrusherRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return CrusherRecipe.findRecipe(level, inserting);
	}

	@Override
	protected CrusherRecipe getRecipeForId(Level level, ResourceLocation id)
	{
		return CrusherRecipe.RECIPES.getById(level, id);
	}

	@Override
	public boolean shouldPlaySound(String sound)
	{
		return shouldRenderAsActive();
	}
}