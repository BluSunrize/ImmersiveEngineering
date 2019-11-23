/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.blocks.multiblocks.StaticTemplateManager;
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

//TODO rotaion of blocks?
public abstract class TemplateMultiblock implements MultiblockHandler.IMultiblock
{
	private final ResourceLocation loc;
	private final BlockPos masterFromOrigin;
	public final BlockPos triggerFromOrigin;
	private final Map<Block, Tag<Block>> tags;
	@Nullable
	private Template template;
	@Nullable
	private IngredientStack[] materials;
	private BlockState trigger = Blocks.AIR.getDefaultState();

	public TemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin)
	{
		this(loc, masterFromOrigin, triggerFromOrigin, ImmutableMap.of());
	}

	public TemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Map<Block, Tag<Block>> tags)
	{
		this.loc = loc;
		this.masterFromOrigin = masterFromOrigin;
		this.triggerFromOrigin = triggerFromOrigin;
		this.tags = tags;
	}

	@Nonnull
	private Template getTemplate()
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
				}
				materials = null;
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		return template;
	}

	//TODO make all of these non-final (currently final to make porting easier)

	@Override
	public final ResourceLocation getUniqueName()
	{
		return loc;
	}

	@Override
	public final boolean isBlockTrigger(BlockState state)
	{
		getTemplate();
		//TODO facing dependant
		return state.getBlock()==trigger.getBlock();
	}


	@Override
	public final boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		if(side.getAxis()==Axis.Y)
			side = Direction.fromAngle(player.rotationYaw);
		Rotation rot = Utils.getRotationBetweenFacings(Direction.NORTH, side.getOpposite());
		if(rot==null)
			return false;
		Template template = getTemplate();
		List<Mirror> mirrorStates;
		if(canBeMirrored())
			mirrorStates = ImmutableList.of(Mirror.NONE, Mirror.FRONT_BACK);
		else
			mirrorStates = ImmutableList.of(Mirror.NONE);
		mirrorLoop:
		for(Mirror mirror : mirrorStates)
		{
			PlacementSettings placeSet = new PlacementSettings().setMirror(mirror).setRotation(rot);
			BlockPos origin = pos.subtract(Template.transformedBlockPos(placeSet, triggerFromOrigin));
			for(Template.BlockInfo info : template.blocks.get(0))
			{
				BlockPos realRelPos = Template.transformedBlockPos(placeSet, info.pos);
				BlockPos here = origin.add(realRelPos);

				BlockState expected = info.state.mirror(mirror).rotate(rot);

				BlockState inWorld = world.getBlockState(here);
				boolean valid;
				if(tags.containsKey(expected.getBlock()))
					valid = inWorld.getBlock().isIn(tags.get(expected.getBlock()));
				else
					valid = inWorld==expected;
				if(!valid)
					continue mirrorLoop;
			}
			form(world, origin, rot, mirror, side);
			return true;
		}
		return false;
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
	public final List<BlockInfo> getStructure()
	{
		return getTemplate().blocks.get(0);
	}

	@Override
	public final Vec3i getSize()
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
	public final IngredientStack[] getTotalMaterials()
	{
		if(materials==null)
		{
			List<BlockInfo> structure = getStructure();
			List<IngredientStack> ret = new ArrayList<>(structure.size());
			RayTraceResult rtr = new BlockRayTraceResult(Vec3d.ZERO, Direction.DOWN, BlockPos.ZERO, false);
			for(BlockInfo info : structure)
			{
				ItemStack picked = Utils.getPickBlock(info.state, rtr, ImmersiveEngineering.proxy.getClientPlayer());
				boolean added = false;
				for(IngredientStack existing : ret)
					if(existing.matchesItemStackIgnoringSize(picked))
					{
						++existing.inputSize;
						added = true;
						break;
					}
				if(!added)
					ret.add(new IngredientStack(picked));
			}
			materials = ret.toArray(new IngredientStack[0]);
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
