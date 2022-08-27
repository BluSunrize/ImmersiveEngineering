package blusunrize.immersiveengineering.mixin.accessors.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraftforge.client.ChunkRenderTypeSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SimpleBakedModel.class)
public interface SimpleModelAccess
{
	@Accessor(remap = false)
	ChunkRenderTypeSet getBlockRenderTypes();

	@Accessor(remap = false)
	List<RenderType> getItemRenderTypes();

	@Accessor(remap = false)
	List<RenderType> getFabulousItemRenderTypes();
}
