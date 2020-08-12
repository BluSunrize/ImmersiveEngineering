/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher.Result;
import blusunrize.immersiveengineering.common.blocks.multiblocks.StaticTemplateManager;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TemplateMultiblock implements MultiblockHandler.IMultiblock
{
	private final ResourceLocation loc;
	protected final BlockPos masterFromOrigin;
	protected final BlockPos triggerFromOrigin;
	protected final List<BlockMatcher.MatcherPredicate> additionalPredicates;
	@Nullable
	private Template template;
	@Nullable
	private ItemStack[] materials;
	private BlockState trigger = Blocks.AIR.getDefaultState();

	public TemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin,
							  List<BlockMatcher.MatcherPredicate> additionalPredicates)
	{
		this.loc = loc;
		this.masterFromOrigin = masterFromOrigin;
		this.triggerFromOrigin = triggerFromOrigin;
		this.additionalPredicates = additionalPredicates;
	}

	public TemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin)
	{
		this(loc, masterFromOrigin, triggerFromOrigin, ImmutableMap.of());
	}

	public TemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Map<Block, Tag<Block>> tags)
	{
		this(loc, masterFromOrigin, triggerFromOrigin, ImmutableList.of(
				(expected, found, world, pos) -> {
					Tag<Block> tag = tags.get(expected.getBlock());
					if(tag!=null)
					{
						if(found.isIn(tag))
							return Result.allow(2);
						else
							return Result.deny(2);
					}
					else
						return Result.DEFAULT;
				}
		));
	}

	@Nonnull
	protected Template getTemplate()
	{
		if(template==null)//TODO reset on resource reload
		{
			try
			{
				template = StaticTemplateManager.loadStaticTemplate(loc);
				List<Template.BlockInfo> blocks = template.blocks.get(0);
				for(int i = 0; i < blocks.size(); i++)
				{
					Template.BlockInfo info = blocks.get(i);
					if(info.pos.equals(triggerFromOrigin))
						trigger = info.state;
					if(info.state==Blocks.AIR.getDefaultState())
					{
						blocks.remove(i);
						i--;
					}
					else if(info.state.isAir())
					{
						// Usually means it contains a block that has been renamed
						IELogger.error("Found non-default air block in template "+loc);
					}
				}
				materials = null;
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		return template;
	}

	@Override
	public ResourceLocation getUniqueName()
	{
		return loc;
	}

	@Override
	public boolean isBlockTrigger(BlockState state, Direction d)
	{
		getTemplate();
		Rotation rot = Utils.getRotationBetweenFacings(Direction.NORTH, d.getOpposite());
		if(rot==null)
			return false;
		for(Mirror mirror : getPossibleMirrorStates())
		{
			BlockState modifiedTrigger = applyToState(trigger, mirror, rot);
			if(BlockMatcher.matches(modifiedTrigger, state, null, null, additionalPredicates).isAllow())
				return true;
		}
		return false;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		if(side.getAxis()==Axis.Y)
			side = Direction.fromAngle(player.rotationYaw).getOpposite();
		Rotation rot = Utils.getRotationBetweenFacings(Direction.NORTH, side.getOpposite());
		if(rot==null)
			return false;
		Template template = getTemplate();
		mirrorLoop:
		for(Mirror mirror : getPossibleMirrorStates())
		{
			PlacementSettings placeSet = new PlacementSettings().setMirror(mirror).setRotation(rot);
			BlockPos origin = pos.subtract(Template.transformedBlockPos(placeSet, triggerFromOrigin));
			for(Template.BlockInfo info : template.blocks.get(0))
			{
				BlockPos realRelPos = Template.transformedBlockPos(placeSet, info.pos);
				BlockPos here = origin.add(realRelPos);

				BlockState expected = applyToState(info.state, mirror, rot);
				BlockState inWorld = world.getBlockState(here);
				if(!BlockMatcher.matches(expected, inWorld, world, here, additionalPredicates).isAllow())
					continue mirrorLoop;
			}
			form(world, origin, rot, mirror, side);
			return true;
		}
		return false;
	}

	private BlockState applyToState(BlockState in, Mirror m, Rotation r)
	{
		return in.mirror(m).rotate(r);
	}

	private List<Mirror> getPossibleMirrorStates()
	{
		if(canBeMirrored())
			return ImmutableList.of(Mirror.NONE, Mirror.FRONT_BACK);
		else
			return ImmutableList.of(Mirror.NONE);
	}

	protected void form(World world, BlockPos pos, Rotation rot, Mirror mirror, Direction sideHit)
	{
		BlockPos masterPos = withSettingsAndOffset(pos, masterFromOrigin, mirror, rot);
		for(BlockInfo block : getStructure())
		{
			BlockPos actualPos = withSettingsAndOffset(pos, block.pos, mirror, rot);
			replaceStructureBlock(block, world, actualPos, mirror!=Mirror.NONE, sideHit,
					actualPos.subtract(masterPos));
		}
	}

	protected abstract void replaceStructureBlock(BlockInfo info, World world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster);

	@Override
	public List<BlockInfo> getStructure()
	{
		return getTemplate().blocks.get(0);
	}

	@Override
	public Vec3i getSize()
	{
		return getTemplate().getSize();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean overwriteBlockRender(BlockState state, int iterator)
	{
		return false;
	}

	public static BlockPos withSettingsAndOffset(BlockPos origin, BlockPos relative, Mirror mirror, Rotation rot)
	{
		PlacementSettings settings = new PlacementSettings().setMirror(mirror).setRotation(rot);
		return origin.add(Template.transformedBlockPos(settings, relative));
	}

	public static BlockPos withSettingsAndOffset(BlockPos origin, BlockPos relative, boolean mirrored, Direction facing)
	{
		Rotation rot = Utils.getRotationBetweenFacings(Direction.NORTH, facing);
		if(rot==null)
			return origin;
		return withSettingsAndOffset(origin, relative, mirrored?Mirror.FRONT_BACK: Mirror.NONE,
				rot);
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		if(materials==null)
		{
			List<BlockInfo> structure = getStructure();
			List<ItemStack> ret = new ArrayList<>(structure.size());
			RayTraceResult rtr = new BlockRayTraceResult(Vec3d.ZERO, Direction.DOWN, BlockPos.ZERO, false);
			for(BlockInfo info : structure)
			{
				ItemStack picked = Utils.getPickBlock(info.state, rtr, ImmersiveEngineering.proxy.getClientPlayer());
				boolean added = false;
				for(ItemStack existing : ret)
					if(ItemStack.areItemsEqual(existing, picked))
					{
						existing.grow(1);
						added = true;
						break;
					}
				if(!added)
					ret.add(picked.copy());
			}
			materials = ret.toArray(new ItemStack[0]);
		}
		return materials;
	}

	@Override
	public void disassemble(World world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation)
	{
		Mirror mirror = mirrored?Mirror.FRONT_BACK: Mirror.NONE;
		Rotation rot = Utils.getRotationBetweenFacings(Direction.NORTH, clickDirectionAtCreation);
		Preconditions.checkNotNull(rot);
		for(BlockInfo block : getStructure())
		{
			BlockPos actualPos = withSettingsAndOffset(origin, block.pos, mirror, rot);
			prepareBlockForDisassembly(world, actualPos);
			world.setBlockState(actualPos, block.state.mirror(mirror).rotate(rot));
		}
	}

	protected void prepareBlockForDisassembly(World world, BlockPos pos)
	{
	}

	@Override
	public BlockPos getTriggerOffset()
	{
		return triggerFromOrigin;
	}

	public boolean canBeMirrored()
	{
		return true;
	}

}
