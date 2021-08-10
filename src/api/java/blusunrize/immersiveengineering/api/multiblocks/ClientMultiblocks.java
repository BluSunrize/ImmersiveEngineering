package blusunrize.immersiveengineering.api.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class ClientMultiblocks
{
	private static final Map<IMultiblock, MultiblockRenderProperties> CACHED_PROPERTIES = new IdentityHashMap<>();

	public static MultiblockRenderProperties get(IMultiblock multiblock) {
		return CACHED_PROPERTIES.computeIfAbsent(multiblock, mb -> {
			Mutable<MultiblockRenderProperties> result = new MutableObject<>();
			mb.initializeClient(result::setValue);
			return Objects.requireNonNull(result.getValue(), "Did not get client properties for "+mb.getUniqueName());
		});
	}

	public interface MultiblockRenderProperties {
		/**
		 * A list of ItemStacks that summarizes the total amount of materials needed for the structure. Will be rendered in the Engineer's Manual
		 */
		NonNullList<ItemStack> getTotalMaterials();

		/**
		 * Use this to overwrite the rendering of a Multiblock's Component
		 */
		//TODO is this still used anywhere? May be something to remove
		default boolean overwriteBlockRender(BlockState state, BlockPos iterator) {
			return false;
		}

		/**
		 * returns true to add a button that will switch between the assembly of multiblocks and the finished render
		 */
		boolean canRenderFormedStructure();

		/**
		 * use this function to render the complete multiblock
		 *
		 */
		void renderFormedStructure(PoseStack transform, MultiBufferSource buffer);
	}
}
