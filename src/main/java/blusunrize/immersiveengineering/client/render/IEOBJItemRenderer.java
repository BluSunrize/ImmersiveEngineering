/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.client.ieobj.DefaultCallback;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.client.models.obj.GlobalTempData;
import blusunrize.immersiveengineering.client.models.obj.SpecificIEOBJModel;
import blusunrize.immersiveengineering.client.models.obj.SpecificIEOBJModel.ShadedQuads;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.InvertingVertexBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import malte0811.modelsplitter.model.Group;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class IEOBJItemRenderer extends BlockEntityWithoutLevelRenderer
{
	public IEOBJItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_)
	{
		super(p_172550_, p_172551_);
	}

	@Override
	public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext transformType, @Nonnull PoseStack matrixStackIn, @Nonnull MultiBufferSource bufferIn,
							 int combinedLightIn, int combinedOverlayIn)
	{
		renderByItem(stack, transformType, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, GlobalTempData.getActiveModel());
	}

	public <T> void renderByItem(
			ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn, SpecificIEOBJModel<T> model
	)
	{
		ItemCallback<T> callback;
		{
			IEOBJCallback<T> baseCallback = model.getCallback();
			if(baseCallback instanceof ItemCallback<T> itemCB)
				callback = itemCB;
			else
				callback = DefaultCallback.cast();
		}
		float partialTicks = mc().getTimer().getGameTimeDeltaTicks();
		Set<String> visible = new HashSet<>();
		for(String g : model.getGroups().keySet())
			if(callback.shouldRenderGroup(model.getKey(), g, null))
				visible.add(g);
		LivingEntity entity = GlobalTempData.getActiveHolder();
		Vector3f baseTransformScale = model.getBaseTransforms(transformType).scale;
		if(baseTransformScale.x()*baseTransformScale.y()*baseTransformScale.z() < 0)
		{
			 MultiBufferSource oldBufferIn = bufferIn;
			 bufferIn = type -> new InvertingVertexBuffer(4, oldBufferIn.getBuffer(type));
		}
		for(List<String> groups : callback.getSpecialGroups(stack, transformType, entity))
		{
			Transformation mat = callback.getTransformForGroups(stack, groups, transformType, entity,
					partialTicks);
			matrixStackIn.pushTransformation(mat);
			renderQuadsForGroups(groups, model, callback, stack, matrixStackIn, bufferIn, visible,
					combinedLightIn, combinedOverlayIn);
			matrixStackIn.popPose();
		}
		renderQuadsForGroups(List.copyOf(visible), model, callback, stack, matrixStackIn,
				bufferIn, visible, combinedLightIn, combinedOverlayIn);
	}

	private <T> void renderQuadsForGroups(List<String> groups, SpecificIEOBJModel<T> model, ItemCallback<T> callback,
										  ItemStack stack, PoseStack matrix, MultiBufferSource buffer,
										  Set<String> visible, int light, int overlay)
	{
		List<ShadedQuads> quadsByLayer = new ArrayList<>();
		for(String groupName : groups)
		{
			Group<OBJMaterial> group = model.getGroups().get(groupName);
			if(visible.contains(groupName)&&callback.shouldRenderGroup(model.getKey(), groupName, null))
				quadsByLayer.addAll(model.addQuadsForGroup(groupName, group, true)
						.stream().filter(Objects::nonNull).toList());
			visible.remove(groupName);
		}
		matrix.pushPose();
		for(ShadedQuads quadsForLayer : quadsByLayer)
		{
			boolean bright = callback.areGroupsFullbright(stack, groups);
			RenderType baseType;
			ResourceLocation atlas = InventoryMenu.BLOCK_ATLAS;
			Color4 color = quadsForLayer.layer().getColor();
			List<RenderType> renderTypes = model.getRenderTypes(stack, Minecraft.useFancyGraphics());
			if(bright)
				baseType = IERenderTypes.getFullbrightTranslucent(atlas);
			else if(quadsForLayer.layer().isTranslucent()||color.a() < 1)
				baseType = RenderType.entityTranslucent(atlas);
			else if(!renderTypes.isEmpty())
				baseType = renderTypes.get(0);
			else
				baseType = RenderType.entityCutoutNoCull(atlas);
			RenderType actualType = quadsForLayer.layer().getRenderType(baseType);
			VertexConsumer builder = buffer.getBuffer(actualType);
			for(BakedQuad quad : quadsForLayer.quadsInLayer())
				builder.putBulkData(matrix.last(), quad, color.r(), color.g(), color.b(), color.a(), light, overlay, false);
			matrix.scale(1.0005F, 1.0005F, 1.0005F);
		}
		matrix.popPose();
	}
}
