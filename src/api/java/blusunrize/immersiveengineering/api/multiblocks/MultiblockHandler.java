/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author BluSunrize - 27.04.2015
 * <br>
 * The handler for IE multiblocks. TO handle custom structures, create a class implementing IMultiblock and register it
 */
public class MultiblockHandler
{
	private static final Direction[] DIRECTIONS = Direction.values();

	static ArrayList<IMultiblock> multiblocks = new ArrayList<>();
	static Map<ResourceLocation, IMultiblock> byUniqueName = new HashMap<>();

	public static void registerMultiblock(IMultiblock multiblock)
	{
		multiblocks.add(multiblock);
		byUniqueName.put(multiblock.getUniqueName(), multiblock);
	}

	public static ArrayList<IMultiblock> getMultiblocks()
	{
		return multiblocks;
	}

	@Nullable
	public static IMultiblock getByUniqueName(ResourceLocation name)
	{
		return byUniqueName.get(name);
	}

	public interface IMultiblock
	{
		/**
		 * @return name of the Multiblock. This is used for the interdiction NBT system on the hammer, so this name /must/ be unique.
		 */
		ResourceLocation getUniqueName();

		/**
		 * Check whether the given block can be used to trigger the structure creation of the multiblock.<br>
		 * Basically, a less resource-intensive preliminary check to avoid checking every structure.
		 */
		boolean isBlockTrigger(BlockState state, Direction side, @Nullable Level world);

		/**
		 * This method checks the structure and sets the new one.
		 *
		 * @return if the structure was valid and transformed
		 */
		boolean createStructure(Level world, BlockPos pos, Direction side, Player player);

		/**
		 * TODO
		 *
		 * @param world
		 * @return
		 */
		List<StructureBlockInfo> getStructure(@Nullable Level world);

		/**
		 * An array of ItemStacks that summarizes the total amount of materials needed for the structure. Will be rendered in the Engineer's Manual
		 *
		 * @return
		 */
		@OnlyIn(Dist.CLIENT)
		ItemStack[] getTotalMaterials();

		/**
		 * Use this to overwrite the rendering of a Multiblock's Component
		 */
		@OnlyIn(Dist.CLIENT)
		boolean overwriteBlockRender(BlockState state, int iterator);

		/**
		 * returns the scale modifier to be applied when rendering the structure in the IE manual
		 */
		float getManualScale();

		/**
		 * returns true to add a button that will switch between the assembly of multiblocks and the finished render
		 */
		@OnlyIn(Dist.CLIENT)
		boolean canRenderFormedStructure();

		/**
		 * use this function to render the complete multiblock
		 *
		 * @param transform
		 * @param buffer
		 */
		@OnlyIn(Dist.CLIENT)
		void renderFormedStructure(PoseStack transform, MultiBufferSource buffer);

		Vec3i getSize(@Nullable Level world);

		void disassemble(Level world, BlockPos startPos, boolean mirrored, Direction clickDirectionAtCreation);

		BlockPos getTriggerOffset();
	}

	public static MultiblockFormEvent postMultiblockFormationEvent(Player player, IMultiblock multiblock, BlockPos clickedBlock, ItemStack hammer)
	{
		MultiblockFormEvent event = new MultiblockFormEvent(player, multiblock, clickedBlock, hammer);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	/**
	 * This event is fired BEFORE the multiblock is attempted to be formed.<br>
	 * No checks of the structure have been made. The event simply exists to cancel the formation of the multiblock before it ever happens.
	 */
	@Cancelable
	public static class MultiblockFormEvent extends PlayerEvent
	{
		private final IMultiblock multiblock;
		private final BlockPos clickedBlock;
		private final ItemStack hammer;

		public MultiblockFormEvent(Player player, IMultiblock multiblock, BlockPos clickedBlock, ItemStack hammer)
		{
			super(player);
			this.multiblock = multiblock;
			this.clickedBlock = clickedBlock;
			this.hammer = hammer;
		}

		public IMultiblock getMultiblock()
		{
			return multiblock;
		}

		public BlockPos getClickedBlock()
		{
			return clickedBlock;
		}

		public ItemStack getHammer()
		{
			return hammer;
		}
	}
}